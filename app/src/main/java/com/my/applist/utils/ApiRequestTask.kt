package com.my.applist.utils

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
class ApiRequestTask(private val deviceInfo: String, private val callback: ApiCallback) :
    AsyncTask<Void, Void, String>() {

    private val urlString = "https://api.jojoyfun.online/api/v1/config"
    private val TAG = "ApiRequestTask"

    override fun doInBackground(vararg params: Void?): String {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            // 创建 URL 对象
            val url = URL(urlString)
            // 打开连接
            connection = url.openConnection() as HttpURLConnection
            // 设置请求方法为 POST
            connection.requestMethod = "POST"
            // 设置连接超时时间和读取超时时间
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/json")
            val jsonObject = JSONObject(deviceInfo)
            Log.e(TAG, "doInBackground: $deviceInfo" )
            val packageName = jsonObject.getString("packageName")
            val versionName = jsonObject.getString("versionName")
            val channel = jsonObject.getString("channel")
            val mutableMapOf = mutableMapOf<String, String>()
            mutableMapOf["packageName"] = packageName
            mutableMapOf["versionName"] = versionName
            mutableMapOf["channel"] = channel
            mutableMapOf["region"] = getCountry()
            val sortedParamStr = mutableMapOf.toSortedMap().entries.joinToString("&") { "${it.key}=${it.value}" }
            val currentTimeMillis = System.currentTimeMillis()
            val string = "POST|/api/v1/config|$sortedParamStr|${currentTimeMillis}"
            val encryptSafe = HmacSHA256Utils.encryptSafe(string, "test-secret-key")
            connection.setRequestProperty("X-Signature",encryptSafe )
            connection.setRequestProperty("X-Timestamp",currentTimeMillis.toString())

            Log.e(TAG, "$string --doInBackground: ${HmacSHA256Utils.verifySignature(string, encryptSafe.toString(), "test-secret-key")}")
            // 允许输出数据
            connection.doOutput = true

            // 构建请求体
            val json = JSONObject()
            json.put("data", deviceInfo)
            val requestBody = json.toString()
            Log.e(TAG, "Request Body: $requestBody && $encryptSafe && $currentTimeMillis")

            // 获取输出流并写入请求体
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(requestBody)
            outputStream.flush()
            outputStream.close()

            // 获取响应码
            val responseCode = connection.responseCode
            Log.e(TAG, "Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 获取输入流并读取响应数据
                reader = BufferedReader(InputStreamReader(connection.inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                val responseData = stringBuilder.toString()
                Log.e(TAG, "Response Data: $responseData")
                return responseData
            } else {
                Log.e(TAG, "Request failed with response code: $responseCode")
                return ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during request${e.message}", e)
            return ""
        } finally {
            // 关闭连接和流
            connection?.disconnect()
            reader?.close()
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        try {
            Log.e(TAG, "Result: $result")
            if (result.isEmpty()) {
                Log.e(TAG, "Response data is empty")
                callback.onFailure("Response data is empty")
                return
            }
            val jsonObject = JSONObject(result)
            val code = jsonObject.getInt("code")
            if (code == 200) {
                val data = jsonObject.getString("data")
                callback.onSuccess(data)
            } else {
                val msg = jsonObject.getString("msg")
                callback.onFailure(msg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error${e.message}", e)
            callback.onFailure("JSON parsing error")
        }
    }
}

interface ApiCallback {
    fun onSuccess(data: String)
    fun onFailure(errorMessage: String)
}