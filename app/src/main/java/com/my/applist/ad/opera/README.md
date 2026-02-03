# Opera 广告集成工具类

## 📁 文件说明

### 1. `OperaAdManager.kt`
核心广告管理类，提供四种广告类型的加载和显示功能：
- 原生广告 (Native Ad)
- 横幅广告 (Banner Ad)
- 插页式广告 (Interstitial Ad)
- 激励视频 (Rewarded Video Ad)

### 2. `OperaAdConfig.kt`
广告配置类，管理所有广告单元 ID，支持测试模式和生产模式切换。

### 3. `OperaAdUsageExample.kt`
使用示例类，展示如何使用 `OperaAdManager` 加载和显示各种广告。

---

## 🚀 快速开始

### 1. 配置广告单元 ID

在 `OperaAdConfig.kt` 中替换为你从 Opera Ads 控制台获取的真实广告单元 ID：

```kotlin
private const val PROD_NATIVE_AD_UNIT_ID = "你的原生广告ID"
private const val PROD_BANNER_AD_UNIT_ID = "你的横幅广告ID"
private const val PROD_INTERSTITIAL_AD_UNIT_ID = "你的插页式广告ID"
private const val PROD_REWARDED_AD_UNIT_ID = "你的激励视频ID"
```

### 2. 在 Application 中初始化

在 `APP.kt` 的 `initOperaAds()` 方法中添加初始化代码（根据 Opera Ads SDK 官方文档）。

### 3. 使用广告

#### 加载横幅广告（最简单）

```kotlin
// 在 Activity 中
val bannerContainer = findViewById<FrameLayout>(R.id.bannerContainer)

OperaAdManager.loadBannerAd(
    context = this,
    adUnitId = OperaAdConfig.bannerAdUnitId,
    adSize = AdSize.BANNER,  // 320x50
    container = bannerContainer,
    listener = object : OperaAdManager.BannerAdLoadListener {
        override fun onAdLoaded(bannerAd: BannerAd) {
            Log.d("Ad", "横幅广告加载成功")
        }
        
        override fun onAdFailed(error: AdError) {
            Log.e("Ad", "横幅广告加载失败: ${error.message}")
        }
    }
)
```

#### 加载并显示插页式广告

```kotlin
// 1. 先加载广告
var interstitialAd: InterstitialAd? = null

OperaAdManager.loadInterstitialAd(
    context = this,
    adUnitId = OperaAdConfig.interstitialAdUnitId,
    listener = object : OperaAdManager.InterstitialAdLoadListener {
        override fun onAdLoaded(ad: InterstitialAd) {
            interstitialAd = ad
        }
        
        override fun onAdFailed(error: AdError) {
            Log.e("Ad", "插页式广告加载失败")
        }
    }
)

// 2. 在适当的时机显示广告（例如游戏关卡结束）
interstitialAd?.let { ad ->
    OperaAdManager.showInterstitialAd(
        context = this,
        interstitialAd = ad,
        listener = object : OperaAdManager.InterstitialAdShowListener {
            override fun onAdDismissed() {
                // 广告关闭，继续游戏或预加载下一个广告
            }
            
            override fun onAdFailed(error: AdError) {
                Log.e("Ad", "插页式广告显示失败")
            }
        }
    )
}
```

#### 加载并显示激励视频

```kotlin
// 1. 先加载激励视频
var rewardedAd: RewardedAd? = null

OperaAdManager.loadRewardedAd(
    context = this,
    adUnitId = OperaAdConfig.rewardedAdUnitId,
    listener = object : OperaAdManager.RewardedAdLoadListener {
        override fun onAdLoaded(ad: RewardedAd) {
            rewardedAd = ad
            // 可以显示"观看视频获得奖励"按钮
        }
        
        override fun onAdFailed(error: AdError) {
            Log.e("Ad", "激励视频加载失败")
        }
    }
)

// 2. 用户点击"观看视频"按钮时显示
rewardedAd?.let { ad ->
    OperaAdManager.showRewardedAd(
        context = this,
        rewardedAd = ad,
        listener = object : OperaAdManager.RewardedAdShowListener {
            override fun onRewarded(reward: RewardItem) {
                // 发放奖励给用户
                Toast.makeText(
                    this@MainActivity,
                    "获得奖励：${reward.type} x ${reward.amount}",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            override fun onAdDismissed() {
                // 视频关闭
            }
            
            override fun onAdFailed(error: AdError) {
                Log.e("Ad", "激励视频显示失败")
            }
        }
    )
}
```

