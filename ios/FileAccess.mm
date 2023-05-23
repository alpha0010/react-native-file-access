#import "FileAccess.h"
#import <ReactNativeFileAccess-Swift.h>

@implementation FileAccess
FileAccessImpl *impl;

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

- (instancetype)init
{
    if (self = [super init]) {
        impl = [[FileAccessImpl alloc] init];
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents
{
    return [impl supportedEvents];
}

RCT_EXPORT_METHOD(appendFile:(NSString *)path
                  data:(NSString *)data
                  encoding:(NSString *)encoding
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl appendFile:path withData:data withEncoding:encoding withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(cancelFetch:(double)requestId
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSNumber *reqId = [NSNumber numberWithInt:requestId];
    [impl cancelFetch:reqId withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(concatFiles:(NSString *)source
                  target:(NSString *)target
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl concatFiles:source withTarget:target withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(cp:(NSString *)source
                  target:(NSString *)target
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl cp:source withTarget:target withResolver:resolve withRejecter:reject];
}

// 'type' ignored on iOS.
RCT_EXPORT_METHOD(cpAsset:(NSString *)asset
                  target:(NSString *)target
                  type:(NSString *)type
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl cpAsset:asset withTarget:target withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(cpExternal:(NSString *)source
                  targetName:(NSString *)targetName
                  dir:(NSString *)dir
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl cpExternal:source withTargetName:targetName withDir:dir withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(df:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl df:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(exists:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl exists:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(getAppGroupDir:(NSString *)groupName
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl getAppGroupDir:groupName withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(hash:(NSString *)path
                  algorithm:(NSString *)algorithm
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl hash:path withAlgorithm:algorithm withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(isDir:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl isDir:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(ls:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl ls:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(mkdir:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl mkdir:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(mv:(NSString *)source
                  target:(NSString *)target
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl mv:source withTarget:target withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(readFile:(NSString *)path
                  encoding:(NSString *)encoding
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl readFile:path withEncoding:encoding withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(stat:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl stat:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(statDir:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl statDir:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(unlink:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl unlink:path withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(unzip:(NSString *)source
                  target:(NSString *)target
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl unzip:source withTarget:target withResolver:resolve withRejecter:reject];
}

RCT_EXPORT_METHOD(writeFile:(NSString *)path
                  data:(NSString *)data
                  encoding:(NSString *)encoding
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [impl writeFile:path withData:data withEncoding:encoding withResolver:resolve withRejecter:reject];
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
RCT_EXPORT_METHOD(fetch:(double)requestId
                  resource:(NSString *)resource
                  init:(JS::NativeFileAccess::SpecFetchInit &)init)
{
    NSNumber *reqId = [NSNumber numberWithInt:requestId];
    // TODO: Avoid type safety bypass.
    NSMutableDictionary *config = [NSMutableDictionary new];
    if (init.body()) { [config setObject:init.body() forKey:@"body"]; }
    if (init.headers()) { [config setObject:init.headers() forKey:@"headers"]; }
    if (init.method()) { [config setObject:init.method() forKey:@"method"]; }
    if (init.network()) { [config setObject:init.network() forKey:@"network"]; }
    if (init.path()) { [config setObject:init.path() forKey:@"path"]; }
    [impl fetch:reqId withResource:resource withConfig:config withEmitter:self];
}

- (facebook::react::ModuleConstants<JS::NativeFileAccess::Constants::Builder>)constantsToExport
{
    return [self getConstants];
}

- (facebook::react::ModuleConstants<JS::NativeFileAccess::Constants::Builder>)getConstants
{
    // TODO: Avoid type safety bypass.
    NSDictionary *constants = [impl constantsToExport];
    return facebook::react::typedConstants<JS::NativeFileAccess::Constants::Builder>({
        .CacheDir = constants[@"CacheDir"],
        .DocumentDir = constants[@"DocumentDir"],
        .LibraryDir = constants[@"LibraryDir"],
        .MainBundleDir = constants[@"MainBundleDir"]
    });
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeFileAccessSpecJSI>(params);
}
#else
RCT_EXPORT_METHOD(fetch:(double)requestId
                  resource:(NSString *)resource
                  init:(NSDictionary *)init)
{
    NSNumber *reqId = [NSNumber numberWithInt:requestId];
    [impl fetch:reqId withResource:resource withConfig:init withEmitter:self];
}

- (NSDictionary *)constantsToExport
{
    return [impl constantsToExport];
}
#endif

@end
