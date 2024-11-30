package com.example.a_final_money

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.a_final_money.model.FinancialProduct
import com.example.a_final_money.model.ProductType
import com.example.a_final_money.model.User
import java.time.LocalDate
import java.util.logging.Logger

// Enhanced DBHelper with more transaction and error handling
class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DB_NAME = "FinancialApp.db"
        private const val DATABASE_VERSION = 5
        private const val TABLE_USER = "userinfo"
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_FINANCIAL_PRODUCTS = "financial_products"

        // User table columns
        private const val COLUMN_USER_ID = "uid"
        private const val COLUMN_USERPWD = "upwd"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_BALANCE = "account_balance"

        // Transaction table columns
        private const val COLUMN_TRANSACTION_ID = "transaction_id"
        private const val COLUMN_TRANSACTION_USER = "user_id"
        private const val COLUMN_TRANSACTION_AMOUNT = "amount"
        private const val COLUMN_TRANSACTION_TYPE = "transaction_type"
        private const val COLUMN_TRANSACTION_DATE = "transaction_date"
        private const val COLUMN_TRANSACTION_PRODUCT_ID = "product_id"

        // Financial Product table columns
        private const val COLUMN_PRODUCT_ID = "product_id"
        private const val COLUMN_PRODUCT_NAME = "product_name"
        private const val COLUMN_ANNUAL_RATE = "annual_rate"
        private const val COLUMN_INVESTMENT_AMOUNT = "investment_amount"
        private const val COLUMN_INVESTMENT_DATE = "investment_date"
        private const val COLUMN_PRODUCT_DURATION = "duration"
        private const val COLUMN_PRODUCT_TYPE = "product_type"
    }

    // Create table SQL statements
    private val CREATE_USER_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_USER(
            $COLUMN_USER_ID TEXT NOT NULL PRIMARY KEY,
            $COLUMN_USERPWD TEXT NOT NULL,
            $COLUMN_USERNAME TEXT,
            $COLUMN_BALANCE REAL DEFAULT 0.0
        )
    """

    private val CREATE_TRANSACTIONS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS(
            $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TRANSACTION_USER TEXT NOT NULL,
            $COLUMN_TRANSACTION_AMOUNT REAL NOT NULL,
            $COLUMN_TRANSACTION_TYPE TEXT NOT NULL,
            $COLUMN_TRANSACTION_DATE TEXT NOT NULL,
            $COLUMN_TRANSACTION_PRODUCT_ID TEXT,
            FOREIGN KEY($COLUMN_TRANSACTION_USER) REFERENCES $TABLE_USER($COLUMN_USER_ID)
        )
    """

    private val CREATE_FINANCIAL_PRODUCTS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_FINANCIAL_PRODUCTS(
            $COLUMN_PRODUCT_ID TEXT PRIMARY KEY,
            $COLUMN_USER_ID TEXT NOT NULL,
            $COLUMN_PRODUCT_NAME TEXT NOT NULL,
            $COLUMN_ANNUAL_RATE REAL NOT NULL,
            $COLUMN_INVESTMENT_AMOUNT REAL NOT NULL,
            $COLUMN_INVESTMENT_DATE TEXT NOT NULL,
            $COLUMN_PRODUCT_DURATION INTEGER NOT NULL,
            $COLUMN_PRODUCT_TYPE TEXT NOT NULL,
            FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_USER_ID)
        )
    """

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_TRANSACTIONS_TABLE)
        db.execSQL(CREATE_FINANCIAL_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            // Comprehensive upgrade path
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_FINANCIAL_PRODUCTS")
            onCreate(db)
        }
    }

    // Enhanced transaction methods with more robust error handling
    fun <T> executeInTransaction(operation: () -> T): T? {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val result = operation()
            db.setTransactionSuccessful()
            result
        } catch (e: Exception) {
            Logger.getLogger("DBHelper").severe("Transaction failed: ${e.message}")
            null
        } finally {
            db.endTransaction()
        }
    }

    // Add financial product for a user
    fun addFinancialProduct(userId: String, product: FinancialProduct): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_PRODUCT_ID, product.productId)
            put(COLUMN_USER_ID, userId)
            put(COLUMN_PRODUCT_NAME, product.productName)
            put(COLUMN_ANNUAL_RATE, product.annualRate)
            put(COLUMN_INVESTMENT_AMOUNT, product.investmentAmount)
            put(COLUMN_INVESTMENT_DATE, product.investmentDate.toString())
            put(COLUMN_PRODUCT_DURATION, product.duration)
            put(COLUMN_PRODUCT_TYPE, product.productType.name)
        }
        return db.insert(TABLE_FINANCIAL_PRODUCTS, null, contentValues) != -1L
    }

    // Retrieve user's financial products
    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserFinancialProducts(userId: String): List<FinancialProduct> {
        val db = readableDatabase
        val products = mutableListOf<FinancialProduct>()
        val cursor = db.query(
            TABLE_FINANCIAL_PRODUCTS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    FinancialProduct(
                        productId = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        productName = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        annualRate = it.getDouble(it.getColumnIndexOrThrow(COLUMN_ANNUAL_RATE)),
                        investmentAmount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_INVESTMENT_AMOUNT)),
                        investmentDate = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(COLUMN_INVESTMENT_DATE))),
                        duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_PRODUCT_DURATION)),
                        productType = ProductType.valueOf(it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_TYPE)))
                    )
                )
            }
        }
        return products
    }

    // Remove financial product
    fun removeFinancialProduct(productId: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_FINANCIAL_PRODUCTS, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId)) > 0
    }

    // 更新用户余额
    fun updateUserBalance(userId: String, newBalance: Double): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_BALANCE, newBalance)
        }
        return db.update(
            TABLE_USER,
            contentValues,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId)
        )
    }
    // 记录交易
    @RequiresApi(Build.VERSION_CODES.O)
    fun addTransaction(userId: String, amount: Double, type: String): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TRANSACTION_USER, userId)
            put(COLUMN_TRANSACTION_AMOUNT, amount)
            put(COLUMN_TRANSACTION_TYPE, type)
            put(COLUMN_TRANSACTION_DATE, LocalDate.now().toString())
        }
        return db.insert(TABLE_TRANSACTIONS, null, contentValues)
    }
    // Enhanced method to add transaction with optional product ID
    @RequiresApi(Build.VERSION_CODES.O)
    fun addTransaction(
        userId: String,
        productId: String? = null,
        amount: Double,
        type: String
    ): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TRANSACTION_USER, userId)
            put(COLUMN_TRANSACTION_AMOUNT, amount)
            put(COLUMN_TRANSACTION_TYPE, type)
            put(COLUMN_TRANSACTION_DATE, LocalDate.now().toString())
            // Nullable column for product ID
            productId?.let { put(COLUMN_TRANSACTION_PRODUCT_ID, it) }
        }
        return db.insert(TABLE_TRANSACTIONS, null, contentValues)
    }


    // 开始事务
    fun beginTransaction() {
        writableDatabase.beginTransaction()
    }

    // 提交事务
    fun setTransactionSuccessful() {
        writableDatabase.setTransactionSuccessful()
    }

    // 结束事务
    fun endTransaction() {
        writableDatabase.endTransaction()
    }

    // 用户注册
    fun registerUser(user: User): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_ID, user.userId)
            put(COLUMN_USERPWD, user.userPwd)
            put(COLUMN_USERNAME, user.userName)
            put(COLUMN_BALANCE, 0.0)
        }
        return db.insert(TABLE_USER, null, contentValues)
    }

    // 用户登录
    fun userLogin(userId: String, userPwd: String): User? {
        val db = readableDatabase
        return db.query(
            TABLE_USER,
            arrayOf(COLUMN_USER_ID, COLUMN_USERPWD, COLUMN_USERNAME, COLUMN_BALANCE),
            "$COLUMN_USER_ID=? AND $COLUMN_USERPWD=?",
            arrayOf(userId, userPwd),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                User(
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    userPwd = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERPWD)),
                    userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    accountBalance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE))
                )
            } else {
                null
            }
        }
    }

    // 修改用户名
    fun updateUserName(userId: String, newUserName: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USERNAME, newUserName)
        }
        return db.update(
            TABLE_USER,
            contentValues,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId)
        )
    }



    // Method to get all financial products across all users
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllFinancialProducts(): List<FinancialProduct> {
        val db = readableDatabase
        val products = mutableListOf<FinancialProduct>()
        val cursor = db.query(
            TABLE_FINANCIAL_PRODUCTS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    FinancialProduct(
                        productId = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        productName = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        annualRate = it.getDouble(it.getColumnIndexOrThrow(COLUMN_ANNUAL_RATE)),
                        investmentAmount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_INVESTMENT_AMOUNT)),
                        investmentDate = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(COLUMN_INVESTMENT_DATE))),
                        duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_PRODUCT_DURATION)),
                        productType = ProductType.valueOf(it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_TYPE)))
                    )
                )
            }
        }
        return products
    }

    // Method to get financial products filtered by various criteria
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilteredFinancialProducts(
        productType: ProductType? = null,
        minInvestmentAmount: Double? = null,
        maxInvestmentAmount: Double? = null,
        minAnnualRate: Double? = null,
        maxAnnualRate: Double? = null
    ): List<FinancialProduct> {
        val db = readableDatabase
        val products = mutableListOf<FinancialProduct>()

        // Build dynamic where clause
        val whereClause = buildString {
            val conditions = mutableListOf<String>()
            productType?.let { conditions.add("$COLUMN_PRODUCT_TYPE = ?") }
            minInvestmentAmount?.let { conditions.add("$COLUMN_INVESTMENT_AMOUNT >= ?") }
            maxInvestmentAmount?.let { conditions.add("$COLUMN_INVESTMENT_AMOUNT <= ?") }
            minAnnualRate?.let { conditions.add("$COLUMN_ANNUAL_RATE >= ?") }
            maxAnnualRate?.let { conditions.add("$COLUMN_ANNUAL_RATE <= ?") }

            if (conditions.isNotEmpty()) {
                append(conditions.joinToString(" AND "))
            }
        }

        // Prepare selection arguments
        val selectionArgs = mutableListOf<String>().apply {
            productType?.let { add(it.name) }
            minInvestmentAmount?.let { add(it.toString()) }
            maxInvestmentAmount?.let { add(it.toString()) }
            minAnnualRate?.let { add(it.toString()) }
            maxAnnualRate?.let { add(it.toString()) }
        }

        val cursor = db.query(
            TABLE_FINANCIAL_PRODUCTS,
            null,
            whereClause.takeIf { it.isNotEmpty() },
            selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray(),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    FinancialProduct(
                        productId = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        productName = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        annualRate = it.getDouble(it.getColumnIndexOrThrow(COLUMN_ANNUAL_RATE)),
                        investmentAmount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_INVESTMENT_AMOUNT)),
                        investmentDate = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(COLUMN_INVESTMENT_DATE))),
                        duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_PRODUCT_DURATION)),
                        productType = ProductType.valueOf(it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCT_TYPE)))
                    )
                )
            }
        }
        return products
    }






    // 添加一个方法用于填充初始理财产品
    @RequiresApi(Build.VERSION_CODES.O)
    fun seedFinancialProducts() {
        val db = writableDatabase

        // 预定义一些理财产品
        val initialProducts = listOf(
            FinancialProduct(
                productId = "FUND_001",
                productName = "稳健成长基金",
                annualRate = 4.5,
                investmentAmount = 5000.0,
                investmentDate = LocalDate.now(),
                duration = 12,
                productType = ProductType.FUND
            ),
            FinancialProduct(
                productId = "BOND_001",
                productName = "国债债券基金",
                annualRate = 3.8,
                investmentAmount = 3000.0,
                investmentDate = LocalDate.now(),
                duration = 6,
                productType = ProductType.BOND
            ),
            FinancialProduct(
                productId = "DEPOSIT_001",
                productName = "半年定期存款",
                annualRate = 3.2,
                investmentAmount = 10000.0,
                investmentDate = LocalDate.now(),
                duration = 6,
                productType = ProductType.FIXED_DEPOSIT
            ),
            FinancialProduct(
                productId = "STOCK_FUND_001",
                productName = "蓝筹股票基金",
                annualRate = 6.5,
                investmentAmount = 8000.0,
                investmentDate = LocalDate.now(),
                duration = 24,
                productType = ProductType.STOCK_FUND
            ),
            FinancialProduct(
                productId = "MONEY_MARKET_001",
                productName = "货币市场基金",
                annualRate = 2.8,
                investmentAmount = 2000.0,
                investmentDate = LocalDate.now(),
                duration = 3,
                productType = ProductType.MONEY_MARKET
            )
        )

        // 使用事务批量插入
        db.beginTransaction()
        try {
            initialProducts.forEach { product ->
                val contentValues = ContentValues().apply {
                    put(COLUMN_PRODUCT_ID, product.productId)
                    put(COLUMN_USER_ID, "SYSTEM") // 系统预置产品
                    put(COLUMN_PRODUCT_NAME, product.productName)
                    put(COLUMN_ANNUAL_RATE, product.annualRate)
                    put(COLUMN_INVESTMENT_AMOUNT, product.investmentAmount)
                    put(COLUMN_INVESTMENT_DATE, product.investmentDate.toString())
                    put(COLUMN_PRODUCT_DURATION, product.duration)
                    put(COLUMN_PRODUCT_TYPE, product.productType.name)
                }

                // 插入产品，如果已存在则忽略
                db.insertWithOnConflict(
                    TABLE_FINANCIAL_PRODUCTS,
                    null,
                    contentValues,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // 可选：添加一个方法清除所有预置的理财产品
    fun clearSeedProducts() {
        val db = writableDatabase
        db.delete(TABLE_FINANCIAL_PRODUCTS, "$COLUMN_USER_ID = ?", arrayOf("SYSTEM"))
    }
}

// 使用示例
class FinancialProductInitializer(private val dbHelper: DBHelper) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun initializeProducts() {
        // 首次使用应用时调用
        dbHelper.seedFinancialProducts()
    }
}