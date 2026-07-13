package sdk.vlplay.vn.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Locale;

import sdk.vlplay.vn.antiaddiction.AntiAddictionManager;
import sdk.vlplay.vn.antiaddiction.AntiAddictionStatus;
import sdk.vlplay.vn.common.Common;
import sdk.vlplay.vn.common.VLPlayString;
import sdk.vlplay.vn.config.DemoMode;
import sdk.vlplay.vn.config.SdkConfig;
import sdk.vlplay.vn.config.SdkConfigManager;
import sdk.vlplay.vn.connection.VLPlayHttpConnection;
import sdk.vlplay.vn.model.VLPlayPayParams;
import sdk.vlplay.vn.tracking.AppsFlyerHelper;
import sdk.vlplay.vn.tracking.VLPlaySDKManager;
import sdk.vlplay.vn.util.Util;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Activity mActivity;
    private LinearLayout layoutPreLogin;
    private LinearLayout layoutGameScreen;
    private TextView tvStatus;
    private TextView tvUserName;
    private TextView tvUserId;
    private TextView tvLoginType;
    private TextView tvEventLog;
    private EditText edtGiftCode;
    private TextView tvPlayTime;
    private TextView tvAntiAddiction;
    private Button btnToggleDemo;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int eventCount = 0;
    private long sessionStartTime = 0;
    private final Runnable playTimeUpdater = new Runnable() {
        @Override
        public void run() {
            updatePlayTimeDisplay();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        VLPlaySDKManager.initStartSDK(mActivity);
        initController();
    }

    private void initController() {
        Common.showLog("key: " + printKeyHash(this));

        layoutPreLogin = findViewById(R.id.layoutPreLogin);
        layoutGameScreen = findViewById(R.id.layoutGameScreen);
        tvStatus = findViewById(R.id.tvStatus);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserId = findViewById(R.id.tvUserId);
        tvLoginType = findViewById(R.id.tvLoginType);
        tvEventLog = findViewById(R.id.tvEventLog);
        tvPlayTime = findViewById(R.id.tvPlayTime);
        tvAntiAddiction = findViewById(R.id.tvAntiAddiction);
        edtGiftCode = findViewById(R.id.edtGiftCode);
        btnToggleDemo = findViewById(R.id.btn_toggle_demo);

        findViewById(R.id.btn_Start).setOnClickListener(this);
        findViewById(R.id.btn_account).setOnClickListener(this);
        findViewById(R.id.btn_Payment).setOnClickListener(this);
        findViewById(R.id.btn_Logout).setOnClickListener(this);
        findViewById(R.id.btn_buy_100).setOnClickListener(this);
        findViewById(R.id.btn_buy_500).setOnClickListener(this);
        findViewById(R.id.btn_buy_1000).setOnClickListener(this);
        findViewById(R.id.btn_redeem).setOnClickListener(this);
        btnToggleDemo.setOnClickListener(this);

        findViewById(R.id.btn_fire_all).setOnClickListener(this);
        findViewById(R.id.btn_evt_lifecycle).setOnClickListener(this);
        findViewById(R.id.btn_evt_login).setOnClickListener(this);
        findViewById(R.id.btn_evt_register).setOnClickListener(this);
        findViewById(R.id.btn_evt_third).setOnClickListener(this);
        findViewById(R.id.btn_evt_autologin).setOnClickListener(this);
        findViewById(R.id.btn_evt_payment).setOnClickListener(this);
        findViewById(R.id.btn_evt_logout).setOnClickListener(this);
        findViewById(R.id.btn_evt_delete).setOnClickListener(this);
        findViewById(R.id.btn_evt_bind).setOnClickListener(this);
        findViewById(R.id.btn_evt_game).setOnClickListener(this);
        findViewById(R.id.btn_evt_legacy).setOnClickListener(this);
        findViewById(R.id.btn_evt_retention).setOnClickListener(this);

        findViewById(R.id.btn_aa_warning).setOnClickListener(this);
        findViewById(R.id.btn_aa_kick).setOnClickListener(this);
        findViewById(R.id.btn_aa_session).setOnClickListener(this);
        findViewById(R.id.btn_aa_status).setOnClickListener(this);

        findViewById(R.id.btn_iap_catalog).setOnClickListener(this);
        findViewById(R.id.btn_iap_purchase).setOnClickListener(this);
        findViewById(R.id.btn_iap_history).setOnClickListener(this);

        updateDemoButton();
        updateSdkConfigDisplay();
    }

    private void updateSdkConfigDisplay() {
        String[] envNames = {"Staging", "Production", "Local"};
        int env = Util.LIB_STATUS;
        String envName = (env >= 0 && env < envNames.length) ? envNames[env] : "Unknown(" + env + ")";

        ((TextView) findViewById(R.id.tvConfigEnv)).setText("Env: " + envName);
        ((TextView) findViewById(R.id.tvConfigClientId)).setText("ClientID: " + VLPlayString.CLIENT_ID);
        ((TextView) findViewById(R.id.tvConfigApiKey)).setText("ApiKey: " + (VLPlayHttpConnection.getApiKey().isEmpty() ? "(from config)" : VLPlayHttpConnection.getApiKey()));

        String serverUrl = env == 2 ? "http://10.0.2.2:3089" : env == 0 ? "gw-s.vlplay.vn" : "gw.vlplay.vn";
        ((TextView) findViewById(R.id.tvConfigServer)).setText("Server: " + serverUrl);

        SdkConfig config = SdkConfigManager.getConfig();
        if (config != null) {
            ((TextView) findViewById(R.id.tvConfigFeatures)).setText(
                    "Features: identity=" + config.isIdentityVerification() + " anti-addiction=" + config.isAntiAddiction());

            String gId = config.getGoogleClientId();
            String google = gId.isEmpty() ? "none" : gId.substring(0, Math.min(25, gId.length())) + "...";
            String fb = config.getFacebookAppId().isEmpty() ? "none" : config.getFacebookAppId();
            String apple = config.getAppleBundleId().isEmpty() ? "none" : config.getAppleBundleId();
            ((TextView) findViewById(R.id.tvConfigSocial)).setText(
                    "Google: " + google + "\nFB: " + fb + " | Apple: " + apple);

            ((TextView) findViewById(R.id.tvConfigSupport)).setText(
                    "Support: " + config.getHotline() + " | " + config.getEmailSupport());

            ((TextView) findViewById(R.id.tvConfigMaintenance)).setText(
                    "Maintenance: " + config.isMaintenanceMode() + " | ForceUpdate: " + config.isForceUpdate()
                    + " | MinVer: " + config.getSdkMinVersion());

            String af = config.getAppsflyerDevKey().isEmpty() ? "none" : config.getAppsflyerDevKey().substring(0, Math.min(10, config.getAppsflyerDevKey().length())) + "...";
            ((TextView) findViewById(R.id.tvConfigTracking)).setText(
                    "AppsFlyer: " + af);
        } else {
            ((TextView) findViewById(R.id.tvConfigFeatures)).setText("Features: (loading...)");
            ((TextView) findViewById(R.id.tvConfigSocial)).setText("Social: (loading...)");
            ((TextView) findViewById(R.id.tvConfigSupport)).setText("Support: (loading...)");
            ((TextView) findViewById(R.id.tvConfigMaintenance)).setText("Maintenance: (loading...)");
            ((TextView) findViewById(R.id.tvConfigTracking)).setText("Tracking: (loading...)");
            new Handler(Looper.getMainLooper()).postDelayed(this::updateSdkConfigDisplay, 3000);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_Start) {
            onLoginClick();
        } else if (id == R.id.btn_account) {
            onAccountClick();
        } else if (id == R.id.btn_Payment) {
            onPaymentClick("tttq.100", "100 Kim Cuong", 22000);
        } else if (id == R.id.btn_Logout) {
            onLogoutClick();
        } else if (id == R.id.btn_buy_100) {
            onPaymentClick("tttq.100", "100 Kim Cuong", 22000);
        } else if (id == R.id.btn_buy_500) {
            onPaymentClick("tttq.500", "500 Kim Cuong", 99000);
        } else if (id == R.id.btn_buy_1000) {
            onPaymentClick("tttq.1000", "1000 Kim Cuong", 199000);
        } else if (id == R.id.btn_redeem) {
            onRedeemClick();
        } else if (id == R.id.btn_toggle_demo) {
            DemoMode.setEnabled(!DemoMode.isEnabled());
            updateDemoButton();
        }
        // Event testing
        else if (id == R.id.btn_fire_all) { fireAllEvents(); }
        else if (id == R.id.btn_evt_lifecycle) { fireLifecycleEvents(); }
        else if (id == R.id.btn_evt_login) { fireLoginEvents(); }
        else if (id == R.id.btn_evt_register) { fireRegisterEvents(); }
        else if (id == R.id.btn_evt_third) { fireThirdPartyLoginEvents(); }
        else if (id == R.id.btn_evt_autologin) { fireAutoLoginEvents(); }
        else if (id == R.id.btn_evt_payment) { firePaymentEvents(); }
        else if (id == R.id.btn_evt_logout) { fireLogoutEvents(); }
        else if (id == R.id.btn_evt_delete) { fireDeleteAccountEvents(); }
        else if (id == R.id.btn_evt_bind) { fireBindPhoneEvents(); }
        else if (id == R.id.btn_evt_game) { fireGameEvents(); }
        else if (id == R.id.btn_evt_legacy) { fireLegacyEvents(); }
        else if (id == R.id.btn_evt_retention) { fireRetentionEvents(); }
        // Anti-addiction
        else if (id == R.id.btn_aa_warning) { triggerAAWarning(); }
        else if (id == R.id.btn_aa_kick) { triggerAAKick(); }
        else if (id == R.id.btn_aa_session) { toggleAASession(); }
        else if (id == R.id.btn_aa_status) {
            AntiAddictionManager.checkStatusNow();
            updateStatus("Anti-addiction: status check triggered");
        }
        // V3 IAP (PARITY-A2)
        else if (id == R.id.btn_iap_catalog) { onIapCatalogClick(); }
        else if (id == R.id.btn_iap_purchase) { onIapPurchaseClick(); }
        else if (id == R.id.btn_iap_history) { onIapHistoryClick(); }
    }

    private void onIapCatalogClick() {
        updateStatus("Fetching IAP catalog...");
        VLPlaySDKManager.getProductCatalog(new VLPlaySDKManager.ProductCatalogListener() {
            @Override
            public void onSuccess(@androidx.annotation.NonNull java.util.List<sdk.vlplay.vn.payment.StorePackage> packages) {
                StringBuilder sb = new StringBuilder("Catalog: ").append(packages.size()).append(" packages\n");
                for (sdk.vlplay.vn.payment.StorePackage p : packages) {
                    sb.append("• ").append(p.getStoreProductId()).append(" — ")
                            .append(p.getName()).append(" (").append(p.getPriceVND())
                            .append(" ").append(p.getCurrency()).append(")\n");
                }
                Toast.makeText(MainActivity.this, "Catalog: " + packages.size() + " items", Toast.LENGTH_SHORT).show();
                updateStatus(sb.toString());
            }

            @Override
            public void onFailure(String message, int errorCode) {
                Toast.makeText(MainActivity.this, "Catalog fail: " + message, Toast.LENGTH_LONG).show();
                updateStatus("IAP catalog fail [" + errorCode + "]: " + message);
            }
        });
    }

    private void onIapPurchaseClick() {
        updateStatus("Launching V3 purchase pack_001...");
        VLPlaySDKManager.purchasePackage("vn.vlplay.demo.pack_001", "Pack 001", 22000,
                this, new VLPlaySDKManager.PurchaseListenerV3() {
                    @Override
                    public void onSuccess(@androidx.annotation.NonNull String transactionId,
                                          @androidx.annotation.NonNull String productId,
                                          boolean deliveryPending) {
                        String status = deliveryPending ? " (delivery pending)" : "";
                        Toast.makeText(MainActivity.this, "Purchase OK: " + transactionId + status,
                                Toast.LENGTH_LONG).show();
                        updateStatus("V3 purchase success: txn=" + transactionId
                                + " product=" + productId + status);
                    }

                    @Override
                    public void onFailure(String message, int errorCode) {
                        Toast.makeText(MainActivity.this, "Purchase fail: " + message,
                                Toast.LENGTH_LONG).show();
                        updateStatus("V3 purchase fail [" + errorCode + "]: " + message);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, "Purchase canceled", Toast.LENGTH_SHORT).show();
                        updateStatus("V3 purchase canceled by user");
                    }
                });
    }

    private void onIapHistoryClick() {
        updateStatus("Fetching purchase history...");
        VLPlaySDKManager.listSuccessfulTransactions(1, 20, new VLPlaySDKManager.PurchaseHistoryListener() {
            @Override
            public void onSuccess(@androidx.annotation.NonNull java.util.List<sdk.vlplay.vn.payment.TransactionItem> items,
                                  int total, int page, int limit) {
                StringBuilder sb = new StringBuilder("History: ").append(items.size())
                        .append("/").append(total).append(" (page=").append(page)
                        .append(" limit=").append(limit).append(")\n");
                for (sdk.vlplay.vn.payment.TransactionItem t : items) {
                    sb.append("• ").append(t.getTransactionId()).append(" — ")
                            .append(t.getProductId()).append(" (").append(t.getAmount())
                            .append(" ").append(t.getCurrency()).append(")\n");
                }
                Toast.makeText(MainActivity.this, "History: " + items.size() + " transactions",
                        Toast.LENGTH_SHORT).show();
                updateStatus(sb.toString());
            }

            @Override
            public void onFailure(String message, int errorCode) {
                Toast.makeText(MainActivity.this, "History fail: " + message, Toast.LENGTH_LONG).show();
                updateStatus("Purchase history fail [" + errorCode + "]: " + message);
            }
        });
    }

    private void onLoginClick() {
        VLPlaySDKManager.setUserSignOutListener(() -> {
            Common.showLog("Signed out (global listener)");
            showLoginScreen();
        });
        VLPlaySDKManager.setUserSignInListener(new VLPlaySDKManager.UserSignInListener() {
            @Override
            public void onSignInSuccess() {
                Common.showLog("Login success: " + VLPlaySDKManager.userModel.getAccountName());
                VLPlaySDKManager.updateGameInfo("58114", "123", "Player1");
                showGameScreen();
            }

            @Override
            public void onSignInFail() {
                updateStatus("Dang nhap that bai");
            }
        });
        VLPlaySDKManager.signIn(this);
    }

    private void onAccountClick() {
        // SDK scope cleanup (P3-01): goToAccountInfo is no longer part of the public
        // game-integrator surface because edit-profile belongs to the vlplay.vn app,
        // not partner games. Read-only profile state lives on
        // VLPlaySDKManager.userModel — render it inline rather than launching an
        // SDK-owned editor.
        String name = VLPlaySDKManager.userModel != null
                ? VLPlaySDKManager.userModel.getAccountName()
                : "(none)";
        Toast.makeText(this, "Account: " + name, Toast.LENGTH_SHORT).show();
    }

    private void onPaymentClick(String productId, String productName, double amount) {
        VLPlaySDKManager.setUserPurchaseListener(new VLPlaySDKManager.UserPurchaseListener() {
            @Override
            public void onPurchaseSuccess() {
                updateStatus("Nap tien thanh cong: " + productName);
                Toast.makeText(mActivity, "Nap thanh cong!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPurchaseFail() {
                updateStatus("Nap tien that bai");
            }

            @Override
            public void onReLogin() {
                showLoginScreen();
                VLPlaySDKManager.signIn(mActivity);
            }
        });

        VLPlayPayParams params = new VLPlayPayParams();
        params.setProductId(productId);
        params.setProductName(productName);
        params.setAmount(amount);
        params.setCurrency("VND");
        params.setRoleId("174100001");
        params.setRoleName("Player1");
        params.setServerGameId("1");
        params.setRechargeType("IN_GAME");
        params.setExtraData("demo_extra");
        VLPlaySDKManager.openPay(this, params);
    }

    private void onLogoutClick() {
        VLPlaySDKManager.signOut(this, () -> {
            showLoginScreen();
        });
    }

    private void onRedeemClick() {
        String code = edtGiftCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui long nhap ma qua tang", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Redeem: " + code + " (open Account > Gift Code)", Toast.LENGTH_LONG).show();
        edtGiftCode.setText("");
    }

    private void showGameScreen() {
        layoutPreLogin.setVisibility(View.GONE);
        layoutGameScreen.setVisibility(View.VISIBLE);

        String name = VLPlaySDKManager.userModel.getAccountName();
        String id = VLPlaySDKManager.userModel.getAccountId();
        boolean isGuest = VLPlaySDKManager.userModel.isGuest();

        tvUserName.setText("User: " + (name != null ? name : "---"));
        tvUserId.setText("ID: " + (id != null ? id : "---"));
        tvLoginType.setText("Type: " + (isGuest ? "Guest" : "Account"));
        updateStatus("Dang choi game - " + (name != null ? name : ""));

        sessionStartTime = System.currentTimeMillis();
        handler.removeCallbacks(playTimeUpdater);
        handler.post(playTimeUpdater);

        SdkConfig config = SdkConfigManager.getConfig();
        if (config != null) {
            tvAntiAddiction.setText("AA: " + (config.isAntiAddiction() ? "ON" : "OFF")
                    + " | Identity: " + (config.isIdentityVerification() ? "ON" : "OFF")
                    + " | Guest: " + (config.isGuestLogin() ? "ON" : "OFF")
                    + " | OTP: " + (config.isOtpRequired() ? "ON" : "OFF")
                    + " | AF: " + (config.isAppsFlyerTracking() ? "ON" : "OFF"));
        }
    }

    private void showLoginScreen() {
        layoutPreLogin.setVisibility(View.VISIBLE);
        layoutGameScreen.setVisibility(View.GONE);
        tvUserName.setText("User: ---");
        tvUserId.setText("ID: ---");
        tvLoginType.setText("Type: ---");
        updateStatus("Vui long dang nhap");
        handler.removeCallbacks(playTimeUpdater);
        sessionStartTime = 0;
    }

    private void updatePlayTimeDisplay() {
        if (sessionStartTime == 0) return;
        long elapsed = (System.currentTimeMillis() - sessionStartTime) / 1000;
        long min = elapsed / 60;
        long sec = elapsed % 60;
        String playStr = String.format(Locale.US, "Play: %d:%02d", min, sec);

        AntiAddictionStatus status = AntiAddictionManager.getLastStatus();
        if (status != null) {
            int remaining = status.getRemainingMinutesToday();
            String remStr = remaining < 0 ? "unlimited" : remaining + " min";
            playStr += " | Remaining: " + remStr;
            if (status.isRestricted()) playStr += " [RESTRICTED]";
            if (status.isCurfew()) playStr += " [CURFEW]";
        } else {
            playStr += " | AA: waiting...";
        }
        tvPlayTime.setText(playStr);
    }

    private void updateDemoButton() {
        btnToggleDemo.setText("Demo Mode: " + (DemoMode.isEnabled() ? "ON" : "OFF"));
    }

    private void updateStatus(String status) {
        tvStatus.setText(status);
    }

    // ===== Event Testing =====

    private void logEvent(String msg) {
        eventCount++;
        tvEventLog.append(eventCount + ". " + msg + "\n");
    }

    private void fireAllEvents() {
        eventCount = 0;
        tvEventLog.setText("");
        logEvent("Firing all 125 events...");
        handler.postDelayed(this::fireLifecycleEvents, 0);
        handler.postDelayed(this::fireLoginEvents, 300);
        handler.postDelayed(this::fireRegisterEvents, 600);
        handler.postDelayed(this::fireThirdPartyLoginEvents, 900);
        handler.postDelayed(this::fireAutoLoginEvents, 1200);
        handler.postDelayed(this::firePaymentEvents, 1500);
        handler.postDelayed(this::fireLogoutEvents, 2000);
        handler.postDelayed(this::fireDeleteAccountEvents, 2300);
        handler.postDelayed(this::fireBindPhoneEvents, 2600);
        handler.postDelayed(this::fireGameEvents, 3000);
        handler.postDelayed(this::fireLegacyEvents, 5000);
        handler.postDelayed(this::fireRetentionEvents, 5300);
        handler.postDelayed(() -> logEvent("ALL EVENTS FIRED"), 5600);
    }

    private void fireLifecycleEvents() {
        logEvent("[Lifecycle] 5 events");
        AppsFlyerHelper.trackAppLaunch();
        AppsFlyerHelper.trackSdkStartInitialization();
        AppsFlyerHelper.trackSdkInitialized();
        AppsFlyerHelper.trackSdkFirstInitialized(this);
        AppsFlyerHelper.trackShowLoginSceneSuccess();
    }

    private void fireLoginEvents() {
        logEvent("[Login Funnel] 7 events");
        AppsFlyerHelper.trackAfOpenLogin();
        AppsFlyerHelper.trackLoginStart();
        AppsFlyerHelper.trackAfClickLogin();
        AppsFlyerHelper.trackLoginSubmit();
        AppsFlyerHelper.trackLoginSuccess("account", "test_100001");
        AppsFlyerHelper.trackLoginCompleted("account", "test_100001");
        AppsFlyerHelper.trackLoginSuccessFinal("account", "test_100001");
    }

    private void fireRegisterEvents() {
        logEvent("[Register Funnel] 6 events");
        AppsFlyerHelper.trackRegisterStart();
        AppsFlyerHelper.trackRegisterInputAccount();
        AppsFlyerHelper.trackRegisterSubmit();
        AppsFlyerHelper.trackRegisterSuccess("test_100002");
        AppsFlyerHelper.trackRegisterCompleted("test_100002");
        AppsFlyerHelper.trackAfCompleteRegistration("account", "test_100002");
    }

    private void fireThirdPartyLoginEvents() {
        logEvent("[Third-Party Login] 5 events");
        AppsFlyerHelper.trackThirdLoginClicked("google");
        AppsFlyerHelper.trackThirdLoginInvoked("google");
        AppsFlyerHelper.trackThirdLoginCallbackSuccess("google");
        AppsFlyerHelper.trackThirdLoginCompleted("google");
        AppsFlyerHelper.trackThirdLoginCallbackFail("facebook", "test_error");
    }

    private void fireAutoLoginEvents() {
        logEvent("[Auto Login] 3 events");
        AppsFlyerHelper.trackAutoLoginStart();
        AppsFlyerHelper.trackAutoLoginSuccess("test_100001");
        AppsFlyerHelper.trackAutoLoginFail("test_token_expired");
    }

    private void firePaymentEvents() {
        logEvent("[Payment Funnel] 12 events");
        AppsFlyerHelper.trackPurchaseInitiated("tttq.100");
        AppsFlyerHelper.trackProductSelected("tttq.100", 99000);
        AppsFlyerHelper.trackPaymentSdkInit("tttq.100", 99000);
        AppsFlyerHelper.trackOpenPaymentScreen();
        AppsFlyerHelper.trackPaymentSdkCallback("success");
        AppsFlyerHelper.trackPaymentCallbackSuccess("tttq.100", 99000, "VND", "ORD_001");
        AppsFlyerHelper.trackPaymentCallbackFail("test_fail");
        AppsFlyerHelper.trackPaymentCallbackCancel();
        AppsFlyerHelper.trackPaymentValidated("ORD_001");
        AppsFlyerHelper.trackPaymentValidatedSuccess("ORD_001", 99000, "VND");
        AppsFlyerHelper.trackPaymentValidatedFail("ORD_001", "test_verify_fail");
        AppsFlyerHelper.trackPaymentCompleted("ORD_001", 99000, "VND");
    }

    private void fireLogoutEvents() {
        logEvent("[Logout] 3 events");
        AppsFlyerHelper.trackLogoutClick();
        AppsFlyerHelper.trackLogoutSuccess();
        AppsFlyerHelper.trackLogoutFail("test_fail");
    }

    private void fireDeleteAccountEvents() {
        logEvent("[Delete Account] 5 events");
        AppsFlyerHelper.trackDeleteAccountEntryClick();
        AppsFlyerHelper.trackDeleteAccountConfirm();
        AppsFlyerHelper.trackDeleteAccountSuccess();
        AppsFlyerHelper.trackDeleteAccountFail("test_fail");
        AppsFlyerHelper.trackDeleteAccountCancel();
    }

    private void fireBindPhoneEvents() {
        logEvent("[Bind Phone] 10 events");
        AppsFlyerHelper.trackBindCheckRequired();
        AppsFlyerHelper.trackInputPhone();
        AppsFlyerHelper.trackRequestSmsCode();
        AppsFlyerHelper.trackRequestSmsCodeSuccess();
        AppsFlyerHelper.trackRequestSmsCodeFail("test_sms_fail");
        AppsFlyerHelper.trackBindCheckSubmit();
        AppsFlyerHelper.trackBindSuccess();
        AppsFlyerHelper.trackBindCompleted();
        AppsFlyerHelper.trackBindFail("test_bind_fail");
        AppsFlyerHelper.trackSkipBind();
    }

    private void fireGameEvents() {
        logEvent("[Game Events] 67 events");
        AppsFlyerHelper.trackEnterGameplay();
        AppsFlyerHelper.trackCharacterCreationStarted();
        AppsFlyerHelper.trackCharacterCreationFinished();
        AppsFlyerHelper.trackPlayTime(30);
        AppsFlyerHelper.trackShowStartGame();
        AppsFlyerHelper.trackUpgradeVersionSuccess("2.0.0");
        AppsFlyerHelper.trackResourceStarted();
        AppsFlyerHelper.trackResourceFinished();
        AppsFlyerHelper.trackExtractStarted();
        AppsFlyerHelper.trackExtractFinished();
        int[] levels = {20, 70, 100, 150, 165, 240, 300, 5, 50, 120, 200};
        for (int level : levels) AppsFlyerHelper.trackLevelUp(level);
        int[] vipLevels = {4, 5, 6, 8, 10, 3};
        for (int vip : vipLevels) AppsFlyerHelper.trackVipLevelUp(vip);
        AppsFlyerHelper.trackAfPurchase("tttq.100", 99000, "VND", "TXN_AF_001");
        AppsFlyerHelper.trackAfPaymentSuccess("tttq.100", 99000, "VND", "google");
        AppsFlyerHelper.trackAfCancelPurchase("tttq.100");
        AppsFlyerHelper.trackAfErrorPurchase("tttq.100", "test_error");
        AppsFlyerHelper.trackAfOpenPaymentScreen();
        AppsFlyerHelper.trackAfServerIapPurchase("TXN_001", 99000, "VND");
        AppsFlyerHelper.trackAfServerWebPurchase("TXN_002", 50000, "VND");
        AppsFlyerHelper.trackAfLogin("account", "test_100001");
        AppsFlyerHelper.trackAfLogout("test_100001");
        AppsFlyerHelper.trackAfRegistration("account", "test_100002");
        AppsFlyerHelper.trackAfCompleteRegistration("account", "test_100002");
        AppsFlyerHelper.trackAfOpenLogin();
        AppsFlyerHelper.trackAfClickLogin();
        AppsFlyerHelper.trackAfFirstLaunch(this);
        AppsFlyerHelper.trackAfAppLaunch();
        AppsFlyerHelper.trackErrorApi("/api/v1/test", "test_500_error");
        AppsFlyerHelper.trackLevelAchieved(100);
    }

    private void fireLegacyEvents() {
        logEvent("[Legacy Compat] 3 events");
        AppsFlyerHelper.trackFbLoginSuccess();
        AppsFlyerHelper.trackGgLoginSuccess();
        AppsFlyerHelper.trackYkLoginSuccess();
    }

    private void fireRetentionEvents() {
        logEvent("[Retention] 4 events");
        AppsFlyerHelper.checkRetention(this);
        AppsFlyerHelper.trackPurchase("tttq.100", 99000, "VND", "TXN_003");
        AppsFlyerHelper.trackPaymentSuccess("tttq.100", 99000, "VND", "google");
        AppsFlyerHelper.trackContentView("game_screen", "screen");
    }

    // ===== Anti-Addiction Testing =====

    private void triggerAAWarning() {
        try {
            String mockJson = DemoMode.getMockAAStatusResponse();
            org.json.JSONObject data = new org.json.JSONObject(mockJson).optJSONObject("data");
            AntiAddictionStatus status = AntiAddictionStatus.fromJson(data);
            if (status != null) VLPlaySDKManager.showAntiAddictionWarningPopup(status);
        } catch (Exception e) {
            Common.showLog("AA warning test error: " + e.getMessage());
        }
    }

    private void triggerAAKick() {
        try {
            String mockJson = DemoMode.getMockAAStatusRestricted();
            org.json.JSONObject data = new org.json.JSONObject(mockJson).optJSONObject("data");
            AntiAddictionStatus status = AntiAddictionStatus.fromJson(data);
            if (status != null) VLPlaySDKManager.showAntiAddictionKickPopup(status);
        } catch (Exception e) {
            Common.showLog("AA kick test error: " + e.getMessage());
        }
    }

    private void toggleAASession() {
        Button btn = findViewById(R.id.btn_aa_session);
        if (AntiAddictionManager.isSessionActive()) {
            AntiAddictionManager.stopSession();
            btn.setText("Session");
            updateStatus("Anti-addiction: session stopped");
        } else {
            AntiAddictionManager.startSession();
            btn.setText("Stop");
            updateStatus("Anti-addiction: session started");
        }
    }

    public static String printKeyHash(Activity context) {
        try {
            String packageName = context.getApplicationContext().getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String key = new String(Base64.encode(md.digest(), 0));
                Common.showLog("Key Hash=" + key);
                return key;
            }
        } catch (Exception e) {
            Common.showLog("printKeyHash error: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (VLPlaySDKManager.userModel != null && VLPlaySDKManager.userModel.getAccessToken() != null
                && !VLPlaySDKManager.userModel.getAccessToken().isEmpty()) {
            showGameScreen();
        }
    }
}
