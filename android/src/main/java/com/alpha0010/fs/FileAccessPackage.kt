package com.alpha0010.fs

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider


class FileAccessPackage : BaseReactPackage() {
    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? =
        if (name == FileAccessModule.NAME) {
            FileAccessModule(reactContext)
        } else {
            null
        }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
            val isTurboModule: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            moduleInfos[FileAccessModule.NAME] = ReactModuleInfo(
                FileAccessModule.NAME, // name
                FileAccessModule.NAME, // className
                false,  // canOverrideExistingModule
                false,  // needsEagerInit
                false, // hasConstants NOTE: This is deprecated but we need it to keep compatability with RN <= 0.72
                false,  // isCxxModule
                isTurboModule // isTurboModule
            )
            moduleInfos
        }
    }
}
