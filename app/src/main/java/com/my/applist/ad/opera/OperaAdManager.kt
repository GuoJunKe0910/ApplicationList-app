package com.my.applist.ad.opera

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.opera.ads.AdError
import com.opera.ads.AdSize
import com.opera.ads.RewardItem
import com.opera.ads.banner.BannerAd
import com.opera.ads.banner.BannerAdListener
import com.opera.ads.banner.BannerAdView
import com.opera.ads.interstitial.InterstitialAd
import com.opera.ads.interstitial.InterstitialAdInteractionListener
import com.opera.ads.interstitial.InterstitialAdLoadListener
import com.opera.ads.nativead.NativeAd
import com.opera.ads.nativead.NativeAdListener
import com.opera.ads.nativead.NativeAdLoader
import com.opera.ads.rewarded.RewardedAd
import com.opera.ads.rewarded.RewardedAdInteractionListener
import com.opera.ads.rewarded.RewardedAdLoadListener

/**
 * Opera 广告管理工具类
 * 提供原生广告、横幅广告、插页式广告、激励视频的加载和显示功能
 */
object OperaAdManager {
    
    private const val TAG = "OperaAdManager"
    
    // ======================== 原生广告 ========================
    
    /**
     * 加载原生广告
     * @param context 上下文
     * @param adUnitId 广告单元ID
     * @param listener 加载监听器
     */
    fun loadNativeAd(
        context: Context,
        adUnitId: String,
        listener: NativeAdLoadListener
    ) {
        Log.d(TAG, "开始加载原生广告: $adUnitId")
        
        NativeAdLoader.loadAd(context, adUnitId, object : NativeAdListener {
            override fun onAdLoaded(nativeAd: NativeAd) {
                Log.d(TAG, "原生广告加载成功")
                listener.onAdLoaded(nativeAd)
            }
            
            override fun onAdFailedToLoad(error: AdError) {
                Log.e(TAG, "原生广告加载失败: ${error.message}")
                listener.onAdFailed(error)
            }
            
            override fun onAdImpression() {
                Log.d(TAG, "原生广告曝光")
                listener.onAdImpression()
            }
            
            override fun onAdClicked() {
                Log.d(TAG, "原生广告被点击")
                listener.onAdClicked()
            }
        })
    }
    
    /**
     * 原生广告加载监听器
     */
    interface NativeAdLoadListener {
        fun onAdLoaded(nativeAd: NativeAd)
        fun onAdFailed(error: AdError)
        fun onAdImpression() {}
        fun onAdClicked() {}
    }
    
    // ======================== 横幅广告 ========================
    
    /**
     * 创建并加载横幅广告
     * @param context 上下文
     * @param adUnitId 广告单元ID
     * @param adSize 广告尺寸，默认为 BANNER_MREC (300x250)
     * @param container 广告容器（可选），如果提供则自动添加到容器中
     * @param listener 加载监听器
     * @return BannerAdView 实例
     */
    fun loadBannerAd(
        context: Context,
        adUnitId: String,
        adSize: AdSize = AdSize.BANNER_MREC,
        container: ViewGroup? = null,
        listener: BannerAdLoadListener? = null
    ): BannerAdView {
        Log.d(TAG, "开始加载横幅广告: $adUnitId, 尺寸: $adSize")
        
        val bannerAdView = BannerAdView(context)
        bannerAdView.placementId = adUnitId
        bannerAdView.adSize = adSize
        
        val adListener = object : BannerAdListener {
            override fun onAdLoaded(bannerAd: BannerAd) {
                Log.d(TAG, "横幅广告加载成功, 刷新次数: ${bannerAd.refreshCount}")
                listener?.onAdLoaded(bannerAd)
            }
            
            override fun onAdFailedToLoad(error: AdError) {
                Log.e(TAG, "横幅广告加载失败: ${error.message}")
                listener?.onAdFailed(error)
            }
            
            override fun onAdImpression() {
                Log.d(TAG, "横幅广告曝光")
                listener?.onAdImpression()
            }
            
            override fun onAdClicked() {
                Log.d(TAG, "横幅广告被点击")
                listener?.onAdClicked()
            }
        }
        
        bannerAdView.loadAd(adListener)
        
        // 如果提供了容器，自动添加到容器中
        container?.addView(bannerAdView)
        
        return bannerAdView
    }
    
    /**
     * 横幅广告加载监听器
     */
    interface BannerAdLoadListener {
        fun onAdLoaded(bannerAd: BannerAd)
        fun onAdFailed(error: AdError)
        fun onAdImpression() {}
        fun onAdClicked() {}
    }
    
    // ======================== 插页式广告 ========================
    
