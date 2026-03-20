#import "FileAccess.h"

#if __has_include(<ReactNativeFileAccess/ReactNativeFileAccess-Swift.h>)
#import <ReactNativeFileAccess/ReactNativeFileAccess-Swift.h>
#else
#import "ReactNativeFileAccess-Swift.h"
#endif

@implementation FileAccess
FileAccessImpl *impl;

- (instancetype)init
{
    if (self = [super init]) {
        impl = [[FileAccessImpl alloc] init];
    }
    return self;
}

- (void)appendFile:(NSString *)path data:(NSString *)data encoding:(NSString *)encoding resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl appendFile:path withData:data withEncoding:encoding withResolver:resolve withRejecter:reject];
}

- (void)cancelFetch:(NSInteger)requestId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl cancelFetch:requestId withResolver:resolve withRejecter:reject];
}

- (void)concatFiles:(NSString *)source target:(NSString *)target resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl concatFiles:source withTarget:target withResolver:resolve withRejecter:reject];
}

- (void)cp:(NSString *)source target:(NSString *)target resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl cp:source withTarget:target withResolver:resolve withRejecter:reject];
}

// 'type' ignored on iOS.
- (void)cpAsset:(NSString *)asset target:(NSString *)target type:(NSString *)type resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl cpAsset:asset withTarget:target withResolver:resolve withRejecter:reject];
}

- (void)cpExternal:(NSString *)source targetName:(NSString *)targetName dir:(NSString *)dir resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl cpExternal:source withTargetName:targetName withDir:dir withResolver:resolve withRejecter:reject];
}

- (void)df:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl df:resolve withRejecter:reject];
}

- (void)exists:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl exists:path withResolver:resolve withRejecter:reject];
}

- (void)fetch:(NSInteger)requestId resource:(NSString *)resource init:(JS::NativeFileAccess::FetchInit &)init
{
    NSDictionary *headers = nil;
    if ([init.headers() isKindOfClass:[NSDictionary class]]) {
        headers = (NSDictionary *)init.headers();
    }
    [impl fetchWithRequestId:requestId
                    resource:resource
                        body:init.body()
                     headers:headers
                      method:init.method()
                     network:init.network()
                        path:init.path()
              emitOnProgress:^(NSDictionary *event) { [self emitOnFetchProgress:event]; }
                 emitOnError:^(NSDictionary *event) { [self emitOnFetchError:event]; }
              emitOnComplete:^(NSDictionary *event) { [self emitOnFetchComplete:event]; }];
}

- (void)getAppGroupDir:(NSString *)groupName resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl getAppGroupDir:groupName withResolver:resolve withRejecter:reject];
}

- (void)hardlink:(NSString *)source target:(NSString *)target resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl hardlink:source withTarget:target withResolver:resolve withRejecter:reject];
}

- (void)hash:(NSString *)path algorithm:(NSString *)algorithm resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl hash:path withAlgorithm:algorithm withResolver:resolve withRejecter:reject];
}

- (void)isDir:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl isDir:path withResolver:resolve withRejecter:reject];
}

- (void)ls:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl ls:path withResolver:resolve withRejecter:reject];
}

- (void)mkdir:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl mkdir:path withResolver:resolve withRejecter:reject];
}

- (void)mv:(NSString *)source target:(NSString *)target resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl mv:source withTarget:target withResolver:resolve withRejecter:reject];
}

- (void)readFile:(NSString *)path encoding:(NSString *)encoding resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl readFile:path withEncoding:encoding withResolver:resolve withRejecter:reject];
}

- (void)readFileChunk:(NSString *)path offset:(NSInteger)offset length:(NSInteger)length encoding:(NSString *)encoding resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl readFileChunk:path withOffset:offset withLength:length withEncoding:encoding withResolver:resolve withRejecter:reject];
}

- (void)stat:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl stat:path withResolver:resolve withRejecter:reject];
}

- (void)statDir:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl statDir:path withResolver:resolve withRejecter:reject];
}

- (void)symlink:(NSString *)source target:(NSString *)target resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl symlink:source withTarget:target withResolver:resolve withRejecter:reject];
}

- (void)unlink:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl unlink:path withResolver:resolve withRejecter:reject];
}

- (void)unzip:(NSString *)source target:(NSString *)target resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl unzip:source withTarget:target withResolver:resolve withRejecter:reject];
}

- (void)writeFile:(NSString *)path data:(NSString *)data encoding:(NSString *)encoding resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    [impl writeFile:path withData:data withEncoding:encoding withResolver:resolve withRejecter:reject];
}

- (facebook::react::ModuleConstants<JS::NativeFileAccess::Constants::Builder>)constantsToExport
{
    return [self getConstants];
}

- (facebook::react::ModuleConstants<JS::NativeFileAccess::Constants::Builder>)getConstants
{
    return facebook::react::typedConstants<JS::NativeFileAccess::Constants::Builder>({
        .CacheDir = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject],
        .DocumentDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject],
        .LibraryDir = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) firstObject],
        .MainBundleDir = [[NSBundle mainBundle] bundlePath],
    });
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeFileAccessSpecJSI>(params);
}

+ (NSString *)moduleName
{
    return @"FileAccess";
}

@end
