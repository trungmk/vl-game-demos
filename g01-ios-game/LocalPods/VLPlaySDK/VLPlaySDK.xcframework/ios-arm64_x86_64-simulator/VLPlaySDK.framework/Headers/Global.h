//
//  Global.h
//  VLPlaySDK
//
//   7/25/16.
//  
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "VIDUser.h"
@class VLPlayShopViewController;

NS_ASSUME_NONNULL_BEGIN

// Marks which auth flow last invoked the network — read by the
// handleSignInResponse: chokepoint to fire the right AppsFlyer event
// (sdk_login_completed vs sdk_register_completed vs sdk_third_login_completed).
// Callers set this immediately before triggering the auth network call;
// handleSignInResponse: resets it to Unknown after consuming.
typedef NS_ENUM(NSInteger, VLPlayAuthFlow) {
    VLPlayAuthFlowUnknown   = 0,
    VLPlayAuthFlowLogin,
    VLPlayAuthFlowRegister,
    VLPlayAuthFlowAutoLogin,
    VLPlayAuthFlowSocial,
};

@interface Global : NSObject

+ (Global *)sharedInstance;

/*
 Device token's used to push notification
 */

@property (nonatomic) BOOL isReachable;

@property (nonatomic, copy, getter=getPushDeviceToken) NSString *pushDeviceToken;
@property (nonatomic, copy, getter=getAdvertisingIdentifier) NSString *advertisingIdentifier;

@property (nonatomic, strong) UIColor *globalMainColor;
@property (nonatomic, strong) UIColor *gButtonColor;

@property (nonatomic, copy) NSString *savedUserName;

@property (nonatomic) CGFloat gPortraitNavBarHeight;
@property (nonatomic) CGFloat gLandscapeNavBarHeight;

@property (nonatomic) BOOL flagOpenApp;
@property (nonatomic) BOOL bufferLinkChecked;
@property (nonatomic) BOOL isWaitingForShowLogin;
@property (nonatomic) BOOL isWaitingForShowRegister;

@property (nonatomic) BOOL enableFacebookLogin;
@property (nonatomic) BOOL enableGoogleLogin;
@property (nonatomic) NSString *supportURL;
@property (nonatomic, copy) NSString *hotline;
@property (nonatomic, copy) NSString *emailSupport;
@property (nonatomic, weak) VLPlayShopViewController *shopVC;

@property (nonatomic, readonly) NSString * language;
/*
 UTM distribution URL
 Ex: "democlpt://?utm_source=facebook&utm_campaign=demo&utm_medium=1706"
 
 @"clpt://?utm_source=home&utm_campaign=direct&utm_medium="
 */

@property (nonatomic) NSInteger paymentGoldNumber;
@property (nonatomic, copy) NSString *paymentMessage;
@property (nonatomic, copy) NSString *paymentCurrency;
@property (nonatomic, copy) NSString *currentOrderNo;
@property (nonatomic) BOOL showTakeScreenshot;
@property (nonatomic) BOOL loginIsUpdatePassword;
@property (nonatomic) BOOL needsIdentityVerification;
// Set YES once the auto-show identity popup has actually appeared this session
// (in the VC's -viewDidAppear, auto mode only). Guards re-presenting on the same
// session after dismiss, and lets the cold-launch config re-trigger keep retrying
// until it truly shows. Reset in -handleSignOut.
@property (nonatomic) BOOL identityAutoShownThisSession;
// Set YES the moment a guest verifies identity in THIS process session. The guest
// upgrade nudge (deviation: only from the 2nd entry) is suppressed when this is YES
// (just verified) and shown when NO (already-verified guest on a fresh launch). Process-
// local on purpose — a cold-launch is a new process so it resets to NO. Reset in
// -handleSignOut.
@property (nonatomic) BOOL guestVerifiedThisSession;
@property (nonatomic) BOOL antiAddictionEnabled;
@property (nonatomic) NSInteger unReadMailCount;

@property (nonatomic, assign) VLPlayAuthFlow lastAuthFlow;


- (NSString *)getUtmString;
- (void)setUtmString:(NSString *)utmString;

// Cold-launch identity re-trigger: re-evaluate the auto-show identity gate once
// the authoritative /sdk/config is applied (VLPlaySdkConfigManager.fetchConfig),
// in case the post-login gate ran before the config landed. No-op unless a
// non-guest session is active with its profile loaded and the popup hasn't shown
// yet this session.
- (void)retryIdentityGateIfReady;

// Post-identity step of the mandatory gate (IDENTITY-GATE-FLOW-2026-06-23): AA
// notice (once/day) → enter → for guests an optional dismissable upgrade nudge.
// Called by the identity popup once verification finishes (success or already
// verified). Idempotent per session.
- (void)runPostIdentitySequence;

- (void)handleSignInResponse:(id)loginData authenType:(AuthenType)type containerController:(UIViewController *)controller showWelcome:(BOOL)welcome;
- (void)handleSignOut;

@end

NS_ASSUME_NONNULL_END
