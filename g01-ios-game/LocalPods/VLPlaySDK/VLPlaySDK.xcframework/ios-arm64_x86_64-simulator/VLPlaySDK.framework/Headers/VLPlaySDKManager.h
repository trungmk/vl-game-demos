//
//  VLPlaySDKManager.h
//  VLPlaySDK
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "Constants.h"

// Forward-declare VLPlayFABTab so this header doesn't have to pull the full
// FAB header through the framework module map. The actual enum + the rest
// of the FAB API live in VLPlayFAB.h — partner games that want to call
// +showHudWithTab: with a named case (`VLPlayFABTabAccount`) should
// `#import <VLPlaySDK/VLPlayFAB.h>` directly.
typedef NS_ENUM(NSInteger, VLPlayFABTab);
//#import <FBSDKShareKit/FBSDKSharing.h>
//#import <FBSDKShareKit/FBSDKShareDialog.h>
//#import <FBSDKShareKit/FBSDKShareLinkContent.h>
//#import <FBSDKShareKit/FBSDKSharePhotoContent.h>
//#import <FBSDKShareKit/FBSDKSharePhoto.h>
@class VIDUser;

NS_ASSUME_NONNULL_BEGIN

/// Provider identifiers for `-bindProvider:token:completion:`.
/// (Re-exported here so binary-xcframework integrators get them from the public umbrella.)
FOUNDATION_EXPORT NSString *const VLPlayBindProviderFacebook;
FOUNDATION_EXPORT NSString *const VLPlayBindProviderGoogle;
FOUNDATION_EXPORT NSString *const VLPlayBindProviderApple;

@protocol VLPlaySDKManagerDelegate <NSObject>

@required
/*!
 @brief method to tells the application that user has signed in.
 @param user current user, who has just signed in
 @note require!
 */
- (void)sdkManagerDidSignInWithUser:(VIDUser *)user;
/*!
 @brief method to tells the application that user has signed out.
 @note require!
 */
- (void)sdkManagerDidSignOut;
/*!
 @brief method to tells the application that user has purchased a payment package successfully.
 @note require!
 */
- (void)sdkManagerDidPurchaseSuccessfully;

@optional
/*!
 @brief The access token expired and token refresh also failed. Game should show login UI.
 @note optional — SDK will still clear cached session internally.
 */
- (void)sdkManagerDidSessionExpire;

/*!
 @brief Fired when a support ticket is successfully created from the FAB HUD
        Support tab. Use this hook to refresh game-side ticket counters or
        analytics. Fires on the main queue.
 @param ticketId BE-assigned ticket id (parity Web SDK `supportTicketCreated`).
 */
- (void)sdkManagerSupportTicketCreated:(NSString * _Nonnull)ticketId;

/*!
 @brief Fired after the FAB HUD Profile sub-tab successfully commits an
        email / phone bind or change. `user` is the updated VIDUser.
 */
- (void)sdkManagerProfileUpdated:(VIDUser * _Nonnull)user;

/*!
 @brief Fired after the user finishes identity verification through any
        SDK entry point (FAB Identity tab hand-off OR direct CCCD form).
 */
- (void)sdkManagerIdentityVerified;

/*!
 @brief Fired when the Decree-147 anti-addiction gate warns the player.
        The SDK still shows its own warning popup; this hook is additive, so the
        game can react too (e.g. pause). Fires once per session, alongside the popup.
 @param warningMessage server-provided text; nil when the server sends none.
 */
- (void)sdkManagerAntiAddictionWarn:(NSString * _Nullable)warningMessage;

/*!
 @brief Fired when the Decree-147 anti-addiction gate kicks the player. The SDK
        still shows its own popup and signs the user out when it is acknowledged;
        this hook is additive (e.g. return to the lobby, stop the game loop).
 @param kickReason    server enum: `curfew` / `daily_limit_reached` /
                      `session_limit_reached` / `cooldown`; nil when absent.
 @param cooldownUntil ISO-8601 instant the forced break ends, for `cooldown`; nil
                      otherwise. Server-authoritative — never derive it from the config.
 */
- (void)sdkManagerAntiAddictionKick:(NSString * _Nullable)kickReason
                      cooldownUntil:(NSString * _Nullable)cooldownUntil;

@end