    /**
     * 加载插页式广告
     * @param context 上下文
     * @param adUnitId 广告单元ID
     * @param listener 加载监听器
     */
    fun loadInterstitialAd(
        context: Context,
        adUnitId: String,
        listener: InterstitialAdLoadListener
    ) {
        Log.d(TAG, "开始加载插页式广告: $adUnitId")
        
        InterstitialAd.load(context, adUnitId, object : com.opera.ads.interstitial.InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "插页式广告加载成功")
                listener.onAdLoaded(ad)
            }
            
            override fun onAdFailedToLoad(error: AdError) {
                Log.e(TAG, "插页式广告加载失败: ${error.message}")
                listener.onAdFailed(error)
            }
        })
    }
    
    /**
     * 显示插页式广告
     * @param context 上下文
     * @param interstitialAd 已加载的插页式广告
     * @param listener 交互监听器
     */
    fun showInterstitialAd(
        context: Context,
        interstitialAd: InterstitialAd,
        listener: InterstitialAdShowListener? = null
    ) {
        if (interstitialAd.isAdInvalidated()) {
            Log.w(TAG, "插页式广告已失效，无法显示")
            listener?.onAdFailed("广告已失效")
            return
        }
        
        Log.d(TAG, "显示插页式广告")
        
        interstitialAd.show(context, object : InterstitialAdInteractionListener {
            override fun onAdClicked() {
                Log.d(TAG, "插页式广告被点击")
                listener?.onAdClicked()
            }
            
            override fun onAdDisplayed() {
                Log.d(TAG, "插页式广告已显示")
                listener?.onAdDisplayed()
            }
            
            override fun onAdDismissed() {
                Log.d(TAG, "插页式广告已关闭")
                listener?.onAdDismissed()
            }
            
            override fun onAdFailedToShow(error: AdError) {
                Log.e(TAG, "插页式广告显示失败: ${error.message}")
                listener?.onAdFailed(error.message ?: "未知错误")
            }
        })
    }
    
    /**
     * 插页式广告加载监听器
     */
    interface InterstitialAdLoadListener {
        fun onAdLoaded(ad: InterstitialAd)
        fun onAdFailed(error: AdError)
    }
    
    /**
     * 插页式广告显示监听器
     */
    interface InterstitialAdShowListener {
        fun onAdDisplayed() {}
        fun onAdClicked() {}
        fun onAdDismissed() {}
        fun onAdFailed(errorMessage: String)
    }
    
    // ======================== 激励视频 ========================
    
    /**
     * 加载激励视频广告
     * @param context 上下文
     * @param adUnitId 广告单元ID
     * @param listener 加载监听器
     */
    fun loadRewardedAd(
        context: Context,
        adUnitId: String,
        listener: RewardedAdLoadListener
    ) {
        Log.d(TAG, "开始加载激励视频: $adUnitId")
        
        RewardedAd.load(context, adUnitId, object : com.opera.ads.rewarded.RewardedAdLoadListener {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "激励视频加载成功")
                listener.onAdLoaded(ad)
            }
            
            override fun onAdFailedToLoad(error: AdError) {
                Log.e(TAG, "激励视频加载失败: ${error.message}")
                listener.onAdFailed(error)
            }
        })
    }
    
    /**
     * 显示激励视频广告
     * @param context 上下文
     * @param rewardedAd 已加载的激励视频广告
     * @param listener 交互监听器
     */
    fun showRewardedAd(
        context: Context,
        rewardedAd: RewardedAd,
        listener: RewardedAdShowListener
    ) {
        if (rewardedAd.isAdInvalidated()) {
            Log.w(TAG, "激励视频已失效，无法显示")
            return
        }
        
        Log.d(TAG, "显示激励视频")
        
        rewardedAd.show(context, object : RewardedAdInteractionListener {
            override fun onAdClicked() {
                Log.d(TAG, "激励视频被点击")
                listener.onAdClicked()
            }
            
            override fun onAdDisplayed() {
                Log.d(TAG, "激励视频已显示")
                listener.onAdDisplayed()
            }
            
            override fun onAdDismissed() {
                Log.d(TAG, "激励视频已关闭")
                listener.onAdDismissed()
            }
            
            override fun onAdFailedToShow(error: AdError) {
                Log.e(TAG, "激励视频显示失败: ${error.message}")
                listener.onAdFailed(error)
            }
            
            override fun onUserRewarded(reward: RewardItem) {
                Log.d(TAG, "用户获得奖励: ${reward.type}, 数量: ${reward.amount}")
                listener.onRewarded(reward)
            }
        })
    }
    
    /**
     * 激励视频加载监听器
     */
    interface RewardedAdLoadListener {
        fun onAdLoaded(ad: RewardedAd)
        fun onAdFailed(error: AdError)
    }
    
    /**
     * 激励视频显示监听器
     */
    interface RewardedAdShowListener {
        fun onAdDisplayed() {}
        fun onAdClicked() {}
        fun onAdDismissed() {}
        fun onRewarded(reward: RewardItem)
        fun onAdFailed(error: AdError)
    }
}

