#import <React/RCTEventEmitter.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNFileAccessSpec.h"

@interface FileAccess : RCTEventEmitter <NativeFileAccessSpec>
#else
#import <React/RCTBridgeModule.h>

@interface FileAccess : RCTEventEmitter <RCTBridgeModule>
#endif

@end
