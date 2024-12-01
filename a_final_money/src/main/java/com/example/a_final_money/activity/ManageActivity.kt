package com.example.a_final_money.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.DBHelper
import com.example.a_final_money.R
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.model.User
import java.time.LocalDate

class ManageActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var currentUser: User
    private var selectId: Long = -1

    private lateinit var edtDate: EditText
    private lateinit var edtType: EditText
    private lateinit var edtMoney: EditText
    private lateinit var edtState: EditText
    private lateinit var tvTest: TextView
    private lateinit var listView: ListView
    private lateinit var userManager: UserManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)
        userManager = UserManager.getInstance(this)
        // Initialize DBHelper and get current user
        dbHelper = DBHelper(this)
        currentUser = intent.getSerializableExtra("USER") as User

        // Initialize UI components
        tvTest = findViewById(R.id.tv_test)
        edtDate = findViewById(R.id.edt_date)
        edtType = findViewById(R.id.edt_type)
        edtMoney = findViewById(R.id.edt_money)
        edtState = findViewById(R.id.edt_state)
        listView = findViewById(R.id.recordlistview)

        // Populate list view with transactions
        loadTransactions()

        // Add button
        findViewById<Button>(R.id.btn_add).setOnClickListener {
            addTransaction()
        }

        // Update button
        findViewById<Button>(R.id.btn_update).setOnClickListener {
            updateTransaction()
        }

        // Delete button
        findViewById<Button>(R.id.btn_delete).setOnClickListener {
            deleteTransaction()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTransactions() {
        try {
            // Query transactions (you might want to modify this to get user-specific transactions)
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "transactions",
                null,
                "user_id = ?",
                arrayOf(currentUser.userId),
                null,
                null,
                null
            )

            val list = ArrayList<Map<String, String>>()

            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow("transaction_id"))
                    val date = it.getString(it.getColumnIndexOrThrow("transaction_date"))
                    val amount = it.getDouble(it.getColumnIndexOrThrow("amount"))
                    val type = it.getString(it.getColumnIndexOrThrow("transaction_type"))
                    val state = "" // You might want to add a state column to transactions

                    val map = mapOf(
                        "id" to id.toString(),
                        "date" to date,
                        "type" to type,
                        "money" to amount.toString(),
                        "state" to state
                    )
                    list.add(map)
                }
            }

            val simpleAdapter = SimpleAdapter(
                applicationContext,
                list,
                R.layout.record_item_layout,
                arrayOf("id", "date", "type", "money", "state"),
                intArrayOf(R.id.list_id, R.id.list_date, R.id.list_type, R.id.list_money, R.id.list_state)
            )

            listView.adapter = simpleAdapter

            // Set item click listener
            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, _ ->
                val selectedItem = parent.getItemAtPosition(position) as Map<String, String>

                selectId = selectedItem["id"]?.toLongOrNull() ?: -1
                edtDate.setText(selectedItem["date"])
                edtType.setText(selectedItem["type"])
                edtMoney.setText(selectedItem["money"])
                edtState.setText(selectedItem["state"] ?: "")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "加载数据失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTransaction() {
        // Validate input
        if (validateInput()) {
            try {
                val date = edtDate.text.toString()
                val type = edtType.text.toString()
                val amount = edtMoney.text.toString().toDouble()

                // Add transaction using DBHelper
                val transactionId = dbHelper.addTransaction(
                    currentUser.userId,
                    amount,
                    type
                )

                if (transactionId != -1L) {
                    Toast.makeText(this, "新增数据成功!", Toast.LENGTH_LONG).show()
                    loadTransactions()
                    clearInputFields()
                } else {
                    Toast.makeText(this, "新增数据失败!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "新增数据异常: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateTransaction() {
        // Note: The current DBHelper doesn't have a direct update transaction method
        // You might need to add this method to DBHelper or implement a different approach
        if (selectId == -1L) {
            Toast.makeText(this, "请选择要修改的行!", Toast.LENGTH_LONG).show()
            return
        }

        if (validateInput()) {
            Toast.makeText(this, "更新功能暂未实现", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteTransaction() {
        if (selectId == -1L) {
            Toast.makeText(this, "请选择要删除的行!", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("删除操作")
            .setMessage("确定删除？此操作不可逆，请慎重选择！")
            .setPositiveButton("确定") { _, _ ->
                try {
                    val db = dbHelper.writableDatabase
                    val rowsAffected = db.delete(
                        "transactions",
                        "transaction_id = ?",
                        arrayOf(selectId.toString())
                    )

                    if (rowsAffected > 0) {
                        Toast.makeText(applicationContext, "删除数据成功!", Toast.LENGTH_LONG).show()
                        loadTransactions()
                        selectId = -1
                        clearInputFields()
                    } else {
                        Toast.makeText(applicationContext, "删除数据失败!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "删除数据异常: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun validateInput(): Boolean {
        if (edtDate.text.isEmpty() || edtType.text.isEmpty() ||
            edtMoney.text.isEmpty()) {
            Toast.makeText(this, "数据不能为空!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun clearInputFields() {
        tvTest.text = ""
        edtDate.text.clear()
        edtType.text.clear()
        edtMoney.text.clear()
        edtState.text.clear()
    }
}