package com.example.a_final_money.manager

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.a_final_money.model.FinancialProduct
import com.example.a_final_money.model.User
import com.example.a_final_money.DBHelper
import java.math.BigDecimal
import java.time.LocalDate
import java.util.logging.Logger

// Enhanced InvestmentManager with more comprehensive error handling and validation
class InvestmentManager(private val dbHelper: DBHelper) {
    private val logger = Logger.getLogger("InvestmentManager")

    // Enum for investment outcomes
    enum class InvestmentResult {
        SUCCESS, INSUFFICIENT_BALANCE, PRODUCT_NOT_MATURED, TRANSACTION_FAILED
    }

    // Comprehensive method for buying financial product
    @RequiresApi(Build.VERSION_CODES.O)
    fun buyProduct(user: User, product: FinancialProduct): InvestmentResult {
        return dbHelper.executeInTransaction {
            // Validate inputs
            if (user.accountBalance < product.investmentAmount) {
                logger.warning("Insufficient balance for investment")
                return@executeInTransaction InvestmentResult.INSUFFICIENT_BALANCE
            }

            // Deduct investment amount
            val updatedBalance = user.accountBalance - product.investmentAmount
            val balanceUpdateResult = dbHelper.updateUserBalance(user.userId, updatedBalance)

            if (balanceUpdateResult <= 0) {
                logger.severe("Failed to update user balance")
                return@executeInTransaction InvestmentResult.TRANSACTION_FAILED
            }

            // Add financial product to database
            val productAddResult = dbHelper.addFinancialProduct(user.userId, product)
            if (!productAddResult) {
                logger.severe("Failed to add financial product")
                return@executeInTransaction InvestmentResult.TRANSACTION_FAILED
            }

            // Record transaction
            val transactionResult = dbHelper.addTransaction(
                user.userId,
                product.productId,
                product.investmentAmount,
                "投资支出"
            )

            if (transactionResult == -1L) {
                logger.severe("Failed to record transaction")
                return@executeInTransaction InvestmentResult.TRANSACTION_FAILED
            }

            // Update user state
            user.accountBalance = updatedBalance
            user.ownedFinancialProducts.add(product)

            InvestmentResult.SUCCESS
        } ?: InvestmentResult.TRANSACTION_FAILED
    }

    // Comprehensive method for redeeming financial product
    @RequiresApi(Build.VERSION_CODES.O)
    fun redeemProduct(user: User, product: FinancialProduct): Pair<InvestmentResult, Double> {
        return dbHelper.executeInTransaction {
            // Check product maturity
            val currentDate = LocalDate.now()
            val maturityDate = product.getMaturityDate()

            if (currentDate < maturityDate) {
                logger.warning("Product not yet matured")
                return@executeInTransaction Pair(InvestmentResult.PRODUCT_NOT_MATURED, 0.0)
            }

            // Calculate profit
            val profit = product.calculateExpectedProfit()
            val totalAmount = product.investmentAmount + profit
            val updatedBalance = user.accountBalance + totalAmount

            // Update user balance
            val balanceUpdateResult = dbHelper.updateUserBalance(user.userId, updatedBalance)
            if (balanceUpdateResult <= 0) {
                logger.severe("Failed to update user balance")
                return@executeInTransaction Pair(InvestmentResult.TRANSACTION_FAILED, 0.0)
            }

            // Remove product from database
            val productRemoveResult = dbHelper.removeFinancialProduct(product.productId)
            if (!productRemoveResult) {
                logger.severe("Failed to remove financial product")
                return@executeInTransaction Pair(InvestmentResult.TRANSACTION_FAILED, 0.0)
            }

            // Record transaction
            val transactionResult = dbHelper.addTransaction(
                user.userId,
                product.productId,
                totalAmount,
                "投资收入"
            )

            if (transactionResult == -1L) {
                logger.severe("Failed to record transaction")
                return@executeInTransaction Pair(InvestmentResult.TRANSACTION_FAILED, 0.0)
            }

            // Update user state
            user.accountBalance = updatedBalance
            user.ownedFinancialProducts.remove(product)

            Pair(InvestmentResult.SUCCESS, profit)
        } ?: Pair(InvestmentResult.TRANSACTION_FAILED, 0.0)
    }

    // Comprehensive total assets calculation
    fun calculateTotalAssets(user: User): Double {
        val productValues = user.ownedFinancialProducts.sumOf {
            it.investmentAmount + it.calculateExpectedProfit()
        }
        return user.accountBalance + productValues
    }
}
