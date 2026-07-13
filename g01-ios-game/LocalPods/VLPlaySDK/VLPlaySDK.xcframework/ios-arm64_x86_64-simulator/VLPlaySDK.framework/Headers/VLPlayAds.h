//
//  VLPlayAds.h
//  VLPlaySDK
//
//  Layer 1 — the public, provider-agnostic ads facade (IAA-01 design §3).
//  PUBLIC header. The API never names a mediation vendor — the provider is
//  server-driven per game via the /sdk/config `ads` block. Mirror of the
//  Android VLPlayAds facade.
//
//  Lifecycle: the module initializes itself from remote config after the SDK
//  bootstrap (no game-side init call). Every method is safe to call at any
//  time — when ads are disabled/not ready the delegates receive a
//  provider-neutral VLPlayAdError instead of anything throwing into game code.
//
//  Enabling ads in a game (DP-2a): the SDK framework does NOT link any ad
//  vendor. Add the vendor pod to the APP (e.g. `pod 'AppLovinSDK'`), have Ops
//  enable the CMS ads config — the SDK detects the vendor at runtime. Remove
//  the pod (or Ops flips ads.enabled off) to remove ads; nothing crashes.
//
//  Rewarded integrity (design §6.1): grant currency ONLY in
//  `vlplayAdRewardConfirmed:` (server SSV verified) — never in
//  `vlplayAdUserRewarded:` (advisory).
//
//      [VLPlayAds preload:@"rewarded_shop"];
//      if ([VLPlayAds isReady:@"rewarded_shop"]) {
//          [VLPlayAds showRewarded:@"rewarded_shop" fromViewController:vc delegate:self];
//      }
//

#import <UIKit/UIKit.h>
#import <VLPlaySDK/VLPlayAdsModels.h>

NS_ASSUME_NONNULL_BEGIN

@interface VLPlayAds : NSObject

/// YES when the remote config enables ads for this game.
+ (BOOL)isEnabled;

/// YES when the mediation provider finished initializing.
+ (BOOL)isInitialized;

/// Preloads a fill for the placement (recommended before show). Fire-and-forget.
+ (void)preload:(NSString *)placementId;

/// Preloads a fill with load-result callbacks.
+ (void)preload:(NSString *)placementId delegate:(nullable id<VLPlayAdLoadDelegate>)delegate;

/// Preloads every placement flagged preload:true in the remote config.
+ (void)preloadAll;

/// YES when a fill for the placement is loaded and ready to show.
+ (BOOL)isReady:(NSString *)placementId;

/// Shows a rewarded ad. The delegate receives the full lifecycle on the main
/// thread; failures (disabled / unknown placement / not ready) arrive as
/// `vlplayAdShowFailed:` — nothing throws. The SDK holds the delegate strongly
/// until the reward flow reaches a terminal state.
+ (void)showRewarded:(NSString *)placementId
  fromViewController:(nullable UIViewController *)viewController
            delegate:(nullable id<VLPlayRewardedAdDelegate>)delegate;

/// Shows an interstitial ad (frequency-capped per the remote config).
+ (void)showInterstitial:(NSString *)placementId
      fromViewController:(nullable UIViewController *)viewController
                delegate:(nullable id<VLPlayInterstitialAdDelegate>)delegate;

/// Ties impressions + SSV reward callbacks to the VLPlay account. Buffered
/// until the provider is ready.
+ (void)setUserId:(nullable NSString *)userId;

@end

NS_ASSUME_NONNULL_END