/// Visual style for the SDK's loading/progress HUD (used by IAP verify, login,
/// account ops, etc.). Pick once at app startup via +setHUDStyle:.
typedef NS_ENUM(NSInteger, VLPlayHUDStyle) {
    VLPlayHUDStyleLight = 0,  // white HUD, dark text (default)
    VLPlayHUDStyleDark  = 1,  // black HUD, white text
};

@interface VLPlaySDKManager : NSObject

//@property (nonatomic, weak) id<FBSDKSharingDelegate> fbSharingdelegate;
@property (nonatomic, weak) id<VLPlaySDKManagerDelegate> delegate;
@property (nonatomic) BOOL isShowCloseButtonInAuthenVC; // default NO
@property (nonatomic) BOOL allowRotationInLoginView; // default YES
@property (nonatomic) UIInterfaceOrientationMask loginViewOrientationMask; // default UIInterfaceOrientationMaskAll (defers to host Info.plist; set before signIn() to narrow)

@property (nonatomic) BOOL allowRotationInPaymentView; // default YES
@property (nonatomic) UIInterfaceOrientationMask paymentViewOrientationMask; // default UIInterfaceOrientationMaskAll (defers to host Info.plist)

@property (nonatomic) BOOL isSandbox; //default NO
@property (nonatomic) BOOL ignoreCaptcha; //default NO
@property (nonatomic) BOOL isSaveAccessToken; // default NO
@property (nonatomic) NSString* sitekeyRecapchaIos;

/*!
 @brief shared instance of VLPlaySDKManager
 */
+ (VLPlaySDKManager *)defaultManager NS_AVAILABLE_IOS(8_0);

// MARK: handle application delegate methods

/*!
 @brief The very first method need to add to project. It will init the SDK and handle the most important delegate method of application.
 @param application shared application instance
 @param launchOptions object contains all launching configuration
 @note require
 */

+ (void)handleApplication:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions NS_AVAILABLE_IOS(8_0);

/*!
 @brief Set the visual style for the SDK's loading/progress HUD (IAP verify
 banner, login spinner, account ops). Call once after handleApplication:.
 @param style VLPlayHUDStyleLight (default) or VLPlayHUDStyleDark.
 */
+ (void)setHUDStyle:(VLPlayHUDStyle)style;

/*!
 @brief Override the active SDK language at runtime (DECISION-5 layer 1).
 @param language one of `@"vie"`, `@"eng"`, `@"khm"` (case-insensitive 3-letter
        code). Unknown / empty code falls back to device locale → `vie`. `khm`
        is accepted-but-not-translated in Phase 1-5 (data fills land in Phase 6).
 @discussion Persists in-memory only. Posts `VLPlayLanguageDidChangeNotification`
        on the main queue so currently-mounted SDK views can refresh their text.
        Safe to call on any thread.
 */
+ (void)setLanguage:(NSString *)language NS_AVAILABLE_IOS(8_0);
/*!
 @brief method to handle the open URL to the app
 @param application shared application instance
 @param url the open URL
 @param sourceApplication The bundle ID of the app that is requesting your app to open the URL (url).
 @param annotation A property list object supplied by the source app to communicate information to the receiving app.
 @note require
 */
+ (BOOL)handleApplication:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation NS_AVAILABLE_IOS(8_0);


/**
 

 @param application <#application description#>
 @param url <#url description#>
 @param options <#options description#>
 @return <#return value description#>
 */
+ (BOOL)handleApplication:(UIApplication *)application openURL:(NSURL *)url options:(nonnull NSDictionary<NSString *,id> *)options;

// MARK: methods
/*!
 @brief method to open sign in interface.
 @note require
 */
- (void)signIn NS_AVAILABLE_IOS(8_0);

// MARK: methods
/*!
 @brief method to open sign in interface.
 @param parentController the view controller where sign-in controller presented
 @note require
 */
- (void)signInFromViewController:(UIViewController *)parentController NS_AVAILABLE_IOS(8_0);
- (void) openTakeScreenshot;
- (void)openLoginFormFromViewController:(UIViewController *_Nullable)parentController NS_AVAILABLE_IOS(8_0);
- (void)openSupportFromViewController:(UIViewController *_Nullable)viewController;

- (void)openDeleteAccount:(UIViewController *_Nonnull)viewController;
/*!
 @brief method to open register interface.
 @note optional
 */
- (void)registerNow NS_AVAILABLE_IOS(8_0);

/*!
 @brief method to open register interface.
 @param parentController the view controller where register controller presented
 @note optional
 */
