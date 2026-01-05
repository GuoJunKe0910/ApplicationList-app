package com.my.applist

import android.graphics.drawable.Drawable

/**
 * 应用信息数据类
 */
data class AppInfo(
    val appName: String,           // 应用名称
    val packageName: String,       // 包名
    val icon: Drawable,            // 应用图标
    val installTime: Long,         // 安装时间（毫秒）
    val updateTime: Long,          // 更新时间（毫秒）
    val isSystemApp: Boolean,      // 是否为系统应用
    val pinyin: String,            // 拼音全拼
    val firstLetter: String,       // 首字母
    val versionName: String,       // 版本名称
    val versionCode: Long          // 版本号
)

