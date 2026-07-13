//
//  AppsFlyerHelper.h
//  VLPlaySDK
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

// Thin wrapper around AppsFlyerLib for SDK-side analytics. The helper owns
// init, customer-user-id, and a generic event dispatcher. Specific events
// are intentionally NOT defined here — call sites use trackCustomEvent so
// the event taxonomy can evolve without touching SDK internals.
@interface AppsFlyerHelper : NSObject

+ (instancetype)shared;

// Initialization
- (void)initWithDevKey:(NSString *)devKey appStoreId:(NSString *)appStoreId;
- (void)setCustomerUserId:(NSString *)userId;
- (void)setDevMode:(BOOL)devMode;
- (BOOL)isDevMode;

// Verbose AppsFlyer SDK logging (full network sends + responses). LOGGING
// ONLY — does not rename events or change data routing (that was devMode's
// `dev_` prefix). Safe to toggle any time, pre- or post-init. Do not leave
// enabled in App Store builds.
- (void)setDebugLogging:(BOOL)enabled;

// Master enable/disable for event dispatch. Defaults to YES. When set to NO,
// trackCustomEvent: short-circuits without queuing or sending. Toggled at
// runtime by VLPlaySDKManager based on remote config `features.appsFlyerTracking`.
- (void)setTrackingEnabled:(BOOL)enabled;
- (BOOL)isTrackingEnabled;

// AF-Events dynamic layer (AF-EVENTS contract C1). Pushed from /sdk/config
// top-level `afEvents`. When enabled: drop events whose stable key is in
// `disabled`, and remap stable key → AF name via `map` (passthrough if absent).
// Default off → resolver no-op. `map`/`disabled` may be nil (treated as empty).
- (void)setAfEventsEnabled:(BOOL)enabled
                       map:(nullable NSDictionary<NSString *, NSString *> *)map
                  disabled:(nullable NSArray<NSString *> *)disabled;

// Hard kill-switch: also flips AppsFlyerLib.isStopped so the underlying SDK
// halts ALL outbound traffic (lifecycle, session, attribution, custom). Use
// this for the CMS master toggle — `setTrackingEnabled:` alone only short-
// circuits our wrapper's logEvent path, leaving AF SDK session pings active.
- (void)setMasterEnabled:(BOOL)enabled;

// Generic event dispatch — primary public API. Callers compose their own
// event name + params per the canonical AppsFlyer event list.
// `params` may be nil. Events posted before `initWithDevKey:appStoreId:`
// completes are queued (bounded) and replayed once init finishes.
- (void)trackCustomEvent:(NSString *)eventName params:(nullable NSDictionary *)params;

@end

NS_ASSUME_NONNULL_END
