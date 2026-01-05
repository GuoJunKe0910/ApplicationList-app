package com.my.applist

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.my.applist.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter
    private val allAppList = mutableListOf<AppInfo>() // 所有应用
    private val displayList = mutableListOf<AppInfo>() // 显示的应用列表
    
    private var currentSearchQuery = "" // 当前搜索关键词
    private var currentFilterMode = FilterMode.NONE // 当前筛选模式
    private var isSearching = false // 是否正在搜索
    private var hasRootAccess = false // 是否有root权限
    
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    
    enum class FilterMode {
        NONE,           // 无筛选
        TIME_DESC,      // 按时间降序
        TIME_ASC,       // 按时间升序
        LETTER_ASC,     // 按首字母升序
        LETTER_DESC     // 按首字母降序
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusBar()
        checkRootAccess()
        setupRecyclerView()
        setupSearchListener()
        setupFilterListeners()
        setupSwipeRefresh()
        loadInstalledApps()
    }

    override fun onResume() {
        super.onResume()
        // 每次返回界面时重新加载应用列表
        if (allAppList.isNotEmpty()) {
            loadInstalledApps()
        }
    }

    /**
     * 设置状态栏适配
     */
    private fun setupStatusBar() {
        // 设置状态栏颜色
        window.statusBarColor = getColor(R.color.primary_dark)
    }

    /**
     * 检测Root权限
     */
    private fun checkRootAccess() {
        lifecycleScope.launch {
            binding.tvRootStatus.text = "检测Root权限中..."
            
            hasRootAccess = withContext(Dispatchers.IO) {
                RootUtils.checkRootAccess()
            }
            
            binding.tvRootStatus.text = if (hasRootAccess) {
                "✓ Root权限已获取，可使用高级功能"
            } else {
                "⚠ 无Root权限，部分功能受限"
            }
        }
    }


    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = AppListAdapter(
            appList = displayList,
            onLaunchClick = { appInfo -> launchApp(appInfo) },
            onForceStopClick = { appInfo -> forceStopApp(appInfo) },
            onAppInfoClick = { appInfo -> openAppSettings(appInfo) },
            onClearDataClick = { appInfo -> clearAppData(appInfo) },
            onUninstallClick = { appInfo -> uninstallApp(appInfo) }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    /**
     * 设置搜索监听
     */
    private fun setupSearchListener() {
        // 文字变化监听（带防抖）
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                // 取消之前的搜索任务
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                val query = s?.toString()?.trim() ?: ""
                
                // 如果是空字符串，立即恢复显示
                if (query.isEmpty()) {
                    currentSearchQuery = ""
                    isSearching = false
                    applyFilter()
                    return
                }
                
                // 延迟300ms执行搜索，避免频繁触发
                searchRunnable = Runnable {
                    currentSearchQuery = query
                    isSearching = true
                    performSearch()
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
        })
        
        // 键盘搜索按钮监听
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 点击搜索按钮时立即执行搜索
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                currentSearchQuery = binding.etSearch.text?.toString()?.trim() ?: ""
                isSearching = currentSearchQuery.isNotEmpty()
                if (isSearching) {
                    performSearch()
                }
                // 隐藏软键盘
                binding.etSearch.clearFocus()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    /**
     * 设置筛选监听
     */
    private fun setupFilterListeners() {
        // 按更新时间筛选
        binding.btnFilterTime.setOnClickListener {
            if (!isSearching) {
                showTimeFilterDialog()
            } else {
                Toast.makeText(this, "请先清除搜索", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 按首字母筛选
        binding.btnFilterLetter.setOnClickListener {
            if (!isSearching) {
                showLetterFilterDialog()
            } else {
                Toast.makeText(this, "请先清除搜索", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 清除筛选
        binding.btnClearFilter.setOnClickListener {
            currentFilterMode = FilterMode.NONE
            binding.btnFilterTime.text = "按时间"
            binding.btnFilterLetter.text = "按字母"
            binding.btnClearFilter.visibility = android.view.View.GONE
            applyFilter()
        }
    }

    /**
     * 设置下拉刷新
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            getColor(R.color.primary),
            getColor(R.color.primary_dark),
            getColor(R.color.accent)
        )
        
        binding.swipeRefresh.setOnRefreshListener {
            // 重新加载应用列表
            loadInstalledApps()
        }
    }

    /**
     * 显示时间筛选菜单
     */
    private fun showTimeFilterDialog() {
        val popupMenu = PopupMenu(this, binding.btnFilterTime)
        popupMenu.menuInflater.inflate(R.menu.menu_filter_time, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_time_desc -> {
                    currentFilterMode = FilterMode.TIME_DESC
                    binding.btnFilterTime.text = "时间↓"
                    binding.btnClearFilter.visibility = android.view.View.VISIBLE
                    applyFilter()
                    true
                }
                R.id.menu_time_asc -> {
                    currentFilterMode = FilterMode.TIME_ASC
                    binding.btnFilterTime.text = "时间↑"
                    binding.btnClearFilter.visibility = android.view.View.VISIBLE
                    applyFilter()
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }

    /**
     * 显示首字母筛选菜单
     */
    private fun showLetterFilterDialog() {
        val popupMenu = PopupMenu(this, binding.btnFilterLetter)
        popupMenu.menuInflater.inflate(R.menu.menu_filter_letter, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_letter_asc -> {
                    currentFilterMode = FilterMode.LETTER_ASC
                    binding.btnFilterLetter.text = "字母↑"
                    binding.btnClearFilter.visibility = android.view.View.VISIBLE
                    applyFilter()
                    true
                }
                R.id.menu_letter_desc -> {
                    currentFilterMode = FilterMode.LETTER_DESC
                    binding.btnFilterLetter.text = "字母↓"
                    binding.btnClearFilter.visibility = android.view.View.VISIBLE
                    applyFilter()
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }

    /**
     * 执行搜索
     */
    private fun performSearch() {
        val query = currentSearchQuery.lowercase()
        displayList.clear()
        
        displayList.addAll(allAppList.filter { app ->
            app.appName.lowercase().contains(query) ||
            app.packageName.lowercase().contains(query) ||
            app.pinyin.lowercase().contains(query)
        })
        
        adapter.notifyDataSetChanged()
        updateAppCount()
    }

    /**
     * 应用筛选
     */
    private fun applyFilter() {
        displayList.clear()
        
        val sortedList = when (currentFilterMode) {
            FilterMode.TIME_DESC -> allAppList.sortedByDescending { it.updateTime }
            FilterMode.TIME_ASC -> allAppList.sortedBy { it.updateTime }
            FilterMode.LETTER_ASC -> allAppList.sortedBy { it.firstLetter }
            FilterMode.LETTER_DESC -> allAppList.sortedByDescending { it.firstLetter }
            FilterMode.NONE -> allAppList.sortedByDescending { it.updateTime } // 默认按更新时间降序
        }
        
        displayList.addAll(sortedList)
        adapter.notifyDataSetChanged()
        updateAppCount()
    }

    /**
     * 更新应用数量显示
     */
    private fun updateAppCount() {
        if (isSearching) {
            binding.tvAppCount.text = "找到 ${displayList.size} 个应用（共 ${allAppList.size} 个）"
        } else {
            binding.tvAppCount.text = "共 ${displayList.size} 个应用"
        }
    }

    /**
     * 加载已安装的应用列表
     */
    private fun loadInstalledApps() {
        lifecycleScope.launch {
            // 如果不是下拉刷新，显示进度条
            if (!binding.swipeRefresh.isRefreshing) {
                binding.progressBar.visibility = android.view.View.VISIBLE
            }
            
            val apps = withContext(Dispatchers.IO) {
                val pm = packageManager
                val currentPackageName = packageName // 获取本应用的包名
                val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                
                installedApps
                    .filter { it.packageName != currentPackageName } // 排除本应用
                    .mapNotNull { appInfo ->
                        try {
                            val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
                            val appName = appInfo.loadLabel(pm).toString()
                            
                            // 获取版本信息
                            val versionName = packageInfo.versionName ?: "未知"
                            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                packageInfo.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                packageInfo.versionCode.toLong()
                            }
                            
                            AppInfo(
                                appName = appName,
                                packageName = appInfo.packageName,
                                icon = appInfo.loadIcon(pm),
                                installTime = packageInfo.firstInstallTime,
                                updateTime = packageInfo.lastUpdateTime,
                                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                                pinyin = PinyinUtils.getPinyin(appName),
                                firstLetter = PinyinUtils.getFirstLetter(appName),
                                versionName = versionName,
                                versionCode = versionCode
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    // 默认按更新时间降序排序
                    .sortedByDescending { it.updateTime }
            }
            
            allAppList.clear()
            allAppList.addAll(apps)
            
            // 根据当前状态更新显示列表
            if (isSearching) {
                performSearch()
            } else {
                applyFilter()
            }
            
            binding.progressBar.visibility = android.view.View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    /**
     * 启动应用
     */
    private fun launchApp(appInfo: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
        if (launchIntent != null) {
            try {
                startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "无法启动该应用", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "该应用无法启动", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 打开应用设置
     */
    private fun openAppSettings(appInfo: AppInfo) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", appInfo.packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开应用设置", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 强制停止应用
     */
    private fun forceStopApp(appInfo: AppInfo) {
        if (hasRootAccess) {
            // 有root权限，直接停止
            AlertDialog.Builder(this)
                .setTitle("强制停止")
                .setMessage("确定要强制停止 ${appInfo.appName} 吗？")
                .setPositiveButton("确定") { _, _ ->
                    lifecycleScope.launch {
                        val progressDialog = AlertDialog.Builder(this@MainActivity)
                            .setMessage("正在停止...")
                            .setCancelable(false)
                            .create()
                        progressDialog.show()
                        
                        val success = withContext(Dispatchers.IO) {
                            RootUtils.forceStopApp(appInfo.packageName)
                        }
                        
                        progressDialog.dismiss()
                        
                        if (success) {
                            Toast.makeText(this@MainActivity, "已强制停止 ${appInfo.appName}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "停止失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            // 无root权限，跳转到设置
            Toast.makeText(this, "需要Root权限才能强制停止应用", Toast.LENGTH_SHORT).show()
            openAppSettings(appInfo)
        }
    }

    /**
     * 清除应用数据
     */
    private fun clearAppData(appInfo: AppInfo) {
        if (hasRootAccess) {
            // 有root权限，直接清除
            AlertDialog.Builder(this)
                .setTitle("清除数据")
                .setMessage("确定要清除 ${appInfo.appName} 的所有数据吗？\n\n此操作不可恢复！")
                .setPositiveButton("确定") { _, _ ->
                    lifecycleScope.launch {
                        val progressDialog = AlertDialog.Builder(this@MainActivity)
                            .setMessage("正在清除数据...")
                            .setCancelable(false)
                            .create()
                        progressDialog.show()
                        
                        val success = withContext(Dispatchers.IO) {
                            RootUtils.clearAppData(appInfo.packageName)
                        }
                        
                        progressDialog.dismiss()
                        
                        if (success) {
                            Toast.makeText(this@MainActivity, "已清除 ${appInfo.appName} 的数据", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "清除失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            // 无root权限，提示前往设置
            AlertDialog.Builder(this)
                .setTitle("清除数据")
                .setMessage("确定要清除 ${appInfo.appName} 的所有数据吗？\n\n注意：需要在系统设置中手动完成。")
                .setPositiveButton("前往设置") { _, _ ->
                    openAppSettings(appInfo)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    /**
     * 卸载应用
     */
    private fun uninstallApp(appInfo: AppInfo) {
        if (appInfo.isSystemApp) {
            Toast.makeText(this, "系统应用无法卸载", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", appInfo.packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法卸载该应用", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理Handler回调，避免内存泄漏
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
    }
}

