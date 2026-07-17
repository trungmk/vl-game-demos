using System.Collections.Generic;
using UnityEngine;
using VLPlay;

/// <summary>
/// Minimal on-screen console for device-verifying the VLPlay Unity wrapper.
/// Every VLPlayEvents callback is appended to the log panel — this is the
/// harness the SDK phases (U1+) are verified through. IMGUI so it needs no
/// canvas/prefab wiring; grows into the full tile console at U6.
/// </summary>
public sealed class VLPlayDemoConsole : MonoBehaviour
{
    private readonly List<string> _log = new List<string>();
    private Vector2 _scroll;

    // U2-5: mirrored from the SDK, not from what we asked for — refreshed after every
    // Set so the label proves the round-trip. Cached because OnGUI runs several times
    // per frame and each getter is a real JNI / P-Invoke hop.
    private VLPlayLanguage _language;
    private VLPlayOrientation _orientation;

    // U3-5: Buy targets the first catalog package once Catalog has run; before that it
    // falls back to the stg demo game's known product so Buy works standalone too.
    private string _buyProductId = "com.vlplay.demo.pack_001";
    private bool _buyFromCatalog;

    private void OnEnable()
    {
        VLPlayEvents.OnSignIn += HandleSignIn;
        VLPlayEvents.OnSignOut += HandleSignOut;
        VLPlayEvents.OnSessionExpired += HandleSessionExpired;
        VLPlayEvents.OnAntiAddictionWarn += HandleAaWarn;
        VLPlayEvents.OnAntiAddictionKick += HandleAaKick;
        VLPlayEvents.OnPurchaseCompleted += HandlePurchaseCompleted;
        VLPlayEvents.OnError += HandleError;
    }

    private void OnDisable()
    {
        VLPlayEvents.OnSignIn -= HandleSignIn;
        VLPlayEvents.OnSignOut -= HandleSignOut;
        VLPlayEvents.OnSessionExpired -= HandleSessionExpired;
        VLPlayEvents.OnAntiAddictionWarn -= HandleAaWarn;
        VLPlayEvents.OnAntiAddictionKick -= HandleAaKick;
        VLPlayEvents.OnPurchaseCompleted -= HandlePurchaseCompleted;
        VLPlayEvents.OnError -= HandleError;
    }

    private void Start()
    {
        VLPlaySDK.Init();
        _language = VLPlaySDK.GetLanguage();
        _orientation = VLPlaySDK.GetOrientation();
        Log("Init() — EditorBridge mock in Editor, native bridge on device");
        Log("SDK reports language=" + _language + " orientation=" + _orientation);
    }

    private void HandleSignIn(VLPlayUser u) => Log("OnSignIn → " + u);
    private void HandleSignOut() => Log("OnSignOut");
    private void HandleSessionExpired() => Log("OnSessionExpired");
    private void HandleAaWarn(string m) => Log("OnAntiAddictionWarn → " + m);
    private void HandleAaKick(VLPlayAntiAddictionKick k) => Log("OnAntiAddictionKick → " + k);
    private void HandlePurchaseCompleted() => Log("OnPurchaseCompleted (broadcast — also fires for FAB/webpay buys)");
    private void HandleError(VLPlayError e) => Log("OnError → " + e);

    // ---- U3-5: IAP row ----

    private void LogCatalog()
    {
        Log("GetProductCatalog…");
        VLPlaySDK.GetProductCatalog((packages, error) =>
        {
            if (error != null) { Log("GetProductCatalog FAILED → " + error); return; }
            Log("GetProductCatalog → " + packages.Length + " package(s)");
            foreach (var p in packages) Log("  • " + p);
            if (packages.Length > 0)
            {
                _buyProductId = packages[0].storeProductId;
                _buyFromCatalog = true;
                Log("  Buy now targets " + _buyProductId);
            }
        });
    }

    private void Buy()
    {
        string target = _buyProductId + (_buyFromCatalog ? "" : " (fallback — run Catalog first)");
        Log("Purchase " + target + "…");
        VLPlaySDK.Purchase(_buyProductId, result =>
        {
            // iOS reports a user-cancel as Failed (no cancel marker in the native
            // notification) — expected drift, don't file it as a bug.
            Log("Purchase → " + result);
        });
    }

