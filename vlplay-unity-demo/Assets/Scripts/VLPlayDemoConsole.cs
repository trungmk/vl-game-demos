using System.Collections.Generic;
using UnityEngine;
using VLPlay;

/// <summary>
/// On-screen console for device-verifying the VLPlay Unity wrapper (U6-1 polish).
/// Landscape two-pane layout parity the g01 demo hubs: header (env badge + user
/// pill) · sectioned tile grid on the left · live event-log panel on the right.
/// Every VLPlayEvents callback is appended to the log — this is the harness the
/// SDK phases are verified through. IMGUI so it needs no canvas/prefab wiring.
/// </summary>
public sealed class VLPlayDemoConsole : MonoBehaviour
{
    private readonly List<string> _log = new List<string>();
    private Vector2 _logScroll;
    private Vector2 _tileScroll;

    // U2-5: mirrored from the SDK, not from what we asked for — refreshed after every
    // Set so the label proves the round-trip. Cached because OnGUI runs several times
    // per frame and each getter is a real JNI / P-Invoke hop.
    private VLPlayLanguage _language;
    private VLPlayOrientation _orientation;

    // U3-5: Buy targets the first catalog package once Catalog has run; before that it
    // falls back to the stg demo game's known product so Buy works standalone too.
    private string _buyProductId = "com.vlplay.demo.pack_001";
    private bool _buyFromCatalog;

    // U6-1: env badge reads the same settings asset Init() consumes.
    private bool _useStaging = true;

