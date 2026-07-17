// To parse this JSON:
//
//   NSError *error;
//   VLPlayDetailAccountInfoModel *detailAccountInfoModel = [VLPlayDetailAccountInfoModel fromJSON:json encoding:NSUTF8Encoding error:&error];

#import <Foundation/Foundation.h>

@class VLPlayDetailAccountInfoModel;
@class VLPlayData;
@class VLPlayPlayer;

NS_ASSUME_NONNULL_BEGIN

#pragma mark - Object interfaces

@interface VLPlayDetailAccountInfoModel : NSObject
@property (nonatomic, nullable, strong) VLPlayData *data;
@property (nonatomic, nullable, copy)   NSString *message;
@property (nonatomic, nullable, strong) NSNumber *statusCode;

+ (_Nullable instancetype)fromJSON:(NSString *)json encoding:(NSStringEncoding)encoding error:(NSError *_Nullable *)error;
+ (instancetype)fromJSONDictionary:(NSDictionary *)dict;
+ (_Nullable instancetype)fromData:(NSData *)data error:(NSError *_Nullable *)error;
- (NSString *_Nullable)toJSON:(NSStringEncoding)encoding error:(NSError *_Nullable *)error;
- (NSData *_Nullable)toData:(NSError *_Nullable *)error;
@end

@interface VLPlayData : NSObject
@property (nonatomic, nullable, strong) VLPlayPlayer *player;
@end

@interface VLPlayPlayer : NSObject
@property (nonatomic, nullable, copy)   NSString *theID;
@property (nonatomic, nullable, copy)   NSString *dob;
@property (nonatomic, nullable, copy)   NSString *email;
@property (nonatomic, nullable, copy)   NSString *fullName;
@property (nonatomic, nullable, copy)   NSString *identifier;
@property (nonatomic, nullable, copy)   NSString *identityCard;
@property (nonatomic, nullable, strong) NSNumber *identityVerified;   // BE durable flag (PR #94) — authoritative "has verified"
@property (nonatomic, nullable, strong) NSNumber *isBanned;
@property (nonatomic, nullable, strong) NSNumber *isUpdatePassword;
@property (nonatomic, nullable, copy)   NSString *issueDate;
@property (nonatomic, nullable, copy)   NSString *phone;
@property (nonatomic, nullable, copy)   NSString *placeOfGrant;
@property (nonatomic, nullable, copy)   NSString *address;
@property (nonatomic, nullable, copy)   NSString *username;



@end

NS_ASSUME_NONNULL_END
