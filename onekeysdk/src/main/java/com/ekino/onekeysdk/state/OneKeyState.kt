package com.ekino.onekeysdk.state

import androidx.appcompat.app.AppCompatActivity
import com.ekino.onekeysdk.error.OneKeyException
import com.ekino.onekeysdk.model.config.OneKeyCustomObject

interface OneKeyState {
    fun setApiKey(apiKey:String):OneKeyState
    fun setAppName(appName: String): OneKeyState
    fun getAppName(): String
    fun setAppDownloadLink(downloadLink: String): OneKeyState
    fun getAppDownloadLink(): String
    fun init(customObject: OneKeyCustomObject)
    fun getConfiguration(): OneKeyCustomObject

    /**
     * startOneKeySDKFragment is used to embed OneKeySDK into your Activity.
     * @param activity is the context where OneKeySDK screens will be running on.
     * @param containerId the area in layout where fragment runs on.
     * @exception [OneKeyException] if data is invalid.
     */
    @Throws(OneKeyException::class)
    fun startOneKeySDKFragment(activity: AppCompatActivity?, containerId: Int)

    @Throws(OneKeyException::class)
    fun startOneKeySDKActivity(activity: AppCompatActivity?)
}