- (void)registerFromViewController:(UIViewController *)parentController NS_AVAILABLE_IOS(8_0);

/*!
 @brief method to clear the current session and logged-in user info.
 @note require
 */
- (void)signOut NS_AVAILABLE_IOS(8_0);

/*!
 @brief Upgrade the currently signed-in guest account to a full account by setting a username + password.
 @param username 8–12 chars, lowercase alphanumeric only.
 @param password min 8 chars.
 @param email optional — pass nil or empty to omit.
 @param completion called with status=YES on success (VIDUser has been updated with the new accessToken, refreshToken and isGuest=NO). On failure, NSError.userInfo[@"message"] contains a localized message.
 @note Requires the user to be signed in as a guest (accessToken present). This is the Bearer token; never send it in the body.
 */
- (void)upgradeGuestAccountWithUsername:(NSString *)username
                               password:(NSString *)password
                                  email:(NSString *)email
                             completion:(void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Send an SMS OTP to the given phone number.
 @param phone 10-digit Vietnamese phone number (regex `^[0-9]{10}$`).
 @param completion status=YES when BE acknowledges send. status=NO + error on invalid phone or rate-limit.
 @note Requires the user to be signed in (Bearer token). Rate limit: 1 req/60s + 5 req/hour per IP.
 */
- (void)sendOTPToPhone:(NSString *)phone
            completion:(void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Verify the OTP code the user received via SMS.
 @param phone same 10-digit phone used in sendOTPToPhone:.
 @param otpCode 6-digit code from SMS.
 @param completion status=YES when code is correct.
 @note Does NOT require a Bearer token (uses `x-api-key` only). Rate limit: 1 req/60s per IP.
 */
- (void)verifyOTPWithPhone:(NSString *)phone
                   otpCode:(NSString *)otpCode
                completion:(void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Bind a social provider (Facebook / Google / Apple) to the currently signed-in account.
 @param provider one of the public constants declared at the top of this header:
        `VLPlayBindProviderFacebook`, `VLPlayBindProviderGoogle`, `VLPlayBindProviderApple`.
 @param token provider OAuth access token obtained from the corresponding SDK
        (FacebookSDK access token / GoogleSignIn idToken / ASAuthorizationAppleIDCredential identityToken).
 @param completion status=YES when the provider is successfully bound. status=NO + error on conflict
        (already bound, or this provider is bound to a different account), invalid/expired OAuth token,
        or expired session. `error.userInfo[@"message"]` contains a localized message.
 @note Requires the user to be signed in (Bearer token is sent automatically).
 */
- (void)bindProvider:(NSString *)provider
               token:(NSString *)token
          completion:(void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief SDK-driven social bind for the "link account" button (guest → social).
        The SDK runs the native Google / Facebook OAuth internally — the host game
        does NOT integrate the FB / Google SDKs — then binds the resulting token to
        the currently signed-in account via `-bindProvider:token:completion:`.
 @param provider `VLPlayBindProviderGoogle`, `VLPlayBindProviderFacebook`, or
        `VLPlayBindProviderApple` (Apple requires iOS 13+; recommended on iOS when
        Google/Facebook are offered, per App Store guideline 4.8).
 @param completion status=YES when bound; NO + error on user-cancel, OAuth failure,
        or bind failure. `error.userInfo[@"message"]` contains a localized message.
 @note Gate the button on `+isCurrentUserGuest`. End-to-end recovery (re-login by the
        bound social returning the same guest player) depends on the BE `accountBind`
        fix — see handoff/be/GUEST-SOCIAL-BIND-2026-07-02.md.
 */
- (void)bindSocialProvider:(NSString *)provider
                completion:(void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief YES when the currently signed-in account is a guest / quick-start account.
        Use to gate a "link account" / upgrade button in the host UI.
 */
+ (BOOL)isCurrentUserGuest;

/*!
 @brief Submit identity verification (Decree 147/2024 compliance) for the currently
        signed-in user. BE persists DOB on the player record and computes ageGroup;
        play-time limits surface via the next `/anti-addiction/status` poll.
 @param fullName legal full name (Vietnamese diacritics OK). Required, non-empty.
 @param dob date of birth — accepts `DD/MM/YYYY` (Vietnamese) or ISO `YYYY-MM-DD`.
        BE performs final validation. Required, non-empty.
 @param identityCard CCCD/CMND number. Optional — pass nil or empty to omit.
 @param completion called on the main queue with status=YES on `verified:true`.
        On failure, `error.userInfo[@"message"]` contains a localized message.
 @note Requires the user to be signed in (Bearer token sent automatically).
        Idempotent — calling again overwrites prior values on the player record.
        Hosts should gate this UI on `[VLPlaySDKManager isFeatureEnabled:VLPlaySDKFeatureIdentityVerification]`.
 */
- (void)submitIdentityVerification:(NSString *)fullName
                               dob:(NSString *)dob
                      identityCard:(nullable NSString *)identityCard
                        completion:(nullable void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Full-KYC identity verification (parity Web SDK `showIdentityVerification`).
        Persists all 6 identity fields via `PUT /detail/updateInfo`, then seeds the
        Decree-147 age-group via `POST /anti-addiction/identity-verification`.
 @param fullName legal full name. Required, non-empty.
 @param dob date of birth — `DD/MM/YYYY` or ISO `YYYY-MM-DD`. Required, non-empty.
 @param identityCard CCCD (12 digits) / CMND (9 digits). Optional — pass nil/empty to omit.
 @param address residential address. Optional.
 @param placeOfGrant place of issue (Nơi cấp). Optional.
 @param issueDate issue date — `DD/MM/YYYY` or ISO `YYYY-MM-DD`. Optional.
 @param completion called on the main queue; status=YES once both calls succeed.
 @note Email/phone are NOT part of this call — they use the contact-bind flow.
        The 3-arg variant above remains for age-seed-only callers.
 */
- (void)submitIdentityVerification:(NSString *)fullName
                               dob:(NSString *)dob
                      identityCard:(nullable NSString *)identityCard
                           address:(nullable NSString *)address
                      placeOfGrant:(nullable NSString *)placeOfGrant
                         issueDate:(nullable NSString *)issueDate
                        completion:(nullable void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Redeem a gift code for the currently signed-in user.
 @param code gift code string (max 50 chars).
 @param completion status=YES on redeem success; `data` contains the openapi response body
        `data` dictionary (usually `{ code, type }`). status=NO + error on invalid/used
        code, expired session, or rate limit (statusCode 10001 = 5 req/60s per IP).
        `error.userInfo[@"message"]` contains a localized message.
 @note Requires the user to be signed in (Bearer token). gameId is taken from
       `VLPlayConfig.defaultConfig.appId` (plist key `VLPLAY_APP_ID`).
 */
- (void)redeemGiftcode:(NSString *)code
            completion:(void (^)(BOOL status, NSDictionary * _Nullable data, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

// MARK:
/*!
 @brief method to open purchase shop.
 */
- (void)openShop NS_AVAILABLE_IOS(8_0);
/*!
 @brief method to open purchase shop.
 @param parentController the view controller where shop controller presented
 */
- (void)openShopFromViewController:(UIViewController *)parentController NS_AVAILABLE_IOS(8_0);

/*!
 @brief method to close purchase shop.
 */
- (void)closeShop NS_AVAILABLE_IOS(8_0);

/*!
 @brief method to buy specific package by it's bundeID.
 @param areaId game server id where containt player character. Must be not nil.
  @note require
 */
- (void)buyPackage:(NSString *)packageID parentViewController:(UIViewController *)parentController NS_AVAILABLE_IOS(8_0);

/*!
 @brief Fetch the IAP package catalog from the backend for the current game.
 @param completion Called on the main queue.
        On success: `data` is an NSArray of NSDictionary, one entry per Package
        per `openapi-v3.0.yaml#/components/schemas/Package`. Fields:
        `packageId, name, description, price, currency, items[], storeProductId,
        isActive, sortOrder`.
        Legacy v2.1 response shape (`{ productId, productName, price, currency }`)
        is also passed through unchanged — caller should tolerate both.
        On failure: `error.userInfo[@"message"]` contains a localized message.
 @discussion Calls `GET /api/v1/client/purchase/products?clientId={gameId}`.
        Requires the user to be signed in (Bearer + apikey sent automatically).
 */
- (void)getProductCatalog:(nullable void (^)(BOOL status, NSArray * _Nullable data, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Trigger a native IAP purchase for a specific consumable package and report
        the terminal status via a completion block. Uses the V3 store-first flow:
        `SKPaymentQueue` charges first, then the receipt is posted to the BE in a
        single round-trip (no `init-payment` pre-allocation).
 @param productId Apple App Store product ID — must match `Package.storeProductId`
        in the BE catalog (or the legacy `Package.code` fallback BE supports
        for V1 packages). Required.
 @param productName Display name used for AppsFlyer event tagging. Optional.
 @param amount Local price in VND (analytics tagging only — Apple charges
        whatever the App Store Connect price tier specifies). Pass 0 if unknown.
 @param parentController Reserved for future session-expired UI; currently unused
        in the V3 flow (auth errors trigger NetworkModal's silent token refresh).
 @param completion Called on the main queue once the IAP transaction settles.
        `status=YES` → Apple charged the user AND BE accepted the receipt.
        Includes the `DELIVERY_PENDING` case (BE retry script will deliver to
        the game server later) and the `PURCHASE_HAS_EXIST` idempotent replay.
        `status=NO` → Apple charge failed, user cancelled, or BE rejected the
        receipt as invalid (the transaction is left unfinished so Apple's queue
        will retry on next launch). On failure: `error.userInfo[@"message"]`
        contains a localized message; `error.userInfo[@"productId"]` echoes
        the input productId.
 @discussion Internally:
        1. `SKProductsRequest` validates productId against ASC.
        2. `SKPaymentQueue.addPayment` shows the Apple sheet.
        3. Before showing the Apple sheet the SDK calls
           `POST /api/v1/client/purchase/init-payment` with
           `{gameId, bundleId, productId, paymentMethod:"APPLE",
           rechargeType:"IN_GAME", roleId?, ...}` and stores the
           returned `purchaseCode` keyed by productId.
        4. On `.Purchased`, the receipt is read and POSTed to
           `/api/v1/client/purchase/complete-payment` with
           `{purchaseCode, receiptData, externalTransactionId}`. BE
           verifies the receipt with Apple, marks the existing
           order record (type:APPLE) as paid, and dispatches delivery
           to the game server's `urlCallbackGame`.
        5. SDK calls `finishTransaction` only on captured-payment outcomes
           (success / DELIVERY_PENDING / PURCHASE_HAS_EXIST). Validation
           failures leave the transaction unfinished for Apple to retry.
        6. `VLPlaySDKManagerDelegate.sdkManagerDidPurchaseSuccessfully` is still
           fired for backward compatibility.

 @note SDK auto-shows its own full-screen input blocker during App Store
       handover and receipt verify (VLPlayLoading, owned window). Caller MUST
       NOT layer a competing fullscreen loading overlay — it will stack on top
       and clip it. Use a button-disabled state to debounce double-taps
       instead. Style the SDK HUD via +setHUDStyle:.
 */
- (void)purchasePackageWithProductId:(NSString *)productId
                         productName:(nullable NSString *)productName
                              amount:(NSInteger)amount
                    parentController:(nullable UIViewController *)parentController
                          completion:(nullable void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief V3 IAP Phase 1 entry — adds `isConsumable` flag + `extraData` payload.
        Legacy 5-arg overload defaults to {isConsumable:YES, extraData:nil}.
 @param isConsumable YES for gem packs / coins (will replay if BE 1034 hits cold
        launch); NO for remove-ads / lifetime VIP (surfaces via
        -restorePurchases:completion: on reinstall / device migration).
        Apple finishTransaction is unified across both types — the flag is
        persisted to NSUserDefaults so post-restore routing + partner-game
        analytics can distinguish.
 @param extraData partner-game payload forwarded to BE on init-payment +
        complete-payment. Parity Android. Pass nil when not used.
 */
- (void)purchasePackageWithProductId:(NSString *)productId
                         productName:(nullable NSString *)productName
                              amount:(NSInteger)amount
                        isConsumable:(BOOL)isConsumable
                           extraData:(nullable NSString *)extraData
                    parentController:(nullable UIViewController *)parentController
                          completion:(nullable void (^)(BOOL status, NSError * _Nullable error))completion NS_AVAILABLE_IOS(8_0);

/*!
 @brief Restore non-consumable purchases. App Store policy 3.1.3(b)
        REQUIRES this for any app shipping non-consumable IAPs — review will
        reject without a visible Restore Purchases entry point. Wraps
        [SKPaymentQueue restoreCompletedTransactions]; each restored transaction
        fires `onRestored(productId, transactionId)` after BE re-verify
        (idempotent, returns 1034 PURCHASE_HAS_EXIST on prior credit).
        `completion(restoredCount, error)` fires once at end.
 */
typedef void (^VLPlayRestoreItemBlock)(NSString * _Nonnull productId, NSString * _Nonnull transactionId);
typedef void (^VLPlayRestoreCompletionBlock)(NSInteger restoredCount, NSError * _Nullable error);

- (void)restorePurchases:(nullable VLPlayRestoreItemBlock)onRestored
              completion:(nullable VLPlayRestoreCompletionBlock)completion NS_AVAILABLE_IOS(8_0);

/*!

/*!
 @brief method to update game server and character info.
 @param areaId game server id where containt player character. Must be not nil.
 @param dataString whatever extend data server game want to inspire to sdk server.
  @note require
 */
- (void)updateGameInfo:(NSString *)serverGameID roleID:(NSString *)roleID roleName:(NSString *)roleName NS_AVAILABLE_IOS(8_0);

/*!
 @brief Log a custom AppsFlyer in-app event. AppsFlyer-standard passthrough so
        any integrating game can fire its own events freely.
 @discussion Mirrors `[[AppsFlyerLib shared] logEvent:withValues:]`. Compose any
        event name + values — use AppsFlyer's predefined `AFEvent*` /
        `AFEventParam*` constants, or your own custom strings. Routed through the
        SDK so: (1) the CMS dynamic AppsFlyer-events layer (`/sdk/config`
        `afEvents`) gates dispatch — when enabled, ONLY events declared (and not
        disabled) in the CMS map are sent, under their mapped name, (2) the
        master tracking toggle (`features.appsFlyerTracking`) is honored, and
        (3) events fired before SDK init are queued and replayed.
 @param eventName AppsFlyer event name. Required, non-empty.
 @param values    Event values (AF param keys → values). May be nil.
 */
- (void)logEvent:(NSString *)eventName withValues:(nullable NSDictionary<NSString *, id> *)values NS_AVAILABLE_IOS(8_0);

/*! @brief Convenience: log a custom AppsFlyer event with no values. */
- (void)logEvent:(NSString *)eventName NS_AVAILABLE_IOS(8_0);

/*!
 @brief Enable verbose AppsFlyer SDK logging (full network sends + responses)
        for diagnostics — e.g. confirming events reach AppsFlyer without a proxy.
 @discussion LOGGING ONLY: does not rename events or change data routing —
        events still report as production data. Safe to call any time, ideally
        before `handleApplication:didFinishLaunchingWithOptions:`. Do NOT leave
        enabled in App Store builds (prints request payloads to the console).
 @param enabled YES to enable AppsFlyer debug logging.
 */
+ (void)setVerboseLogging:(BOOL)enabled;

/*!
 @brief Show the identity-verification popup on demand (parity Web SDK
        `showIdentityVerification`). Presents the clean dedicated KYC popup
        over the current screen. For a guest account it shows the upgrade
        panel instead (guest must upgrade before verifying); for an
        already-verified account it dismisses with a notice.
 @note No-op when CMS `features.identityVerification` is OFF. Distinct from the
        automatic post-login gate (which skips guests entirely).
 */
+ (void)showIdentityVerification;

/*!
 @brief Show the standalone guest-account upgrade popup on demand (parity Web SDK
        `upgradeGuestAccount()` / `showGuestUpgradePopup`). Presents the dedicated
        upgrade form (username / password / confirm / email) over the current
        screen so a guest can create a full account.
 @note Only acts for a guest account; for a non-guest it shows a brief
        "not a guest account" notice and does nothing.
 */
+ (void)showGuestUpgrade;

/*!
 @brief method to hit custom event to GA.
 @param activity type of activity
 @param extend name of undefine activity. Ex: if you want to log when user spend some coins, sao, gold,... you can log an activity with extend data is "TIEU_SAO" "TIEU_GOLD" "TIEU_COIN" ...
        extend extend string must be uppercase and not include 'space' character
 @param userName logged in user name
 @param userId logged in user id
 @note optional
 */
- (void)hitActivity:(APICategoryType)activity extendData:(NSString *)extend forUser:(NSString *)userName userId:(NSString *)userId completion:(void (^)(BOOL status, id responsedObject, NSError *error))completionBlock  NS_AVAILABLE_IOS(8_0);
/*!
 @brief method to hit custom event to GA.
 @param activity type of activity
 @param extend name of undefine activity. Ex: if you want to log when user spend some coins, sao, gold,... you can log an activity with extend data is "TIEU_SAO" "TIEU_GOLD" "TIEU_COIN" ...
 extend extend string must be uppercase and not include 'space' character
 @param amountNumber quantity of extend parameter. Ex: gold, coins, ...
 @param userName logged in user name
 @param userId logged in user id
 @note optional
 */
- (void)hitActivity:(APICategoryType)activity extendData:(NSString *)extend amount:(NSInteger)amountNumber forUser:(NSString *)userName userId:(NSString *)userId completion:(void (^)(BOOL status, id responsedObject, NSError *error))completionBlock NS_AVAILABLE_IOS(8_0);

/*!
 @brief method to hit custom event to GA.
 @param activity type of activity
 @param category like Google Analytics
 @param action like Google Analytics
 @param label like Google Analytics
 @param value like Google Analytics
 @note optional
 */
- (void)hitCustomActivity:(NSInteger)activity
                 category:(NSString *)category
                   action:(NSString *)action
                    label:(NSString *)label
                    value:(NSNumber *)value
               completion:(void (^)(BOOL status, id responsedObject, NSError *error))completionBlock;
/*!
 @brief method to change main color on navigation bar.
 @param mainColor desired color.
 @note optional
 */
- (void)changeSDKMainColor:(UIColor *)mainColor;

/*!
 @brief method to show the notification message in the notification bar.
 @param message the message will be shown.
 @note optional
 */
- (void)showNotificationMessage:(NSString *)message;

/*!
 @brief hard code the utm source and campaign for direct download version.
 @param utmString the utm string.
 @note optional
 */
- (void)hardcodeUTM:(NSString *)utmString;

/*!
 @brief get the device token for APNS.
 @note optional
 */
- (NSString *)getDeviceToken;

/*!
 @brief Parse a `vlplay://` deep-link and broadcast `VLPlayDeepLinkNotification`.
 @discussion Game hosts can call this from `application:openURL:options:` or
        `scene:openURLContexts:` so universal-link / push-tap dispatches funnel through
        the same SDK pipeline. Returns NO when the URL scheme is not `vlplay`.
 @param urlString a string like `vlplay://shop` or `vlplay://giftcode?code=ABC123`.
 */
+ (BOOL)handleDeepLink:(NSString *)urlString;

- (void)initATT;

/*!
 @brief Rendezvous gate fired from VLPlaySDKAppDelegate's UNUserNotificationCenter
        completion handler. The ATT prompt is held until BOTH this method and
        `handleApplicationDidBecomeActive:` have fired, so the system Notification
        modal and the ATT modal can never overlap on iOS 18 landscape — the layout
        race that causes the stuck-input symptom.
 @discussion Safe to call from any thread; trampolines to main internally.
 */
- (void)notificationPromptSettled;

/**
 checking uninstall With AF
 
 @param deviceToken <#deviceToken description#>
 */
+ (void)handleCheckingUninstallWithAF:(NSData *)deviceToken;


/**
 Shared Text to facebook

 @param link link description
 */
+ (void)facebookShareWithLink:(NSString *_Nonnull)link;
/**
 Shared Images to facebook

 @param images [UIImage]
 */
+ (void)faceBookShareWithImage:(NSArray *_Nonnull)images;

#pragma mark - Remote SDK Config (CMS-driven)

// Payment method identifiers (mirror of VLPlayPaymentMethod* constants — kept
// here so games don't need to import the internal config header).
extern NSString * const VLPlaySDKPaymentMethodAppleIAP;     // "apple_iap"
extern NSString * const VLPlaySDKPaymentMethodGooglePlay;   // "google_play"
extern NSString * const VLPlaySDKPaymentMethodAppotaPay;    // "appotapay"
extern NSString * const VLPlaySDKPaymentMethodExternal;     // "external"

// Feature flag names (matches CMS toggles)
extern NSString * const VLPlaySDKFeatureIdentityVerification;
extern NSString * const VLPlaySDKFeatureAntiAddiction;
extern NSString * const VLPlaySDKFeatureGuestLogin;
extern NSString * const VLPlaySDKFeatureAppsFlyerTracking;
extern NSString * const VLPlaySDKFeatureOTPRequired;
extern NSString * const VLPlaySDKFeatureEmailVerification;

/*!
 @brief Returns the current SDK config from CMS as a serialized NSDictionary,
        or nil if not loaded yet.
 @discussion Config is fetched from `GET /api/v1/sdk/config?clientId=...` at
        SDK init. Sources in order: NSUserDefaults cache → server → plist
        fallback. Returned dict mirrors openapi.yaml `SdkConfigResponse.data`
        shape: nested `features`, `socialLogin.{facebook,google,apple}`,
        `support`, plus top-level `paymentMethods[]`.
 */
+ (nullable NSDictionary *)currentConfig NS_AVAILABLE_IOS(8_0);

/*!
 @brief YES if any source (cache/server/plist) has populated currentConfig.
 */
+ (BOOL)isConfigReady NS_AVAILABLE_IOS(8_0);

/*!
 @brief Check whether a CMS feature flag is ON for this game.
 @param featureName one of the `VLPlaySDKFeature*` constants above.
 @return NO if unknown name or config not loaded.
 */
+ (BOOL)isFeatureEnabled:(NSString *)featureName NS_AVAILABLE_IOS(8_0);

/*!
 @brief Check whether a CMS payment method is enabled for this game.
 @param methodId one of `VLPlaySDKPaymentMethod*` constants.
 */
+ (BOOL)isPaymentMethodEnabled:(NSString *)methodId NS_AVAILABLE_IOS(8_0);

/*!
 @brief All enabled payment method ids for this game (CMS checkboxes).
 */
+ (NSArray<NSString *> *)enabledPaymentMethods NS_AVAILABLE_IOS(8_0);

/*!
 @brief Convenience: `support.hotline` from CMS, or empty string if not set.
 */
+ (NSString *)supportHotline NS_AVAILABLE_IOS(8_0);

/*!
 @brief Convenience: `support.fanpage` URL from CMS, or empty string.
 */
+ (NSString *)supportFanpage NS_AVAILABLE_IOS(8_0);

/*!
 @brief Convenience: `support.emailSupport` from CMS, or empty string.
 */
+ (NSString *)supportEmailSupport NS_AVAILABLE_IOS(8_0);

/*!
 @brief Convenience: `sdkMinVersion` from CMS, or empty string.
 */
+ (NSString *)sdkMinVersion NS_AVAILABLE_IOS(8_0);

#pragma mark - Floating Action Ball (FAB) — DEPRECATED forwarders

/*!
 Sprint E-9 (2026-05-25): single FAB home is `VLPlayFAB` (`#import
 <VLPlaySDK/VLPlayFAB.h>`). The 7 `+[VLPlaySDKManager …]` FAB methods below are
 thin forwarders kept for ABI safety / Phase 1 partner code; drop in v2.0.
 */

+ (void)showFloatingBall
    __deprecated_msg("Use +[VLPlayFAB show] (Sprint E-9 single-home rename)")
    NS_AVAILABLE_IOS(13_0);

+ (void)dismissFloatingBall
    __deprecated_msg("Use +[VLPlayFAB dismiss] (Sprint E-9 single-home rename)")
    NS_AVAILABLE_IOS(13_0);

+ (void)setFABContext:(nullable NSDictionary<NSString *, NSString *> *)context
    __deprecated_msg("Use +[VLPlayFAB setContext:] (Sprint E-9 single-home rename)")
    NS_AVAILABLE_IOS(13_0);

+ (void)configureFAB:(nullable NSDictionary *)config
    __deprecated_msg("Use +[VLPlayFAB configure:] (Sprint E-9 single-home rename)")
    NS_AVAILABLE_IOS(13_0);

+ (void)showHud
    __deprecated_msg("Use +[VLPlayFAB showHudWithInitialTab:] with VLPlayFABTabAccount (Sprint E-9)")
    NS_AVAILABLE_IOS(13_0);

+ (void)showHudWithTab:(VLPlayFABTab)tab
    __deprecated_msg("Use +[VLPlayFAB showHudWithInitialTab:] (Sprint E-9 single-home rename)")
    NS_AVAILABLE_IOS(13_0);

+ (void)setContext:(nullable NSDictionary<NSString *, NSString *> *)opts
    __deprecated_msg("Use +[VLPlayFAB setContext:] (Sprint E-9 single-home rename)")
    NS_AVAILABLE_IOS(13_0);

@end

NS_ASSUME_NONNULL_END