    private void LogHistory()
    {
        Log("GetPurchaseHistory(1, 10)…");
        VLPlaySDK.GetPurchaseHistory(1, 10, (history, error) =>
        {
            if (error != null) { Log("GetPurchaseHistory FAILED → " + error); return; }
            Log("GetPurchaseHistory → " + history.transactions.Length + " row(s)" +
                (history.totalPages >= 0
                    ? "  (totalPages=" + history.totalPages + " page=" + history.page + " limit=" + history.limit + ")"
                    : "  (pagination unknown on iOS — native drops the envelope)"));
            foreach (var t in history.transactions)
                Log("  • " + t + (t.createdAtEpochMillis > 0 ? "  @" + t.createdAt : ""));
        });
    }

    private void Restore()
    {
        Log("RestorePurchases…");
        VLPlaySDK.RestorePurchases((items, error) =>
        {
            if (error != null) { Log("RestorePurchases FAILED → " + error); return; }
            Log("RestorePurchases → " + items.Length + " item(s)" +
                (items.Length == 0 ? " (nothing to restore is a success)" : ""));
            foreach (var it in items) Log("  • " + it);
        });
    }

    // U2-4: synchronous read of the SDK's last server anti-addiction status.
    private void LogAaStatus()
    {
        var s = VLPlaySDK.GetAntiAddictionStatus();
        if (s == null) { Log("GetAntiAddictionStatus → (none yet — SDK hasn't polled)"); return; }
        Log("GetAntiAddictionStatus → " + s +
            " | today " + s.totalPlayedMinutesToday + "m played / " + s.remainingMinutesToday + "m left" +
            " | session " + s.currentSessionMinutes + "m / " + s.remainingSessionMinutes + "m left" +
            (string.IsNullOrEmpty(s.cooldownUntil) ? "" : " | cooldownUntil " + s.cooldownUntil));
    }

    // U2-5: cycle the SDK's language, then read back what it actually resolves to.
    private void CycleLanguage()
    {
        VLPlayLanguage next;
        switch (_language)
        {
            case VLPlayLanguage.Vietnamese: next = VLPlayLanguage.English; break;
            case VLPlayLanguage.English:    next = VLPlayLanguage.Khmer; break;
            default:                        next = VLPlayLanguage.Vietnamese; break;
        }
        VLPlaySDK.SetLanguage(next);
        _language = VLPlaySDK.GetLanguage();
        Log("SetLanguage(" + next + ") → SDK reports " + _language +
            (next == VLPlayLanguage.Khmer ? "  (khm accepted but untranslated → VI strings)" : ""));
    }

    // U2-5: cycle the orientation of the SDK's own screens (not the game's).
    // Open Login afterwards to see it — the mask applies when the popup presents.
    private void CycleOrientation()
    {
        VLPlayOrientation next;
        switch (_orientation)
        {
            case VLPlayOrientation.FollowSystem: next = VLPlayOrientation.Landscape; break;
            case VLPlayOrientation.Landscape:    next = VLPlayOrientation.Portrait; break;
            default:                             next = VLPlayOrientation.FollowSystem; break;
        }
        VLPlaySDK.SetOrientation(next);
        _orientation = VLPlaySDK.GetOrientation();
        Log("SetOrientation(" + next + ") → SDK reports " + _orientation + " (affects SDK screens only)");
    }

    // U2-5: the CMS gates this game runs under.
    // NOTE: the raw dump carries per-game third-party creds (AppsFlyer dev key, OAuth IDs).
    // Fine for this debug harness; do NOT copy this logging into a shipped game.
    private void LogConfig()
    {
        if (!VLPlaySDK.IsConfigReady())
        {
            Log("Config not ready yet — every IsFeatureEnabled() answers false until it lands");
            return;
        }
        Log("Features: " +
            "identityVerification=" + VLPlaySDK.IsFeatureEnabled(VLPlayFeature.IdentityVerification) +
            " antiAddiction=" + VLPlaySDK.IsFeatureEnabled(VLPlayFeature.AntiAddiction) +
            " guestLogin=" + VLPlaySDK.IsFeatureEnabled(VLPlayFeature.GuestLogin) +
            " appsFlyerTracking=" + VLPlaySDK.IsFeatureEnabled(VLPlayFeature.AppsFlyerTracking) +
            " otpRequired=" + VLPlaySDK.IsFeatureEnabled(VLPlayFeature.OtpRequired) +
            " emailVerification=" + VLPlaySDK.IsFeatureEnabled(VLPlayFeature.EmailVerification));
        var json = VLPlaySDK.GetConfigJson();
        Log("GetConfigJson → " + (json ?? "(null)"));
    }

