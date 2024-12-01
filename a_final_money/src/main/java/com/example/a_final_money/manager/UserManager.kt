package com.example.a_final_money.manager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.a_final_money.DBHelper
import com.example.a_final_money.Transaction
import com.example.a_final_money.TransactionType
import com.example.a_final_money.model.FinancialProduct
import com.example.a_final_money.model.User
import java.lang.ref.WeakReference
import java.time.LocalDate
import java.util.logging.Logger

class UserManager private constructor(context: Context) {
    var user: User? = null
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val dbUserHelper: DBHelper? by lazy {
        contextRef.get()?.let { DBHelper(it) }
    }

    companion object {
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context).also { instance = it }
            }
        }
    }

    fun register(id: String, psd: String): Long {
        return dbUserHelper?.registerUser(User(id, psd)) ?: -1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun login(id: String, psd: String): User? {
        return dbUserHelper?.userLogin(id, psd)?.also {
            user = it
            user!!.ownedFinancialProducts = dbUserHelper!!.getUserFinancialProducts(user!!.userId)
        }
    }

    fun getUserId(): String? {
        return user?.userId
    }

    // 存款 - 使用 TransactionType
    @RequiresApi(Build.VERSION_CODES.O)
    fun deposit(amount: Double): Boolean {
        val currentUser = user ?: return false
        val newBalance = currentUser.accountBalance + amount
        try {
            dbUserHelper?.beginTransaction()
            val updated = dbUserHelper?.updateUserBalance(currentUser.userId, newBalance)
            if (updated != 1) throw Exception("Failed to update user balance")

            // 记录交易 - 使用 DEPOSIT 类型
            val transactionId = dbUserHelper?.addTransaction(
                currentUser.userId,
                 amount,
                type = TransactionType.DEPOSIT
            )
            if (transactionId == -1L) throw Exception("Failed to record transaction")

            dbUserHelper?.setTransactionSuccessful()
            currentUser.accountBalance = newBalance
            return true
        } catch (e: Exception) {
            dbUserHelper?.endTransaction()
            Logger.getLogger("UserManager").severe("Deposit failed: ${e.message}")
            return false
        }finally {
            dbUserHelper?.endTransaction()
        }
    }

    // 取款 - 使用 TransactionType
    @RequiresApi(Build.VERSION_CODES.O)
    fun withdraw(amount: Double): Boolean {
        val currentUser = user ?: return false
        if (currentUser.accountBalance < amount) {
            return false // 余额不足
        }
        val newBalance = currentUser.accountBalance - amount
        try {
            dbUserHelper?.beginTransaction()
            val updated = dbUserHelper?.updateUserBalance(currentUser.userId, newBalance)
            if (updated != 1) throw Exception("Failed to update user balance")

            // 记录交易 - 使用 WITHDRAW 类型
            val transactionId = dbUserHelper?.addTransaction(
                currentUser.userId,
                -amount,
                type = TransactionType.WITHDRAW
            )
            if (transactionId == -1L) throw Exception("Failed to record transaction")

            dbUserHelper?.setTransactionSuccessful()
            currentUser.accountBalance = newBalance
            return true
        } catch (e: Exception) {
            dbUserHelper?.endTransaction()
            Logger.getLogger("UserManager").severe("Withdraw failed: ${e.message}")
            return false
        }finally {
            dbUserHelper?.endTransaction()
        }
    }
    // 修改用户名
    fun updateUserName(newUserName: String): Boolean {
        val currentUser = user ?: return false
        val rowsAffected = dbUserHelper?.updateUserName(currentUser.userId, newUserName) ?: 0
        if (rowsAffected > 0) {
            currentUser.userName = newUserName
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserProducts(): List<FinancialProduct> {
        return dbUserHelper!!.getUserFinancialProducts(user!!.userId)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserTransactions(userId: String,
                           startDate: LocalDate? = null,
                           endDate: LocalDate? = null):List<Transaction> {
        return dbUserHelper!!.getUserTransactions(userId,startDate,endDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addOrUpdateUserFinancialProduct(
        userId: String,
        product: FinancialProduct,
        overwriteExisting: Boolean = false
    ): Boolean{
        return dbUserHelper!!.addOrUpdateUserFinancialProduct(userId, product)
    }

    fun removeUserFinancialProduct(userId: String, productId: String): Boolean {
        return dbUserHelper!!.removeUserFinancialProduct(userId, productId)
    }
}