    // Lazily-built styles — GUIStyle needs a live skin, so no field initializers.
    private GUIStyle _headerStyle, _badgeStyle, _pillStyle, _sectionStyle, _logStyle, _tileStyle;
    private Texture2D _badgeStgTex, _badgeProdTex, _pillTex;
    private bool _stylesReady;

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
        var settings = VLPlaySettings.Load();
        if (settings != null) _useStaging = settings.useStaging;
        VLPlaySDK.Init(settings);
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
        _logScroll.y = float.MaxValue;
    }

    // ---- U6-1: chrome ----

    private static Texture2D SolidTex(Color c)
    {
        var t = new Texture2D(1, 1);
        t.SetPixel(0, 0, c);
        t.Apply();
        return t;
    }

    private void EnsureStyles()
    {
        if (_stylesReady) return;
        _stylesReady = true;

        _badgeStgTex  = SolidTex(new Color(0.85f, 0.55f, 0.10f, 0.95f)); // amber
        _badgeProdTex = SolidTex(new Color(0.15f, 0.60f, 0.30f, 0.95f)); // green
        _pillTex      = SolidTex(new Color(0f, 0f, 0f, 0.45f));

        _headerStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 22, fontStyle = FontStyle.Bold,
            normal = { textColor = Color.white }
        };
        _badgeStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 14, fontStyle = FontStyle.Bold, alignment = TextAnchor.MiddleCenter,
            normal = { textColor = Color.white, background = _badgeStgTex },
            padding = new RectOffset(10, 10, 4, 4)
        };
        _pillStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 15, alignment = TextAnchor.MiddleRight,
            normal = { textColor = Color.white, background = _pillTex },
            padding = new RectOffset(12, 12, 4, 4)
        };
        _sectionStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 13, fontStyle = FontStyle.Bold,
            normal = { textColor = new Color(1f, 1f, 1f, 0.65f) }
        };
        _logStyle = new GUIStyle(GUI.skin.label) { fontSize = 14, wordWrap = true };
        _tileStyle = new GUIStyle(GUI.skin.button) { fontSize = 17 };
    }

    private void Section(string title)
    {
        GUILayout.Space(6);
        GUILayout.Label(title, _sectionStyle);
    }

    private bool Tile(string label)
    {
        return GUILayout.Button(label, _tileStyle, GUILayout.Height(52));
    }

    private void DrawHeader()
    {
        GUILayout.BeginHorizontal();
        GUILayout.Label("VLPlay SDK Demo", _headerStyle, GUILayout.ExpandWidth(false));
        GUILayout.Space(10);

        _badgeStyle.normal.background = _useStaging ? _badgeStgTex : _badgeProdTex;
        GUILayout.Label(_useStaging ? "STAGING" : "PRODUCTION", _badgeStyle, GUILayout.ExpandWidth(false));

        GUILayout.FlexibleSpace();

        // User pill — proves CurrentUser carries the real signed-in account.
        var user = VLPlaySDK.CurrentUser;
        string pill = user == null
            ? "○ chưa đăng nhập"
            : "● " + user.username + (user.isGuest ? " [GUEST]" : "") + "  ·  id " + Dash(user.accountId);
        GUILayout.Label(pill, _pillStyle, GUILayout.ExpandWidth(false));
        GUILayout.EndHorizontal();

        var u2 = VLPlaySDK.CurrentUser;
        if (u2 != null && (!string.IsNullOrEmpty(u2.email) || !string.IsNullOrEmpty(u2.phone)))
        {
            GUILayout.BeginHorizontal();
            GUILayout.FlexibleSpace();
            GUILayout.Label("email " + Dash(u2.email) + "    phone " + Dash(u2.phone), _pillStyle,
                GUILayout.ExpandWidth(false));
            GUILayout.EndHorizontal();
        }
    }

    private void DrawTiles()
    {
        _tileScroll = GUILayout.BeginScrollView(_tileScroll);

        Section("TÀI KHOẢN");
        GUILayout.BeginHorizontal();
        if (Tile("Login")) VLPlaySDK.SignIn();
        if (Tile("Logout")) VLPlaySDK.SignOut();
        if (Tile("Log Event")) VLPlaySDK.LogEvent("demo_button", "{\"src\":\"console\"}");
        GUILayout.EndHorizontal();

        Section("CHỐNG NGHIỆN (NĐ147)");
        // Debug rows — a real kick needs a minor's account + a tightened CMS cap, and a
        // session expiry needs the refresh token to actually fail. Both go through the
        // SDK's own debug entry points, so the real native chain runs.
        GUILayout.BeginHorizontal();
        if (Tile("AA warn")) VLPlaySDK.DebugForceAntiAddiction(false);
        if (Tile("AA kick")) VLPlaySDK.DebugForceAntiAddiction(true);
        if (Tile("AA status")) LogAaStatus();
        GUILayout.EndHorizontal();

        Section("SHOP (IAP V3)");
        // Catalog retargets Buy to the first CMS package; Buy needs a signed-in
        // account + a store-configured product (License Tester / sandbox).
        GUILayout.BeginHorizontal();
        if (Tile("Catalog")) LogCatalog();
        if (Tile("Buy" + (_buyFromCatalog ? "" : "*"))) Buy();
        if (Tile("History")) LogHistory();
        if (Tile("Restore")) Restore();
        GUILayout.EndHorizontal();

        Section("CÀI ĐẶT / CONFIG");
        GUILayout.BeginHorizontal();
        if (Tile("Lang: " + _language)) CycleLanguage();
        if (Tile("Orient: " + _orientation)) CycleOrientation();
        if (Tile("Config")) LogConfig();
        GUILayout.EndHorizontal();

#if UNITY_ANDROID && !UNITY_EDITOR
        Section("DEBUG (Android)");
        GUILayout.BeginHorizontal();
        // Android-only: iOS exposes no session-expire hook.
        if (Tile("Session expire")) ForceSessionExpireAndroid();
        GUILayout.EndHorizontal();
#endif

        GUILayout.EndScrollView();
    }

    private void DrawLogPanel()
    {
        GUILayout.BeginHorizontal();
        GUILayout.Label("Event log (" + _log.Count + ")", _sectionStyle);
        GUILayout.FlexibleSpace();
        if (GUILayout.Button("Clear", GUILayout.Width(80), GUILayout.Height(28))) _log.Clear();
        GUILayout.EndHorizontal();

        _logScroll = GUILayout.BeginScrollView(_logScroll, GUI.skin.box);
        for (int i = 0; i < _log.Count; i++) GUILayout.Label(_log[i], _logStyle);
        GUILayout.EndScrollView();
    }

    private void OnGUI()
    {
        EnsureStyles();
        const float pad = 12f;
        GUILayout.BeginArea(new Rect(pad, pad, Screen.width - pad * 2, Screen.height - pad * 2));

        DrawHeader();
        GUILayout.Space(8);

        bool twoColumn = Screen.width > Screen.height; // landscape → tiles | log side by side
        if (twoColumn)
        {
            GUILayout.BeginHorizontal();
            GUILayout.BeginVertical(GUILayout.Width((Screen.width - pad * 2) * 0.52f));
            DrawTiles();
            GUILayout.EndVertical();
            GUILayout.Space(10);
            GUILayout.BeginVertical();
            DrawLogPanel();
            GUILayout.EndVertical();
            GUILayout.EndHorizontal();
        }
        else
        {
            DrawTiles();
            GUILayout.Space(8);
            DrawLogPanel();
        }

        GUILayout.EndArea();
    }
}
