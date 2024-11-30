package com.example.a_final_money.manager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.a_final_money.DBHelper
import com.example.a_final_money.model.User
import java.lang.ref.WeakReference
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

    fun login(id: String, psd: String): User? {
        return dbUserHelper?.userLogin(id, psd)?.also {
            user = it
        }
    }

    fun getUserId(): String? {
        return user?.userId
    }

    // 存款
    @RequiresApi(Build.VERSION_CODES.O)
    fun deposit(amount: Double): Boolean {
        val currentUser = user ?: return false
        val newBalance = currentUser.accountBalance + amount
        try {
            dbUserHelper?.beginTransaction() // 开始事务
            val updated = dbUserHelper?.updateUserBalance(currentUser.userId, newBalance)
            if (updated != 1) throw Exception("Failed to update user balance")

            // 记录交易
            val transactionId = dbUserHelper?.addTransaction(currentUser.userId, amount, "收入")
            if (transactionId == -1L) throw Exception("Failed to record transaction")

            dbUserHelper?.setTransactionSuccessful() // 设置事务成功
            currentUser.accountBalance = newBalance
            return true
        } catch (e: Exception) {
            dbUserHelper?.endTransaction() // 结束事务
            Logger.getLogger("UserManager").severe("Deposit failed: ${e.message}")
            return false
        }
    }

    // 取款
    @RequiresApi(Build.VERSION_CODES.O)
    fun withdraw(amount: Double): Boolean {
        val currentUser = user ?: return false
        if (currentUser.accountBalance < amount) {
            return false // 余额不足
        }
        val newBalance = currentUser.accountBalance - amount
        try {
            dbUserHelper?.beginTransaction() // 开始事务
            val updated = dbUserHelper?.updateUserBalance(currentUser.userId, newBalance)
            if (updated != 1) throw Exception("Failed to update user balance")

            // 记录交易
            val transactionId = dbUserHelper?.addTransaction(currentUser.userId, -amount, "支出")
            if (transactionId == -1L) throw Exception("Failed to record transaction")

            dbUserHelper?.setTransactionSuccessful() // 设置事务成功
            currentUser.accountBalance = newBalance
            return true
        } catch (e: Exception) {
            dbUserHelper?.endTransaction() // 结束事务
            Logger.getLogger("UserManager").severe("Withdraw failed: ${e.message}")
            return false
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
}
