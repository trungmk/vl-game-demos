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
    private GUIStyle _headerStyle, _badgeStyle, _pillStyle, _sectionStyle, _logStyle, _tileStyle, _panelStyle, _logPanelStyle, _clearStyle;
    private Texture2D _badgeStgTex, _badgeProdTex, _pillTex, _panelTex, _logPanelTex, _tileNormalTex, _tileHoverTex, _tileActiveTex;
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
        VLPlayEvents.OnAdLoaded += HandleAdLoaded;
        VLPlayEvents.OnAdLoadFailed += HandleAdLoadFailed;
        VLPlayEvents.OnAdShown += HandleAdShown;
        VLPlayEvents.OnAdDismissed += HandleAdDismissed;
        VLPlayEvents.OnAdShowFailed += HandleAdShowFailed;
        VLPlayEvents.OnUserRewarded += HandleUserRewarded;
        VLPlayEvents.OnRewardConfirmed += HandleRewardConfirmed;
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
        VLPlayEvents.OnAdLoaded -= HandleAdLoaded;
        VLPlayEvents.OnAdLoadFailed -= HandleAdLoadFailed;
        VLPlayEvents.OnAdShown -= HandleAdShown;
        VLPlayEvents.OnAdDismissed -= HandleAdDismissed;
        VLPlayEvents.OnAdShowFailed -= HandleAdShowFailed;
        VLPlayEvents.OnUserRewarded -= HandleUserRewarded;
        VLPlayEvents.OnRewardConfirmed -= HandleRewardConfirmed;
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

    private void HandleSignIn(VLPlayUser u)
    {
        Log("OnSignIn → " + u);
        // The native SDK does NOT wire the ads user id itself — the integrator does.
        // Needed before any rewarded show (SSV user attribution).
        if (u != null && !string.IsNullOrEmpty(u.accountId))
        {
            VLPlayAds.SetUserId(u.accountId);
            Log("VLPlayAds.SetUserId(" + u.accountId + ")");
        }
    }
    private void HandleSignOut() => Log("OnSignOut");
    private void HandleSessionExpired() => Log("OnSessionExpired");
    private void HandleAaWarn(string m) => Log("OnAntiAddictionWarn → " + m);
    private void HandleAaKick(VLPlayAntiAddictionKick k) => Log("OnAntiAddictionKick → " + k);
    private void HandlePurchaseCompleted() => Log("OnPurchaseCompleted (broadcast — also fires for FAB/webpay buys)");
    private void HandleError(VLPlayError e) => Log("OnError → " + e);
    private void HandleAdLoaded(string p) => Log("OnAdLoaded → " + p);
    private void HandleAdLoadFailed(VLPlayAdError e) => Log("OnAdLoadFailed → " + e);
    private void HandleAdShown(string p) => Log("OnAdShown → " + p);
    private void HandleAdDismissed(string p) => Log("OnAdDismissed → " + p);
    private void HandleAdShowFailed(VLPlayAdError e) => Log("OnAdShowFailed → " + e);
    private void HandleUserRewarded(VLPlayAdReward r) => Log("OnUserRewarded → " + r + "  (ADVISORY — no grant)");
    private void HandleRewardConfirmed(VLPlayAdReward r) => Log("★ OnRewardConfirmed → " + r + "  — the ONLY place a game grants");

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

    // ---- U5-4: Account ops + FAB ----

    private bool _fabShown;

    private void LogAccountResult(string op, VLPlayAccountResult r) => Log(op + " → " + r);

    private void BindGoogle()
    {
        Log("BindSocial(google)… (OAuth runs inside the SDK — guest only)");
        VLPlayAccount.BindSocial(VLPlayAccount.ProviderGoogle, r => LogAccountResult("BindSocial", r));
    }

    private void UpgradeGuest()
    {
        // Throwaway creds for the stg test account; no email → no OTP dialog.
        string username = "ug" + ((long)(Time.realtimeSinceStartup * 1000) % 89999999L + 10000000L);
        Log("UpgradeGuest(" + username + ")… (guest only; email omitted → no OTP)");
        VLPlayAccount.UpgradeGuest(username, "12345678", "", r => LogAccountResult("UpgradeGuest", r));
    }

    private void Giftcode()
    {
        Log("RedeemGiftcode(TESTGIFT001)…");
        VLPlayAccount.RedeemGiftcode("TESTGIFT001", r =>
            LogAccountResult("RedeemGiftcode", r)); // 10001 GIFTCODE_USED is a business reply, not a bug
    }

    private void Deactivate()
    {
        Log("⚠ DeactivateAccount… (destructive — OnSignOut follows on success)");
        VLPlayAccount.DeactivateAccount(r => LogAccountResult("DeactivateAccount", r));
    }

    private void ToggleFab()
    {
        if (_fabShown) { Log("DismissFloatingBall"); VLPlaySDK.DismissFloatingBall(); }
        else
        {
            // Local override so the ball shows even when the CMS has no fab block.
            Log("ConfigureFAB({enabled:true}) + ShowFloatingBall + SetFABContext");
            VLPlaySDK.ConfigureFAB("{\"enabled\":true}");
            VLPlaySDK.SetFABContext("{\"serverId\":\"S1\",\"roleName\":\"demo\"}");
            VLPlaySDK.ShowFloatingBall();
        }
        _fabShown = !_fabShown;
    }

    // ---- U4-4: Ads (IAA) ----

    // Same local test config as the native sample's "Nạp AdMob TEST" card: Google's
    // PUBLIC test units (fill everywhere incl. emulator, no account), ssv:false —
    // a locally-injected ssv:true placement could never confirm (no BE ticket).
    // The vendor dependency must be in the app: VLPlay ▸ Sync Ads Dependencies with
    // adsProvider=admob (+ admobAppId, or MobileAds crashes at launch).
    private const string AdmobTestConfigJson =
        "{" +
            "\"enabled\":true," +
            "\"provider\":\"admob\"," +
            "\"testMode\":true," +
            "\"placements\":[" +
                "{\"id\":\"rewarded_test\",\"format\":\"rewarded\"," +
                    "\"unitId\":\"ca-app-pub-3940256099942544/5224354917\"," +
                    "\"preload\":true,\"ssv\":false,\"rewardCurrency\":\"gold\",\"rewardAmount\":10}," +
                "{\"id\":\"interstitial_test\",\"format\":\"interstitial\"," +
                    "\"unitId\":\"ca-app-pub-3940256099942544/1033173712\",\"preload\":true}" +
            "]" +
        "}";

    private const string AdRewardedId = "rewarded_test";
    private const string AdInterstitialId = "interstitial_test";

    // Polled at 2Hz, not per OnGUI pass — every getter is a real JNI / P-Invoke hop.
    private bool _adsEnabled, _adsInitialized, _adsRwReady, _adsIntReady;
    private float _adsNextPoll;

    private void PollAdsStatus()
    {
        if (Time.time < _adsNextPoll) return;
        _adsNextPoll = Time.time + 0.5f;
        _adsEnabled = VLPlayAds.IsEnabled;
        _adsInitialized = VLPlayAds.IsInitialized;
        _adsRwReady = VLPlayAds.IsReady(AdRewardedId);
        _adsIntReady = VLPlayAds.IsReady(AdInterstitialId);
    }

    private void ApplyAdsTestConfig()
    {
        Log("DebugApplyLocalConfig (AdMob TEST units — QA only, strip before shipping)…");
        VLPlayAds.DebugApplyLocalConfig(AdmobTestConfigJson);
        MainThreadLogAdsStatus();
    }

    private void MainThreadLogAdsStatus()
    {
        Log("Ads status: enabled=" + VLPlayAds.IsEnabled + " initialized=" + VLPlayAds.IsInitialized);
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

    // Anti-aliased rounded-rect for 9-slicing — gives tiles + panels real rounded
    // corners without shipping any art. Pair with GUIStyle.border = radius.
    private static Texture2D RoundedTex(Color fill, int radius = 18)
    {
        int size = radius * 2 + 2;
        var t = new Texture2D(size, size, TextureFormat.ARGB32, false) { wrapMode = TextureWrapMode.Clamp };
        var px = new Color[size * size];
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
            {
                float ix = Mathf.Clamp(x, radius, size - 1 - radius);
                float iy = Mathf.Clamp(y, radius, size - 1 - radius);
                float d = Mathf.Sqrt((x - ix) * (x - ix) + (y - iy) * (y - iy));
                var c = fill;
                c.a *= Mathf.Clamp01(radius + 0.5f - d); // solid inside, 1px AA on the rim
                px[y * size + x] = c;
            }
        t.SetPixels(px);
        t.Apply();
        return t;
    }

    private void EnsureStyles()
    {
        if (_stylesReady) return;
        _stylesReady = true;

        _badgeStgTex   = RoundedTex(new Color32(0xE0, 0x8A, 0x14, 0xFF), 12); // amber
        _badgeProdTex  = RoundedTex(new Color32(0x25, 0x9E, 0x4F, 0xFF), 12); // green
        _pillTex       = RoundedTex(new Color32(0x0A, 0x0F, 0x18, 0xC8), 16);
        _panelTex      = RoundedTex(new Color32(0x18, 0x20, 0x2E, 0xF7), 20); // card, near-opaque so text reads crisp
        _logPanelTex   = RoundedTex(new Color32(0x0C, 0x11, 0x1A, 0xFB), 20);
        _tileNormalTex = RoundedTex(new Color32(0x2A, 0x35, 0x49, 0xFF), 16); // slate
        _tileHoverTex  = RoundedTex(new Color32(0x37, 0x46, 0x62, 0xFF), 16);
        _tileActiveTex = RoundedTex(new Color32(0xE8, 0x19, 0x2C, 0xFF), 16); // brand red on press

        _headerStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 30, fontStyle = FontStyle.Bold,
            normal = { textColor = Color.white }
        };
        _badgeStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 16, fontStyle = FontStyle.Bold, alignment = TextAnchor.MiddleCenter,
            normal = { textColor = Color.white, background = _badgeStgTex },
            border = new RectOffset(12, 12, 12, 12),
            padding = new RectOffset(14, 14, 6, 6)
        };
        _pillStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 16, alignment = TextAnchor.MiddleRight,
            normal = { textColor = Color.white, background = _pillTex },
            border = new RectOffset(16, 16, 16, 16),
            padding = new RectOffset(16, 16, 8, 8)
        };
        _sectionStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 17, fontStyle = FontStyle.Bold,
            normal = { textColor = new Color32(0xFF, 0x7A, 0x7A, 0xFF) }, // brand-red accent
            padding = new RectOffset(2, 2, 4, 2)
        };
        _logStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 17, wordWrap = true,
            normal = { textColor = new Color(0.95f, 0.97f, 1f) }
        };
        _tileStyle = new GUIStyle(GUI.skin.button)
        {
            fontSize = 22, fontStyle = FontStyle.Bold, alignment = TextAnchor.MiddleCenter,
            border = new RectOffset(16, 16, 16, 16),
            margin = new RectOffset(5, 5, 5, 5),
            padding = new RectOffset(6, 6, 6, 6),
            normal   = { background = _tileNormalTex, textColor = Color.white },
            hover    = { background = _tileHoverTex,  textColor = Color.white },
            active   = { background = _tileActiveTex, textColor = Color.white },
            onNormal = { background = _tileNormalTex, textColor = Color.white }
        };
        _clearStyle = new GUIStyle(_tileStyle) { fontSize = 16 };
        _panelStyle = new GUIStyle(GUI.skin.box)
        {
            normal = { background = _panelTex },
            border = new RectOffset(20, 20, 20, 20),
            padding = new RectOffset(14, 14, 12, 14)
        };
        _logPanelStyle = new GUIStyle(_panelStyle) { normal = { background = _logPanelTex } };
    }

    private void Section(string title)
    {
        GUILayout.Space(10);
        GUILayout.Label("▎ " + title, _sectionStyle);
    }

    private bool Tile(string label)
    {
        return GUILayout.Button(label, _tileStyle, GUILayout.Height(64));
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
        GUILayout.BeginVertical(_panelStyle);
        _tileScroll = GUILayout.BeginScrollView(_tileScroll);

        Section("TÀI KHOẢN");
        GUILayout.BeginHorizontal();
        if (Tile("Login")) VLPlaySDK.SignIn();
        if (Tile("Logout")) VLPlaySDK.SignOut();
        if (Tile("Log Event")) VLPlaySDK.LogEvent("demo_button", "{\"src\":\"console\"}");
        GUILayout.EndHorizontal();
        // U5 account ops. Bind/Upgrade need a GUEST session; Deact is destructive.
        GUILayout.BeginHorizontal();
        if (Tile("Bind G")) BindGoogle();
        if (Tile("Upgrade")) UpgradeGuest();
        if (Tile("Identity")) { Log("ShowIdentityVerification…"); VLPlayAccount.ShowIdentityVerification(); }
        if (Tile("Guest UI")) { Log("ShowGuestUpgrade…"); VLPlayAccount.ShowGuestUpgrade(); }
        GUILayout.EndHorizontal();
        GUILayout.BeginHorizontal();
        if (Tile("Giftcode")) Giftcode();
        if (Tile("FAB " + (_fabShown ? "off" : "on"))) ToggleFab();
        if (Tile("Deact ⚠")) Deactivate();
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

        PollAdsStatus();
        Section("QUẢNG CÁO (IAA)" +
                (_adsEnabled ? "  ·  " + (_adsInitialized ? "READY" : "init…") : "  ·  off"));
        // "Ads cfg" injects the AdMob public-test config (parity native demo's
        // "Nạp AdMob TEST") — needs the vendor dep in the build. Grant modeling:
        // the log marks OnUserRewarded advisory, OnRewardConfirmed as the grant.
        GUILayout.BeginHorizontal();
        if (Tile("Ads cfg")) ApplyAdsTestConfig();
        if (Tile("Preload all")) { Log("PreloadAll…"); VLPlayAds.PreloadAll(); }
        if (Tile("RW" + (_adsRwReady ? " ✓" : ""))) VLPlayAds.ShowRewarded(AdRewardedId);
        if (Tile("INT" + (_adsIntReady ? " ✓" : ""))) VLPlayAds.ShowInterstitial(AdInterstitialId);
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
        GUILayout.EndVertical();
    }

    private void DrawLogPanel()
    {
        GUILayout.BeginVertical(_logPanelStyle);
        GUILayout.BeginHorizontal();
        GUILayout.Label("▎ EVENT LOG (" + _log.Count + ")", _sectionStyle);
        GUILayout.FlexibleSpace();
        if (GUILayout.Button("Clear", _clearStyle, GUILayout.Width(96), GUILayout.Height(40))) _log.Clear();
        GUILayout.EndHorizontal();
        GUILayout.Space(4);

        _logScroll = GUILayout.BeginScrollView(_logScroll);
        for (int i = 0; i < _log.Count; i++) GUILayout.Label(_log[i], _logStyle);
        GUILayout.EndScrollView();
        GUILayout.EndVertical();
    }

    private void OnGUI()
    {
        EnsureStyles();
        const float pad = 14f;
        // Respect the notch / rounded corners so the header isn't clipped on device.
        Rect sa = Screen.safeArea;
        float top = Screen.height - (sa.y + sa.height);
        var area = new Rect(sa.x + pad, top + pad, sa.width - pad * 2, sa.height - pad * 2);
        GUILayout.BeginArea(area);

        DrawHeader();
        GUILayout.Space(10);

        bool twoColumn = Screen.width > Screen.height; // landscape → tiles | log side by side
        if (twoColumn)
        {
            GUILayout.BeginHorizontal();
            GUILayout.BeginVertical(GUILayout.Width(area.width * 0.52f));
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
