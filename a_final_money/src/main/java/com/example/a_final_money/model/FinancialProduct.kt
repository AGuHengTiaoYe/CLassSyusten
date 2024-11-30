package com.example.a_final_money.model


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

data class FinancialProduct(
    val productId: String,       // 产品唯一标识
    val productName: String,     // 产品名称
    val annualRate: Double,      // 年化收益率
    val investmentAmount: Double, // 投资金额
    val investmentDate: LocalDate, // 投资日期
    val duration: Int,           // 投资期限（月）
    val productType: ProductType // 产品类型
) {
    // 计算预期收益
    fun calculateExpectedProfit(): Double {
        val monthlyRate = annualRate / 12
        return investmentAmount * monthlyRate * duration
    }

    // 计算到期日
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMaturityDate(): LocalDate {
        return investmentDate.plusMonths(duration.toLong())
    }
}


// 产品类型枚举
enum class ProductType {
    FUND,       // 基金
    BOND,       // 债券
    FIXED_DEPOSIT, // 定期存款
    STOCK_FUND, // 股票基金
    MONEY_MARKET // 货币市场基金
}