package com.example.a_final_money.activity

import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.DBHelper
import com.example.a_final_money.R
import com.example.a_final_money.adapter.ProductAdapter
import com.example.a_final_money.manager.InvestmentManager
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.model.FinancialProduct
import com.example.a_final_money.model.User
import java.time.LocalDate
import java.util.UUID

class ProductListActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var investmentManager: InvestmentManager
    private lateinit var listView: ListView
    private lateinit var purchaseAmountEditText: EditText
    private lateinit var purchaseButton: Button
    private lateinit var dbHelper: DBHelper
    private lateinit var user: User

    private var selectedProduct: FinancialProduct? = null
    private lateinit var productList: List<FinancialProduct>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        // Initialize components
        initializeComponents()

        // Load and display products
        loadFinancialProducts()

        // Set up product selection listener
        setupProductSelection()

        // Set up purchase button logic
        setupPurchaseButton()
    }

    private fun initializeComponents() {
        userManager = UserManager.getInstance(this)
        user = userManager.user ?: throw IllegalStateException("User not logged in")

        // Initialize Investment Manager
        dbHelper = DBHelper(this)
        investmentManager = InvestmentManager(dbHelper)

        // Find views
        listView = findViewById(R.id.lvProducts)
        purchaseAmountEditText = findViewById(R.id.etPurchaseAmount)
        purchaseButton = findViewById(R.id.btnPurchase)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadFinancialProducts() {
        // Load all available financial products
        productList = dbHelper.getAllFinancialProducts()
        val adapter = ProductAdapter(this, productList)
        listView.adapter = adapter
    }

    private fun setupProductSelection() {
        listView.setOnItemClickListener { _, _, position, _ ->
            selectedProduct = productList[position]
            Toast.makeText(
                this,
                "选择了产品：${selectedProduct?.productName}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPurchaseButton() {
        purchaseButton.setOnClickListener {
            // Validate and process purchase
            processProductPurchase()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processProductPurchase() {
        // Validate product selection
        val product = selectedProduct
            ?: run {
                Toast.makeText(this, "请先选择一个产品", Toast.LENGTH_SHORT).show()
                return
            }

        // Validate amount
        val amountText = purchaseAmountEditText.text.toString()
        val amount = amountText.toDoubleOrNull()
            ?: run {
                Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show()
                return
            }

        // Create a new product instance for the user
        val userProductId = UUID.randomUUID().toString()
        val userProduct = product.copy(
            productId = userProductId,
            investmentAmount = amount,
            investmentDate = LocalDate.now()
        )

        // Use InvestmentManager to buy the product
        when (val result = investmentManager.buyProduct(user, userProduct)) {
            InvestmentManager.InvestmentResult.SUCCESS -> {
                Toast.makeText(
                    this,
                    "购买成功！当前余额：${String.format("%.2f", user.accountBalance)}",
                    Toast.LENGTH_SHORT
                ).show()

                // Clear input and reset selection
                purchaseAmountEditText.text.clear()
                selectedProduct = null
            }
            InvestmentManager.InvestmentResult.INSUFFICIENT_BALANCE -> {
                Toast.makeText(this, "余额不足，无法购买", Toast.LENGTH_SHORT).show()
            }
            InvestmentManager.InvestmentResult.TRANSACTION_FAILED -> {
                Toast.makeText(this, "交易处理失败，请重试", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "未知错误，购买失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}