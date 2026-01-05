package com.my.applist

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Root权限工具类
 */
object RootUtils {
    
    private var isRootChecked = false
    private var hasRootAccess = false
    
    /**
     * 检查是否有root权限
     */
    fun checkRootAccess(): Boolean {
        if (isRootChecked) {
            return hasRootAccess
        }
        
        var process: Process? = null
        var os: DataOutputStream? = null
        
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            
            val exitValue = process.waitFor()
            hasRootAccess = exitValue == 0
        } catch (e: Exception) {
            hasRootAccess = false
        } finally {
            try {
                os?.close()
                process?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        isRootChecked = true
        return hasRootAccess
    }
    
    /**
     * 执行root命令
     * @param commands 要执行的命令列表
     * @return Pair<成功标志, 输出结果>
     */
    fun executeRootCommand(vararg commands: String): Pair<Boolean, String> {
        var process: Process? = null
        var os: DataOutputStream? = null
        var reader: BufferedReader? = null
        val result = StringBuilder()
        
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            reader = BufferedReader(InputStreamReader(process.inputStream))
            
            for (command in commands) {
                os.writeBytes("$command\n")
                os.flush()
            }
            
            os.writeBytes("exit\n")
            os.flush()
            
            // 读取输出
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
            }
            
            val exitValue = process.waitFor()
            return Pair(exitValue == 0, result.toString())
        } catch (e: Exception) {
            return Pair(false, "执行失败: ${e.message}")
        } finally {
            try {
                reader?.close()
                os?.close()
                process?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 清除应用数据
     * @param packageName 包名
     * @return 是否成功
     */
    fun clearAppData(packageName: String): Boolean {
        val (success, output) = executeRootCommand("pm clear $packageName")
        return success && output.contains("Success")
    }
    
    /**
     * 强制停止应用
     * @param packageName 包名
     * @return 是否成功
     */
    fun forceStopApp(packageName: String): Boolean {
        val (success, _) = executeRootCommand("am force-stop $packageName")
        return success
    }
    
    /**
     * 重置root检测状态（用于测试）
     */
    fun resetRootCheck() {
        isRootChecked = false
        hasRootAccess = false
    }
}


