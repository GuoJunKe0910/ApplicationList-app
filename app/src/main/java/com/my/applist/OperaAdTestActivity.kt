package com.my.applist

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.my.applist.ad.opera.OperaAdUsageExample
import com.my.applist.databinding.ActivityOperaAdTestBinding

/**
 * Opera广告SDK测试Activity
 * 用于测试各种类型的广告加载和显示
 */
class OperaAdTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperaAdTestBinding
    private lateinit var operaAdExample: OperaAdUsageExample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperaAdTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化Opera广告示例
        operaAdExample = OperaAdUsageExample(this)

        setupClickListeners()
        setupStatusBar()
    }

    /**
     * 设置状态栏
     */
    private fun setupStatusBar() {
        window.statusBarColor = getColor(R.color.primary_dark)
    }

    /**
     * 设置按钮点击监听
     */
    private fun setupClickListeners() {
        // 加载原生广告
        binding.btnLoadNativeAd.setOnClickListener {
            Toast.makeText(this, "开始加载原生广告...", Toast.LENGTH_SHORT).show()
            operaAdExample.loadNativeAdExample()
        }
        
        // 显示原生广告
        binding.btnShowNativeAd.setOnClickListener {
            operaAdExample.showNativeAdExample(binding.nativeAdContainer)
        }

        // 加载横幅广告 (320x50)
        binding.btnLoadBannerAd.setOnClickListener {
            Toast.makeText(this, "开始加载横幅广告...", Toast.LENGTH_SHORT).show()
            // 清空容器
            binding.bannerAdContainer.removeAllViews()
            operaAdExample.loadBannerAdExample(binding.bannerAdContainer)
        }

        // 加载中矩形广告 (300x250)
        binding.btnLoadBannerMrec.setOnClickListener {
            Toast.makeText(this, "开始加载中矩形广告...", Toast.LENGTH_SHORT).show()
            // 清空容器
            binding.bannerMrecContainer.removeAllViews()
            operaAdExample.loadBannerMrecExample(binding.bannerMrecContainer)
        }

        // 加载插页式广告
        binding.btnLoadInterstitialAd.setOnClickListener {
            Toast.makeText(this, "开始加载插页式广告...", Toast.LENGTH_SHORT).show()
            operaAdExample.loadInterstitialAdExample()
        }

        // 显示插页式广告
        binding.btnShowInterstitialAd.setOnClickListener {
            operaAdExample.showInterstitialAdExample()
        }

        // 加载插页式广告
        binding.btnLoadInterstitialVideoAd.setOnClickListener {
            Toast.makeText(this, "开始加载插页视频式广告...", Toast.LENGTH_SHORT).show()
            operaAdExample.loadInterstitialVideoAdExample()
        }

        // 显示插页式广告
        binding.btnShowInterstitialVideoAd.setOnClickListener {
            operaAdExample.showInterstitialVideoAdExample()
        }

        // 加载激励视频广告
        binding.btnLoadRewardedAd.setOnClickListener {
            Toast.makeText(this, "开始加载激励视频广告...", Toast.LENGTH_SHORT).show()
            operaAdExample.loadRewardedAdExample()
        }

        // 显示激励视频广告
        binding.btnShowRewardedAd.setOnClickListener {
            operaAdExample.showRewardedAdExample()
        }

        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放广告资源
        operaAdExample.destroy()
    }
}

