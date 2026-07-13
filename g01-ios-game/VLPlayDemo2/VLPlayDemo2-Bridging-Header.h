//  VLPlaySDK is modular (DEFINES_MODULE=YES) so Swift uses `import VLPlaySDK`.
//  AntiAddiction headers aren't in the umbrella VLPlaySDK.h — import here so demo2
//  Swift can reach AntiAddictionManager / AntiAddictionStatus directly.

#import <VLPlaySDK/AntiAddictionManager.h>
#import <VLPlaySDK/AntiAddictionStatus.h>
#import <VLPlaySDK/AntiAddictionConfig.h>
#import <VLPlaySDK/VLPlaySDKManager.h>

// Demo SDK_DEBUGGER opt-in: forward-declare VLPlaySDKManager+Internal selectors so
// Swift dispatch can reach them. Partner-game integrators don't carry this
// bridging header, so the declarations stay demo-scoped (P3-01 internal scope
// preserved). Implementation lives in VLPlaySDK/VLPlaySDKManager.m.
@interface VLPlaySDKManager (DemoInternal)
- (void)refreshAccountInfo:(void (^_Nullable)(BOOL status, NSError *_Nullable error))completion;
- (void)deactivateAccountWithCompletion:(void (^_Nullable)(BOOL status, NSError *_Nullable error))completion;
- (void)forceRefreshTokenWithCompletion:(void (^_Nullable)(BOOL status,
                                                            NSString *_Nullable accessToken,
                                                            NSString *_Nullable refreshToken,
                                                            NSError *_Nullable error))completion;
- (void)listSuccessfulTransactionsAtPage:(NSInteger)page
                                    size:(NSInteger)size
                              completion:(void (^_Nullable)(BOOL status,
                                                            NSArray *_Nullable transactions,
                                                            NSError *_Nullable error))completion;
@end
