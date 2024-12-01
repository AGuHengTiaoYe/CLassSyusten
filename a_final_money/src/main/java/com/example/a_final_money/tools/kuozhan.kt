package com.example.a_final_money.tools

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// 创建 String 扩展方法
// 扩展函数：将 yyyyMM 格式的字符串转为 LocalDate
@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDate(): LocalDate? {
    if (this.isBlank()) return null
    return try {
        // 自定义格式化器，解析 yyyyMM 格式的日期
        val formatter = DateTimeFormatter.ofPattern("yyyyMM")
        Log.v("wq",LocalDate.parse(this + "01", formatter).toString())
        // 在字符串末尾添加 "01" 来表示默认的日期
        LocalDate.parse(this + "01", formatter)
    } catch (e: Exception) {
        // 解析失败时返回 null
        null
    }
}