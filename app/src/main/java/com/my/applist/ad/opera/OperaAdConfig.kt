package com.my.applist.ad.opera

/**
 * Opera 广告配置类
 * 用于管理所有广告单元 ID
 */
object OperaAdConfig {
    
    /**
     * 是否启用测试模式
     * 测试模式下会使用测试广告单元ID
     */
    var isTestMode = true
    
    // ======================== 测试广告单元ID ========================
    // 在测试阶段使用这些ID，正式发布前替换为真实的广告单元ID
    
    private const val TEST_NATIVE_AD_UNIT_ID = "s13429368154496"
    private const val TEST_BANNER_AD_UNIT_ID = "s13423621779136"
    private const val TEST_BANNER_AD_UNIT_ID2 = "s13429297184768"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "s13423624619200"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID2 = "s13424442482432"
    private const val TEST_REWARDED_AD_UNIT_ID = "s13938889680960"
    
    // ======================== 生产环境广告单元ID ========================
    // TODO: 替换为从 Opera Ads 控制台获取的真实广告单元ID
    
    private const val PROD_NATIVE_AD_UNIT_ID = "s13429368154496"
    private const val PROD_BANNER_AD_UNIT_ID = "s13423621779136"
    private const val PROD_BANNER_AD_UNIT_ID2 = "s13429297184768"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "s13423624619200"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID2 = "s13424442482432"
    private const val PROD_REWARDED_AD_UNIT_ID = "s13938889680960"
    
    // ======================== 获取广告单元ID ========================
    
    /**
     * 获取原生广告单元ID
     */
    val nativeAdUnitId: String
        get() = if (isTestMode) TEST_NATIVE_AD_UNIT_ID else PROD_NATIVE_AD_UNIT_ID
    
    /**
     * 获取横幅广告单元ID
     */
    val bannerAdUnitId: String
        get() = if (isTestMode) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
    /**
     * 获取横幅广告单元ID（视频）
     */
    val bannerVideoAdUnitId: String
        get() = if (isTestMode) TEST_BANNER_AD_UNIT_ID2 else PROD_BANNER_AD_UNIT_ID2
    
    /**
     * 获取插页式广告单元ID
     */
    val interstitialAdUnitId: String
        get() = if (isTestMode) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    /**
     * 获取插页式广告单元ID (视频)
     */
    val interstitialVideoAdUnitId: String
        get() = if (isTestMode) TEST_INTERSTITIAL_AD_UNIT_ID2 else PROD_INTERSTITIAL_AD_UNIT_ID2

    /**
     * 获取激励视频广告单元ID
     */
    val rewardedAdUnitId: String
        get() = if (isTestMode) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
    
    /**
     * 切换到生产环境
     * 使用真实的广告单元ID
     */
    fun switchToProduction() {
        isTestMode = false
    }
    
    /**
     * 切换到测试环境
     * 使用测试广告单元ID
     */
    fun switchToTest() {
        isTestMode = true
    }
}

