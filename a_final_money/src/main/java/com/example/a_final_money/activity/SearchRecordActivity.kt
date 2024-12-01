package com.example.a_final_money.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.DBHelper
import com.example.a_final_money.R
import com.example.a_final_money.Transaction
import com.example.a_final_money.TransactionType
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.tools.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class SearchRecordActivity : AppCompatActivity() {
    // Define data for spinners
    private val dateData = arrayOf("", "202411", "202412", "202501", "202502", "202503", "202504", "202505", "202506", "202507", "202508", "202509", "202510", "202511", "202512")
    private val typeData = arrayOf("", "充值", "转出","购买","卖出","自定义")

    private lateinit var spinDate: Spinner
    private lateinit var spinType: Spinner
    private lateinit var listView: ListView
    private lateinit var tvShow: TextView

    private var sum = 0.0
    private var selectDate: LocalDate? = null
    private var selectType: String? = null

    private lateinit var dbHelper: DBHelper
    private lateinit var userManager: UserManager

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun selectSumMoney(userId: String) {
        sum = 0.0
        if (selectDate != null){
            val endDate = selectDate!!.plusMonths(1)
            userManager.getUserTransactions(userId,selectDate,endDate)
        }else{
            userManager.getUserTransactions(userId)
        }.forEach{
            sum+=it.amount
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectData(userId: String) {
        val list:ArrayList<Map<String,String>> = arrayListOf()
        // 获取筛选条件的交易记录
        val transactions = if (selectDate != null) {
            val endDate = selectDate!!.plusMonths(1)
            userManager.getUserTransactions(userId, selectDate, endDate)
        } else {
            userManager.getUserTransactions(userId)
        }

        // 根据选择的交易类型进行筛选
        transactions.forEach {
            if (selectType == null || selectType == it.type.toString()) {
                list.add(mapOf(
                    "id" to it.id.toString(),
                    "date" to it.date.toString(),
                    "type" to it.type.toString(),
                    "money" to it.amount.toString(),
                    "state" to it.remark.toString()
                ))
            }
        }


        val simpleAdapter = SimpleAdapter(
            this,
            list,
            R.layout.record_item_layout,
            arrayOf("id", "date", "type", "money", "state"),
            intArrayOf(R.id.list_id, R.id.list_date, R.id.list_type, R.id.list_money, R.id.list_state)
        )

        listView.adapter = simpleAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initClick() {
        // Date spinner item selected listener
        spinDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectDate = dateData[position].toLocalDate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Type spinner item selected listener
        spinType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectType = typeData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Search button click event
        findViewById<Button>(R.id.btn_search).setOnClickListener {
            // Get current user ID using UserManager
            val userId = userManager.getUserId() ?: return@setOnClickListener
            selectData(userId)
        }

        // Calculate total sum button click event
        findViewById<Button>(R.id.btn_calc).setOnClickListener {
            // Get current user ID using UserManager
            val userId = userManager.getUserId() ?: return@setOnClickListener
            selectSumMoney(userId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_record)

        // Initialize UserManager
        userManager = UserManager.getInstance(this)

        // Initialize database helper
        dbHelper = DBHelper(this)

        tvShow = findViewById(R.id.tv_show)

        // Initialize ListView
        listView = findViewById(R.id.searchlistview)

        // Initialize date spinner
        val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateData).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinDate = findViewById(R.id.spin_date)
        spinDate.adapter = dateAdapter

        // Initialize type spinner
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeData).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinType = findViewById(R.id.spin_type)
        spinType.adapter = typeAdapter

        // Initialize click events
        initClick()

        // Initial data load for current user
        val userId = userManager.getUserId() ?: return
        selectData(userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Close database connection
        dbHelper.close()
    }
}