#### 加载原生广告

```kotlin
OperaAdManager.loadNativeAd(
    context = this,
    adUnitId = OperaAdConfig.nativeAdUnitId,
    listener = object : OperaAdManager.NativeAdLoadListener {
        override fun onAdLoaded(nativeAd: NativeAd) {
            // 渲染原生广告
            renderNativeAd(nativeAd)
        }
        
        override fun onAdFailed(error: AdError) {
            Log.e("Ad", "原生广告加载失败")
        }
    }
)

// 渲染原生广告
fun renderNativeAd(nativeAd: NativeAd) {
    if (!nativeAd.isAdInvalidated) {
        nativeAd.setAdChoicePosition(NativeAd.AdChoicePosition.TOP_RIGHT)
        
        val interactionViews = NativeAd.InteractionViews.Builder(mediaView)
            .setTitleView(titleView)
            .setBodyView(bodyView)
            .setCallToActionView(ctaButton)
            .setIconView(iconView)
            .build()
        
        nativeAd.registerInteractionViews(rootView, interactionViews)
    }
}
```

---

## 💡 最佳实践

### 1. 预加载广告
在需要显示广告之前提前加载，避免用户等待：

```kotlin
// 在 Activity onCreate 或 Fragment onViewCreated 中预加载
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 预加载插页式广告
    loadInterstitialAd()
    
    // 预加载激励视频
    loadRewardedAd()
}
```

### 2. 释放资源
在 Activity/Fragment 销毁时释放广告资源：

```kotlin
override fun onDestroy() {
    super.onDestroy()
    
    // 释放原生广告
    nativeAd?.destroy()
    
    // 释放插页式广告
    interstitialAd?.destroy()
    
    // 释放激励视频
    rewardedAd?.destroy()
    
    // 释放横幅广告
    bannerAdView?.destroy()
}
```

### 3. 检查广告有效性
显示广告前检查是否有效：

```kotlin
if (interstitialAd?.isAdInvalidated == false) {
    // 广告有效，可以显示
    showInterstitialAd(interstitialAd)
} else {
    // 广告已失效，需要重新加载
    loadInterstitialAd()
}
```

### 4. 测试模式切换
开发测试时使用测试模式，发布前切换到生产模式：

```kotlin
// 测试阶段
OperaAdConfig.isTestMode = true

// 发布前
OperaAdConfig.isTestMode = false
// 或
OperaAdConfig.switchToProduction()
```

---

## 🔧 自定义配置

### 横幅广告尺寸
支持多种横幅广告尺寸：

```kotlin
AdSize.BANNER          // 320x50 标准横幅
AdSize.BANNER_MREC     // 300x250 中矩形
AdSize.LARGE_BANNER    // 320x100 大横幅
AdSize.LEADERBOARD     // 728x90 排行榜
```

### 横幅广告自动刷新
```kotlin
val bannerAdView = OperaAdManager.loadBannerAd(...)

// 设置自动刷新间隔（秒）
bannerAdView.setAutoRefreshInterval(60)

// 禁用自动刷新
bannerAdView.setAutoRefreshEnabled(false)
```

### 原生广告 AdChoice 位置
```kotlin
nativeAd.setAdChoicePosition(NativeAd.AdChoicePosition.TOP_RIGHT)
// 可选: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
```

---

## ⚠️ 注意事项

1. **广告单元 ID**：在正式发布前，务必替换为真实的广告单元 ID
2. **资源释放**：及时调用 `destroy()` 释放广告资源，避免内存泄漏
3. **广告有效性**：显示广告前检查 `isAdInvalidated`
4. **用户体验**：不要过于频繁显示插页式广告，影响用户体验
5. **奖励发放**：只在 `onRewarded` 回调中发放奖励，确保用户观看完整视频

---

## 📚 更多信息

- [Opera Ads 官方文档](https://ads.opera.com/)
- [Opera Ads SDK 集成指南](https://docs.ads.opera.com/)

