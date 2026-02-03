package com.my.applist

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.opera.ads.AdError
import com.opera.ads.OperaAds
import com.opera.ads.initialization.OnSdkInitCompleteListener
import com.opera.ads.initialization.SdkInitConfig



/**
 * Application 全局类
 * 用于全局初始化和管理应用生命周期
 */
class APP : Application() {

    companion object {
        private const val TAG = "APP"
        
        // 全局Context
        private lateinit var instance: APP
        
        /**
         * 获取全局Application实例
         */
        fun getInstance(): APP {
            return instance
        }
        
        /**
         * 获取全局Context
         */
        fun getContext(): Context {
            return instance.applicationContext
        }

        /**
         * APP ID
         */
        private const val APPLICATION_ID = "pub13423013211200/ep13423013211584/app13423536670400"

        /**
         * 运营商
         */
        private const val PUBLISHER = "Opera"

        /**
         * 是否是COPPA合规
         */
        private const val COPPA = 1

        /**
         * US 隐私协议
         */
        private const val US_PRIVACY = "1YNY"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.d(TAG, "Application onCreate")
        
        // 初始化各个组件
        initOperaAds()
        initOtherComponents()
    }

    /**
     * 初始化 Opera Ads SDK
     */
    private fun initOperaAds() {
        try {
            // Opera Ads SDK 初始化
            Log.d(TAG, "Opera Ads SDK 初始化参数: APPLICATION_ID = ${APPLICATION_ID} PUBLISHER = ${PUBLISHER} COPPA = ${COPPA} US_PRIVACY = ${US_PRIVACY}")
            OperaAds.initialize(getInstance(),
                SdkInitConfig.Builder(APPLICATION_ID)
                    .publisherName(PUBLISHER)
                    .coppa(COPPA)
                    .usPrivacy(US_PRIVACY)
                    .build(),
                object : OnSdkInitCompleteListener{
                    override fun onSuccess() {
                        Toast.makeText(getContext(), "Opera Ads SDK 初始化成功", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Opera Ads SDK 初始化成功")
                    }

                    override fun onError(error: AdError) {
                        Toast.makeText(getContext(), "Opera Ads SDK 初始化失败", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Opera Ads SDK 初始化失败${error.message}")
                    }
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Opera Ads SDK 初始化失败: ${e.message}", e)
        }
    }

    /**
     * 初始化其他组件
     */
    private fun initOtherComponents() {
        try {
            // 可以在这里初始化其他第三方SDK或工具
            // 例如：崩溃分析、统计分析、日志框架等
            AppsFlyerLib.getInstance().init("n77Xcr82ht63tJadSR24ob",null, this)
            AppsFlyerLib.getInstance().start(this,"",object : AppsFlyerRequestListener{
                override fun onSuccess() {
                    Log.e(TAG, "Launch sent successfully, got 200 response code from server")
                }

                override fun onError(i: Int, s: String) {
                    Log.e(TAG, ("Launch failed to be sent:\n" +
                            "Error code: " + i + "\n"
                            + "Error description: " + s) )
                }
            })
            // 设置全局异常处理
            setupGlobalExceptionHandler()
            
            Log.d(TAG, "其他组件初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "组件初始化失败: ${e.message}", e)
        }
    }

    /**
     * 设置全局异常处理
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "未捕获的异常 in thread ${thread.name}", throwable)
            
            // 保存崩溃日志到本地
            saveCrashLog(throwable)
            
            // 调用系统默认的异常处理
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * 保存崩溃日志
     */
    private fun saveCrashLog(throwable: Throwable) {
        try {
            val logDir = getExternalFilesDir("crash_logs")
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs()
            }
            
            val logFile = java.io.File(logDir, "crash_${System.currentTimeMillis()}.txt")
            val writer = logFile.bufferedWriter()
            
            writer.write("崩溃时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            writer.write("应用版本: ${packageManager.getPackageInfo(packageName, 0).versionName}\n")
            writer.write("Android 版本: ${android.os.Build.VERSION.RELEASE}\n")
            writer.write("设备型号: ${android.os.Build.MODEL}\n")
            writer.write("制造商: ${android.os.Build.MANUFACTURER}\n")
            writer.write("\n异常信息:\n")
            writer.write(Log.getStackTraceString(throwable))
            
            writer.close()
            
            Log.d(TAG, "崩溃日志已保存: ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "保存崩溃日志失败: ${e.message}", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application onTerminate")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Application onLowMemory - 系统内存不足")
        // 可以在这里释放一些缓存资源
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.w(TAG, "Application onTrimMemory - level: $level")
        
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                // UI不可见，可以释放UI资源
                Log.d(TAG, "UI已隐藏，可以释放UI相关资源")
            }
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // 应用正在运行但系统内存紧张
                Log.d(TAG, "系统内存紧张，建议释放缓存")
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // 应用在后台且系统内存紧张
                Log.d(TAG, "应用在后台且系统内存紧张，释放所有可释放资源")
            }
        }
    }
}

