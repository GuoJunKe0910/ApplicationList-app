package com.my.applist.ad.opera

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.my.applist.R
import com.opera.ads.AdError
import com.opera.ads.AdSize
import com.opera.ads.RewardItem
import com.opera.ads.banner.BannerAd
import com.opera.ads.interstitial.InterstitialAd
import com.opera.ads.nativead.MediaView
import com.opera.ads.nativead.NativeAd
import com.opera.ads.rewarded.RewardedAd

/**
 * Opera 广告使用示例
 * 展示如何使用 OperaAdManager 加载和显示各种类型的广告
 */
class OperaAdUsageExample(private val context: Context) {
    
    // 缓存已加载的广告
    private var cachedNativeAd: NativeAd? = null
    private var cachedInterstitialAd: InterstitialAd? = null
    private var cachedInterstitialVideoAd: InterstitialAd? = null
    private var cachedRewardedAd: RewardedAd? = null
    
    // ======================== 原生广告示例 ========================
    
    /**
     * 加载原生广告示例
     */
    fun loadNativeAdExample() {
        OperaAdManager.loadNativeAd(
            context = context,
            adUnitId = OperaAdConfig.nativeAdUnitId,
            listener = object : OperaAdManager.NativeAdLoadListener {
                override fun onAdLoaded(nativeAd: NativeAd) {
                    cachedNativeAd = nativeAd
                    Toast.makeText(context, "原生广告加载成功，点击'显示原生广告'查看", Toast.LENGTH_LONG).show()
                }
                
                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "原生广告加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdImpression() {
                    // 广告曝光
                    Toast.makeText(context, "广告曝光", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdClicked() {
                    // 广告被点击
                    Toast.makeText(context, "广告被点击", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    /**
     * 渲染原生广告示例
     */
    fun renderNativeAdExample(
        nativeAd: NativeAd,
        nativeAdRootView: FrameLayout,
        nativeAdMedia: MediaView,
        nativeAdTitle: android.widget.TextView,
        nativeAdBody: android.widget.TextView,
        nativeAdCallToAction: android.widget.Button,
        nativeAdIcon: android.widget.ImageView
    ) {
        if (!nativeAd.isAdInvalidated()) {
            // 设置 AdChoice 图标位置
            nativeAd.setAdChoicePosition(NativeAd.AdChoicePosition.TOP_RIGHT)
            
            // 绑定交互视图
            val interactionViews = NativeAd.InteractionViews.Builder(nativeAdMedia)
                .setTitleView(nativeAdTitle)
                .setBodyView(nativeAdBody)
                .setCallToActionView(nativeAdCallToAction)
                .setIconView(nativeAdIcon)
                .build()
            
            // 注册交互视图
            nativeAd.registerInteractionViews(nativeAdRootView, interactionViews)
        }
    }
    
    /**
     * 显示原生广告示例（简化版，只需要传入容器）
     */
    fun showNativeAdExample(container: FrameLayout) {
        val nativeAd = cachedNativeAd
        if (nativeAd == null) {
            Toast.makeText(context, "请先加载原生广告", Toast.LENGTH_SHORT).show()
            return
        }

        if (nativeAd.isAdInvalidated()) {
            Toast.makeText(context, "原生广告已失效，请重新加载", Toast.LENGTH_SHORT).show()
            return
        }

        // 清空容器
        container.removeAllViews()

        // 加载原生广告布局
        val nativeAdView = LayoutInflater.from(context).inflate(
            R.layout.layout_native_ad,
            container,
            false
        )

        // 获取布局中的各个视图
        val nativeAdRootView = nativeAdView.findViewById<FrameLayout>(R.id.nativeAdRootView)
        val nativeAdMedia = nativeAdView.findViewById<MediaView>(R.id.nativeAdMedia)
        val nativeAdTitle = nativeAdView.findViewById<TextView>(R.id.nativeAdTitle)
        val nativeAdBody = nativeAdView.findViewById<TextView>(R.id.nativeAdBody)
        val nativeAdCallToAction = nativeAdView.findViewById<Button>(R.id.nativeAdCallToAction)
        val nativeAdIcon = nativeAdView.findViewById<ImageView>(R.id.nativeAdIcon)

        // 设置 AdChoice 图标位置
        nativeAd.setAdChoicePosition(NativeAd.AdChoicePosition.TOP_RIGHT)

        // 构建交互视图并注册
        val interactionViews = NativeAd.InteractionViews.Builder(nativeAdMedia)
            .setTitleView(nativeAdTitle)
            .setBodyView(nativeAdBody)
            .setCallToActionView(nativeAdCallToAction)
            .setIconView(nativeAdIcon)
            .build()

        // 注册交互视图
        nativeAd.registerInteractionViews(nativeAdRootView, interactionViews)

        // 添加到容器
        container.addView(nativeAdView)

        Toast.makeText(context, "原生广告已显示", Toast.LENGTH_SHORT).show()
    }
    
    // ======================== 横幅广告示例 ========================
    
    /**
     * 加载横幅广告示例（标准横幅 320x50）
     */
    fun loadBannerAdExample(container: ViewGroup) {
        OperaAdManager.loadBannerAd(
            context = context,
            adUnitId = OperaAdConfig.bannerAdUnitId,
            adSize = AdSize.BANNER,  // 320x50
            container = container,  // 自动添加到容器中
            listener = object : OperaAdManager.BannerAdLoadListener {
                override fun onAdLoaded(bannerAd: BannerAd) {
                    Toast.makeText(context, "横幅广告加载成功", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "横幅广告加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    /**
     * 加载中矩形横幅广告示例（300x250）
     */
    fun loadBannerMrecExample(container: FrameLayout) {
        OperaAdManager.loadBannerAd(
            context = context,
            adUnitId = OperaAdConfig.bannerVideoAdUnitId,
            adSize = AdSize.BANNER_MREC,  // 300x250
            container = container,
            listener = object : OperaAdManager.BannerAdLoadListener {
                override fun onAdLoaded(bannerAd: BannerAd) {
                    Toast.makeText(context, "中矩形广告加载成功", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "中矩形广告加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    // ======================== 插页式广告示例 ========================
    
    /**
     * 加载插页式广告示例
     */
    fun loadInterstitialAdExample() {
        OperaAdManager.loadInterstitialAd(
            context = context,
            adUnitId = OperaAdConfig.interstitialAdUnitId,
            listener = object : OperaAdManager.InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    cachedInterstitialAd = ad
                    Toast.makeText(context, "插页式广告加载成功", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "插页式广告加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    /**
     * 显示插页式广告示例
     */
    fun showInterstitialAdExample() {
        val ad = cachedInterstitialAd
        if (ad == null) {
            Toast.makeText(context, "插页式广告未加载", Toast.LENGTH_SHORT).show()
            return
        }
        
        OperaAdManager.showInterstitialAd(
            context = context,
            interstitialAd = ad,
            listener = object : OperaAdManager.InterstitialAdShowListener {
                override fun onAdDisplayed() {
                    // 广告已显示
                    Toast.makeText(context, "广告已显示", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdClicked() {
                    // 广告被点击
                    Toast.makeText(context, "广告被点击", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdDismissed() {
                    // 广告已关闭，可以预加载下一个
                    cachedInterstitialAd = null
//                    loadInterstitialAdExample()
                }

                override fun onAdFailed(errorMessage: String) {
                    Toast.makeText(context, "插页式广告显示失败: ${errorMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * 加载插页式广告示例（视频）
     */
    fun loadInterstitialVideoAdExample() {
        OperaAdManager.loadInterstitialAd(
            context = context,
            adUnitId = OperaAdConfig.interstitialVideoAdUnitId,
            listener = object : OperaAdManager.InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    cachedInterstitialVideoAd = ad
                    Toast.makeText(context, "插页式(视频)广告加载成功", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "插页式(视频)广告加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * 显示插页式广告示例(视频)
     */
    fun showInterstitialVideoAdExample() {
        val ad = cachedInterstitialVideoAd
        if (ad == null) {
            Toast.makeText(context, "插页式(视频)广告未加载", Toast.LENGTH_SHORT).show()
            return
        }

        OperaAdManager.showInterstitialAd(
            context = context,
            interstitialAd = ad,
            listener = object : OperaAdManager.InterstitialAdShowListener {
                override fun onAdDisplayed() {
                    // 广告已显示
                    Toast.makeText(context, "(视频)广告已显示", Toast.LENGTH_SHORT).show()
                }

                override fun onAdClicked() {
                    // 广告被点击
                    Toast.makeText(context, "(视频)广告被点击", Toast.LENGTH_SHORT).show()
                }

                override fun onAdDismissed() {
                    // 广告已关闭，可以预加载下一个
                    cachedInterstitialVideoAd = null
//                    loadInterstitialAdExample()
                }

                override fun onAdFailed(errorMessage: String) {
                    Toast.makeText(context, "插页式(视频)广告显示失败: ${errorMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    // ======================== 激励视频示例 ========================
    
    /**
     * 加载激励视频广告示例
     */
    fun loadRewardedAdExample() {
        OperaAdManager.loadRewardedAd(
            context = context,
            adUnitId = OperaAdConfig.rewardedAdUnitId,
            listener = object : OperaAdManager.RewardedAdLoadListener {
                override fun onAdLoaded(ad: RewardedAd) {
                    cachedRewardedAd = ad
                    Toast.makeText(context, "激励视频加载成功", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "激励视频加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    /**
     * 显示激励视频广告示例
     */
    fun showRewardedAdExample() {
        val ad = cachedRewardedAd
        if (ad == null) {
            Toast.makeText(context, "激励视频未加载", Toast.LENGTH_SHORT).show()
            return
        }
        
        OperaAdManager.showRewardedAd(
            context = context,
            rewardedAd = ad,
            listener = object : OperaAdManager.RewardedAdShowListener {
                override fun onAdDisplayed() {
                    // 激励视频开始播放
                    Toast.makeText(context, "激励视频开始播放", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdClicked() {
                    // 激励视频被点击
                    Toast.makeText(context, "激励视频被点击", Toast.LENGTH_SHORT).show()
                }
                
                override fun onAdDismissed() {
                    // 激励视频关闭，可以预加载下一个
                    cachedRewardedAd = null
                    loadRewardedAdExample()
                }
                
                override fun onRewarded(reward: RewardItem) {
                    // 用户获得奖励
                    Toast.makeText(
                        context,
                        "恭喜获得奖励：${reward.type} x ${reward.amount}",
                        Toast.LENGTH_LONG
                    ).show()
                    // 在这里发放奖励给用户
                }
                
                override fun onAdFailed(error: AdError) {
                    Toast.makeText(context, "激励视频显示失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    // ======================== 资源释放 ========================
    
    /**
     * 释放所有广告资源
     * 在 Activity/Fragment 销毁时调用
     */
    fun destroy() {
        cachedNativeAd?.destroy()
        cachedNativeAd = null
        
        cachedInterstitialAd?.destroy()
        cachedInterstitialAd = null

        cachedInterstitialVideoAd?.destroy()
        cachedInterstitialVideoAd = null

        cachedRewardedAd?.destroy()
        cachedRewardedAd = null
    }
}

