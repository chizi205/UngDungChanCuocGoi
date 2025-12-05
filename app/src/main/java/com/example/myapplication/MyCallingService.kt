package com.example.myapplication

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyCallingService : CallScreeningService() {

    private val TAG = "MyCallingService"
    private val blockedList = mutableSetOf<String>()  // Dùng Set để tìm nhanh

    override fun onScreenCall(callDetails: Call.Details) {
        // Lấy số điện thoại gọi đến
        val incomingNumber = callDetails.handle?.schemeSpecificPart ?: return
        Log.d(TAG, "Có cuộc gọi từ: $incomingNumber")

        // Load danh sách số bị chặn từ SharedPreferences (cùng chỗ TrangChu lưu)
        loadBlockedNumbers()

        // Kiểm tra xem số này có trong danh sách chặn không
        val shouldBlock = blockedList.contains(incomingNumber) ||
                blockedList.any { incomingNumber.endsWith(it) } ||  // chặn theo đuôi số
                blockedList.any { incomingNumber.contains(it) }     // chặn chứa số

        if (shouldBlock) {
            Log.d(TAG, "ĐÃ CHẶN: $incomingNumber")

            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build()

            respondToCall(callDetails, response)
        } else {
            Log.d(TAG, "CHO QUA: $incomingNumber")
        }
    }

    private fun loadBlockedNumbers() {
        blockedList.clear()
        val prefs = getSharedPreferences("blocked_prefs", MODE_PRIVATE)
        val json = prefs.getString("blocked_numbers", null) ?: return

        try {
            val type = object : TypeToken<List<BlockedPhone>>() {}.type
            val list: List<BlockedPhone> = Gson().fromJson(json, type)
            blockedList.addAll(list.map { it.number.replace("[^0-9]".toRegex(), "") }) // chỉ lấy số
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi load danh sách chặn: ${e.message}")
        }
    }
}