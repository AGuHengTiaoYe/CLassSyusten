package com.example.a_final_money.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.DBHelper
import com.example.a_final_money.R
import com.example.a_final_money.TransactionType
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.model.User
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class ManageActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var currentUser: User
    private var selectId: Long = -1
    private lateinit var userManager: UserManager

    private lateinit var edtDate: EditText
    private lateinit var edtType: EditText
    private lateinit var edtMoney: EditText
    private lateinit var edtRemark: EditText
    private lateinit var listView: ListView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        userManager = UserManager.getInstance(this)
        dbHelper = DBHelper(this)
        currentUser = userManager.user!!

        // 初始化UI组件
        edtDate = findViewById(R.id.edt_date)
        edtType = findViewById(R.id.edt_type)
        edtMoney = findViewById(R.id.edt_money)
        edtRemark = findViewById(R.id.edt_state)
        listView = findViewById(R.id.recordlistview)

        // 加载交易记录
        loadTransactions()

        // 添加按钮点击事件
        findViewById<Button>(R.id.btn_add).setOnClickListener {
            addCustomTransaction()
        }

        // 更新按钮点击事件
        findViewById<Button>(R.id.btn_update).setOnClickListener {
            updateCustomTransaction()
        }

        // 删除按钮点击事件
        findViewById<Button>(R.id.btn_delete).setOnClickListener {
            deleteCustomTransaction()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTransactions() {
        val transactions = userManager.getUserTransactions(currentUser.userId)
        val list = ArrayList<Map<String, String>>()

        // 只显示自定义类型的交易
        transactions.forEach { transaction ->
            if(transaction.type == TransactionType.CUSTOM) {
                mapOf(
                    "id" to transaction.id.toString(),
                    "date" to transaction.date.toString(),
                    "type" to transaction.type.toString(),
                    "money" to transaction.amount.toString(),
                    "state" to transaction.remark
                ).let { list.add(it as Map<String, String>) }
            }
        }

        val adapter = SimpleAdapter(
            this,
            list,
            R.layout.record_item_layout,
            arrayOf("id", "date", "type", "money", "state"),
            intArrayOf(R.id.list_id, R.id.list_date, R.id.list_type, R.id.list_money, R.id.list_state)
        )

        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position) as Map<String, String>
            selectId = item["id"]?.toLong() ?: -1
            edtDate.setText(item["date"])
            edtType.setText(item["type"])
            edtMoney.setText(item["money"])
            edtRemark.setText(item["state"])
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addCustomTransaction() {
        if (!validateInput()) return

        try {
            val amount = edtMoney.text.toString().toDouble()
            val remark = edtRemark.text.toString()

            val result = dbHelper.addTransaction(
                currentUser.userId.toString(),
                amount,
                TransactionType.CUSTOM,
                remark
            )

            if (result != -1L) {
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
                loadTransactions()
                clearInputs()
            } else {
                Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "操作异常: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCustomTransaction() {
        if (selectId == -1L) {
            Toast.makeText(this, "请先选择要修改的记录", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateInput()) return

        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("amount", edtMoney.text.toString().toDouble())
                put("transaction_remark", edtRemark.text.toString())
            }

            val rowsAffected = db.update(
                "transactions",
                values,
                "transaction_id = ? AND transaction_type = ?",
                arrayOf(selectId.toString(), TransactionType.CUSTOM.name)
            )

            if (rowsAffected > 0) {
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show()
                loadTransactions()
                clearInputs()
            } else {
                Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "操作异常: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCustomTransaction() {
        if (selectId == -1L) {
            Toast.makeText(this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这条记录吗?")
            .setPositiveButton("确定") { _, _ ->
                try {
                    val db = dbHelper.writableDatabase
                    val rowsAffected = db.delete(
                        "transactions",
                        "transaction_id = ? AND transaction_type = ?",
                        arrayOf(selectId.toString(), TransactionType.CUSTOM.name)
                    )

                    if (rowsAffected > 0) {
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
                        loadTransactions()
                        clearInputs()
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "操作异常: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun validateInput(): Boolean {
        return when {
            edtMoney.text.isEmpty() -> {
                Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show()
                false
            }
            edtRemark.text.isEmpty() -> {
                Toast.makeText(this, "请输入备注", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun clearInputs() {
        selectId = -1L
        edtDate.text.clear()
        edtType.text.clear()
        edtMoney.text.clear()
        edtRemark.text.clear()
    }
}