    private static string Dash(string s)
    {
        return string.IsNullOrEmpty(s) ? "—" : s;
    }

#if UNITY_ANDROID && !UNITY_EDITOR
    private static void ForceSessionExpireAndroid()
    {
        using (var mgr = new AndroidJavaClass("sdk.vlplay.vn.tracking.VLPlaySDKManager"))
            mgr.CallStatic("notifySessionExpired");
    }
#endif

    private void Log(string line)
    {
        _log.Add("[" + Time.time.ToString("0.0") + "] " + line);
        Debug.Log("[VLPlayDemo] " + line);
        _scroll.y = float.MaxValue;
    }

    private void OnGUI()
    {
        const float pad = 12f;
        GUI.skin.button.fontSize = 20;
        GUI.skin.label.fontSize = 16;
        GUILayout.BeginArea(new Rect(pad, pad, Screen.width - pad * 2, Screen.height - pad * 2));

        GUILayout.Label("VLPlay Unity Demo — " + (VLPlaySDK.IsSignedIn ? "SIGNED IN" : "signed out"));

        // U2-5 user card — proves CurrentUser is populated with the real signed-in account.
        var user = VLPlaySDK.CurrentUser;
        if (user != null)
        {
            GUILayout.BeginVertical(GUI.skin.box);
            GUILayout.Label(user.username + (user.isGuest ? "   [GUEST]" : "") + "   id=" + Dash(user.accountId));
            GUILayout.Label("email " + Dash(user.email) + "    phone " + Dash(user.phone));
            GUILayout.EndVertical();
        }

        GUILayout.BeginHorizontal();
        if (GUILayout.Button("Login", GUILayout.Height(56))) VLPlaySDK.SignIn();
        if (GUILayout.Button("Logout", GUILayout.Height(56))) VLPlaySDK.SignOut();
        if (GUILayout.Button("Log Event", GUILayout.Height(56)))
            VLPlaySDK.LogEvent("demo_button", "{\"src\":\"console\"}");
        GUILayout.EndHorizontal();

        // Debug row — these events are otherwise unreachable in a test: a real kick needs a
        // minor's account + a tightened CMS cap, and a session expiry needs the refresh token
        // to actually fail. Both go through the SDK's own debug entry points, so the real
        // native chain runs.
        GUILayout.BeginHorizontal();
        if (GUILayout.Button("AA warn", GUILayout.Height(56))) VLPlaySDK.DebugForceAntiAddiction(false);
        if (GUILayout.Button("AA kick", GUILayout.Height(56))) VLPlaySDK.DebugForceAntiAddiction(true);
        // Real public API (not debug) — reads the SDK's last server status synchronously.
        if (GUILayout.Button("AA status", GUILayout.Height(56))) LogAaStatus();
#if UNITY_ANDROID && !UNITY_EDITOR
        // Android-only: iOS exposes no session-expire hook.
        if (GUILayout.Button("Session expire", GUILayout.Height(56))) ForceSessionExpireAndroid();
#endif
        GUILayout.EndHorizontal();

        // U2-4 passthrough row. Labels show what the SDK reports back, not what we set.
        GUILayout.BeginHorizontal();
        if (GUILayout.Button("Lang: " + _language, GUILayout.Height(56))) CycleLanguage();
        if (GUILayout.Button("Orient: " + _orientation, GUILayout.Height(56))) CycleOrientation();
        if (GUILayout.Button("Config", GUILayout.Height(56))) LogConfig();
        GUILayout.EndHorizontal();

        // U3-5 IAP row. Catalog retargets Buy to the first CMS package; Buy needs a
        // signed-in account + a store-configured product (License Tester / sandbox).
        GUILayout.BeginHorizontal();
        if (GUILayout.Button("Catalog", GUILayout.Height(56))) LogCatalog();
        if (GUILayout.Button("Buy" + (_buyFromCatalog ? "" : "*"), GUILayout.Height(56))) Buy();
        if (GUILayout.Button("History", GUILayout.Height(56))) LogHistory();
        if (GUILayout.Button("Restore", GUILayout.Height(56))) Restore();
        GUILayout.EndHorizontal();

        GUILayout.Label("Event log:");
        _scroll = GUILayout.BeginScrollView(_scroll, GUI.skin.box);
        for (int i = 0; i < _log.Count; i++) GUILayout.Label(_log[i]);
        GUILayout.EndScrollView();

        GUILayout.EndArea();
    }
}
