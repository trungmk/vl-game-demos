//
//  VLPlayDeepLinkRouter.h
//  VLPlaySDK
//
//  Push notification deep-link parser. Reads `deeplink` field from APNs payload,
//  parses `vlplay://<action>[/<path>][?<query>]`, then broadcasts a notification
//  so the host game (or demo) can react. SDK does not auto-navigate — payloads
//  may arrive while the app is backgrounded or while a different flow is on
//  screen, so the integrator owns the dispatch decision.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

extern NSString * const VLPlayDeepLinkNotification;
extern NSString * const VLPlayDeepLinkActionKey;
extern NSString * const VLPlayDeepLinkPathKey;
extern NSString * const VLPlayDeepLinkParamsKey;
extern NSString * const VLPlayDeepLinkRawURLKey;

extern NSString * const VLPlayDeepLinkActionShop;
extern NSString * const VLPlayDeepLinkActionGiftcode;
extern NSString * const VLPlayDeepLinkActionAntiAddiction;
extern NSString * const VLPlayDeepLinkActionWeb;

@interface VLPlayDeepLinkRouter : NSObject

+ (BOOL)handle:(nullable NSString *)urlString;

@end

NS_ASSUME_NONNULL_END
