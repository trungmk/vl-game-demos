//
//  Constants.h
//  VLPlaySDK
//
//   11/22/16.
//  
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

//! Project version number for VLPlaySDK

#define kVLPlaySDKVersionString @"1.1.9"

#ifdef DEBUG
#define VLPlayLog(FORMAT, ...) NSLog((@"[VLPlaySDK] [%s:%d] " FORMAT), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
#define VLPlayLog(FORMAT, ...)
#endif

#define ColorFromHEX(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]
#define ColorFromAlphaHEX(rgbValue, alphaValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:(float)alphaValue]

#define VLPlayLocalizeString(string) [Utilities localizeStringForString:string]

static NSString *kUserDefaultLoadedBufferLink = @"UserDefaultLoadedBufferLink";
static NSString *kUserDefaultFirstOpen = @"UserDefaultFirstOpenKey";
static NSString *kUserDefaultCachedUser = @"UserDefaultCachedUser";
static NSString *kUserDefaultCachedUTM = @"UserDefaultCachedUTM";
static NSString *kUserDefaultCachedNewRegisteredUser = @"UserDefaultCachedNewRegisteredUser";
static NSString *kUserDefaultStackOfIAPPayment = @"UserDefaultStackOfIAPPayment";
static NSString *kUserDefaultStackOfIAPFinishLog = @"UserDefaultStackOfIAPFinishLog";
static NSString *kUserDefaultSdkConfig = @"vlplay_sdk_config_v2";

static NSString *kKeychainAdvertisingIdentify = @"Ads_Id";
static NSString *kKeychainLoggedInstall = @"cachedInstall";
// BE-GUEST-02: stable per-device fingerprint sent on POST /signup/guest so BE
// can dedup repeated guest creation from the same device. Keychain survives
// app uninstall on iOS — keeps the mapping stable across reinstalls.
static NSString *kKeychainDeviceFingerprint = @"DeviceFingerprint";

static NSString *kNotificationCheckingWithFireBase = @"NotificationCheckingWithFireBase";
static NSString *kNotificationOpenBufferLink = @"NotificationOpenBufferLink";
static NSString *kNotificationGotFacebookGoogleAuthenConfig = @"NotificationGotFacebookGoogleAuthenConfig";

static NSString *generalDeviceType = @"3";

static CGFloat kWidthOfLandscapeSocialLoginButton = 50.0;

static NSString *kStartLogInApp = @"START_PAY_STORE";
static NSString *kFinishLogInApp = @"FINISH_PAY_STORE";

typedef NS_ENUM(NSInteger, SDKLanguage) {
    SDKLanguageViet = 0,
    SDKLanguageEnglish,
    SDKLanguagePhilipine,
    SDKLanguageThailand,
    SDKLanguageLaos,
    SDKLanguageCambodia,
    SDKLanguageMyanmar,
    SDKLanguageMalaysia,
    SDKLanguageIndonesia
};

typedef NS_ENUM(NSInteger, TrackingHitType) {
    TrackingHitTypeEvent = 0,
    TrackingHitTypeScreen
};

typedef NS_ENUM(NSInteger, GACategoryType) {
    GACategoryTypeInAppEvent = 0,
    GACategoryTypeAuthen,
    GACategoryTypeTracking,
    GACategoryTypePayment
};

typedef NS_ENUM(NSInteger, GAActionType) {
    GAActionTypeFirstOpen = 0,
    GAActionTypeOpenApp,
    GAActionTypeCloseApp,
    GAActionTypeCrashApp,
    GAActionTypeInstalledApp,
    GAActionTypeLogin,
    GAActionTypeRegister,
    GAActionTypeUTMCampaign,
    GAActionTypeUTMMedium,
    GAActionTypeUTMSource,
    GAActionTypeInGame
};

typedef NS_ENUM(NSInteger, GALabelType) {
    GALabelTypeNone = 0,
    GALabelTypeVLPlayID,
    GALabelTypeFacebook,
    GALabelTypeApple,
    GALabelTypeGoogle,
    GALabelTypeVLPlay,
    GALabelTypeUTM,
    GALabelTypeVLPlayPay,
    GALabelTypeInAppPurchase
};

typedef NS_ENUM(NSInteger, APICategoryType) {
    APICategoryTypeFirstOpen = 1,
    APICategoryTypeOpen = 2,
    APICategoryTypeCloseApp = 3,
    APICategoryTypeCrashApp = 4,
    APICategoryTypeClick = 5,
    APICategoryTypePayment = 6,
    APICategoryTypeInstall = 7,
    APICategoryTypeLoginVLPlay = 8,
    APICategoryTypeQuickStartLoginVLPlay = 12,
    APICategoryTypeLoginGoogle = 9,
    APICategoryTypeLoginFacebook = 10,
    APICategoryTypeLoginApple= 11,
    APICategoryTypeRegisterVLPlay = 13,
    APICategoryTypeOther = 0
};

static NSString *kAppsFlyerEventTypeLogin = @"af_login";
static NSString *kAppsFlyerEventTypeGMOLogin = @"gmo_login";
static NSString *kAppsFlyerEventTypeRegister = @"af_complete_registration";
static NSString *kAppsFlyerEventTypeCompleteTutorial = @"af_tutorial_completion";
static NSString *kAppsFlyerEventTypeLevelAchieved = @"af_level_achieved";
static NSString *kAppsFlyerEventTypeInitialCheckout = @"af_initiated_checkout";
static NSString *kAppsFlyerEventTypePurchase = @"af_purchase";

// on Apple
static NSString *kAppsFlyerEventTypeAppleRegister = @"af_Apple_complete_registration";
// on QuickStart
static NSString *kAppsFlyerEventTypeQuickStartRegister = @"af_quickstart_complete_registration";
// on facebook
static NSString *kAppsFlyerEventTypeFBRegister = @"af_fb_complete_registration";
// Google
static NSString *kAppsFlyerEventTypeGGRegister = @"af_gg_complete_registration";

static NSString *kAppsFlyerEventTypeLoginValueKey = @"af_login";
static NSString *kAppsFlyerEventTypeGMOLoginValueKey = @"gmo_login";
static NSString *kAppsFlyerEventTypeRegisterValueKey = @"af_complete_registration";
static NSString *kAppsFlyerEventTypeCompleteTutorialValueKey = @"af_tutorial_completion";
static NSString *kAppsFlyerEventTypeLevelAchievedValueKey = @"af_level_achieved";
static NSString *kAppsFlyerEventTypeInitialCheckoutValueKey = @"af_initiated_checkout";
static NSString *kAppsFlyerEventTypePurchaseValueKey = @"af_purchase";

NS_ASSUME_NONNULL_END
