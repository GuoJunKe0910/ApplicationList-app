package com.my.applist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.my.applist.databinding.ItemAppBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 应用列表适配器
 */
class AppListAdapter(
    private val appList: List<AppInfo>,
    private val onLaunchClick: (AppInfo) -> Unit,
    private val onForceStopClick: (AppInfo) -> Unit,
    private val onAppInfoClick: (AppInfo) -> Unit,
    private val onClearDataClick: (AppInfo) -> Unit,
    private val onUninstallClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    inner class AppViewHolder(private val binding: ItemAppBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(appInfo: AppInfo) {
            binding.apply {
                // 设置应用图标
                ivAppIcon.setImageDrawable(appInfo.icon)
                
                // 设置应用名称
                tvAppName.text = appInfo.appName
                
                // 设置包名
                tvPackageName.text = appInfo.packageName
                
                // 设置版本信息
                tvVersion.text = "版本: ${appInfo.versionName} (${appInfo.versionCode})"
                
                // 设置安装时间（精确到秒）
                tvInstallTime.text = "安装: ${dateFormat.format(Date(appInfo.installTime))}"
                
                // 设置更新时间（精确到秒）
                tvUpdateTime.text = "更新: ${dateFormat.format(Date(appInfo.updateTime))}"
                
                // 设置按钮点击事件
                btnLaunch.setOnClickListener {
                    onLaunchClick(appInfo)
                }
                
                btnForceStop.setOnClickListener {
                    onForceStopClick(appInfo)
                }
                
                btnAppInfo.setOnClickListener {
                    onAppInfoClick(appInfo)
                }
                
                btnClearData.setOnClickListener {
                    onClearDataClick(appInfo)
                }
                
                btnUninstall.setOnClickListener {
                    onUninstallClick(appInfo)
                }
                
                // 如果是系统应用，禁用卸载按钮
                if (appInfo.isSystemApp) {
                    btnUninstall.isEnabled = false
                    btnUninstall.alpha = 0.4f
                } else {
                    btnUninstall.isEnabled = true
                    btnUninstall.alpha = 1.0f
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(appList[position])
    }

    override fun getItemCount(): Int = appList.size
}

