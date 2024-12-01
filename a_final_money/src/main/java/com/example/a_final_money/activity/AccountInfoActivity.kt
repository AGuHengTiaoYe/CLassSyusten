package com.example.a_final_money.activity

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.R
import com.example.a_final_money.adapter.FinancialProductAdapter
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.model.FinancialProduct
import com.example.a_final_money.model.ProductType
import com.example.a_final_money.model.User
import java.time.LocalDate

class AccountInfoActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvAccountBalance: TextView
    private lateinit var lvFinancialProducts: ListView
    private lateinit var btnRecharge: Button
    private lateinit var btnTransferOut: Button

    // 模拟一个用户对象
    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var user:User
    private lateinit var userManager: UserManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)

        userManager = UserManager.getInstance(this)
        user = userManager.user!!


        // 绑定视图
        tvUserName = findViewById(R.id.tvUserName)
        tvAccountBalance = findViewById(R.id.tvAccountBalance)
        lvFinancialProducts = findViewById(R.id.lvFinancialProducts)
        btnRecharge = findViewById(R.id.btnRecharge)
        btnTransferOut = findViewById(R.id.btnTransferOut)

        // 初始化界面
        updateUI()

        // 设置充值按钮点击事件
        btnRecharge.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            builder.setTitle("请输入充值金额")
                .setView(input)
                .setPositiveButton("确定") { _, _ ->
                    val amountStr = input.text.toString()
                    if (amountStr.isNotEmpty()) {
                        val rechargeAmount = amountStr.toDouble()
                        if (userManager.deposit(rechargeAmount)) {
                            Toast.makeText(this, "充值成功：+${rechargeAmount}", Toast.LENGTH_SHORT).show()
                            updateUI()
                        }
                    }
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.cancel()
                }
            builder.show()
        }

// 设置转出按钮点击事件
        btnTransferOut.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            builder.setTitle("请输入转出金额")
                .setView(input)
                .setPositiveButton("确定") { _, _ ->
                    val amountStr = input.text.toString()
                    if (amountStr.isNotEmpty()) {
                        val transferAmount = amountStr.toDouble()
                        if (userManager.withdraw(transferAmount)) {
                            Toast.makeText(this, "转出成功：-${transferAmount}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "余额不足，无法转出", Toast.LENGTH_SHORT).show()
                        }
                        updateUI()
                    }
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.cancel()
                }
            builder.show()
        }
    }

    // 更新界面
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUI() {
        tvUserName.text = "用户名：${user.userName}"
        tvAccountBalance.text = "账户余额：${user.accountBalance}"

        // 显示持有的理财产品
        val productAdapter = FinancialProductAdapter(this, user.ownedFinancialProducts)
        lvFinancialProducts.adapter = productAdapter
    }
}
