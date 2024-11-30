package com.example.a_final_money.activity

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.R
import java.util.*

class SearchRecordActivity : AppCompatActivity() {
    // 定义spinner中的数据
    private val dateData = arrayOf("", "202411", "202412", "202501", "202502", "202503", "202504", "202505", "202506", "202507", "202508", "202509", "202510", "202511", "202512")
    private val typeData = arrayOf("", "收入", "支出")

    private lateinit var spinDate: Spinner
    private lateinit var spinType: Spinner
    private lateinit var listView: ListView
    private lateinit var tvShow: TextView

    private var sum = 0f
    private var selectDate: String? = null
    private var selectType: String? = null

    private lateinit var sqLiteDatabase: SQLiteDatabase

    companion object {
        private const val DATABASE_NAME = "Test.db"
        private const val TABLE_NAME = "record"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_MONEY = "money"
        private const val COLUMN_STATE = "state"
    }

    @SuppressLint("Range")
    private fun selectSumMoney() {
        // 自定义查询的sql语句
        val sql = when {
            TextUtils.isEmpty(selectDate) && TextUtils.isEmpty(selectType) -> "select * from $TABLE_NAME"
            !TextUtils.isEmpty(selectDate) && TextUtils.isEmpty(selectType) -> "select * from $TABLE_NAME where date='$selectDate'"
            TextUtils.isEmpty(selectDate) && !TextUtils.isEmpty(selectType) -> "select * from $TABLE_NAME where type='$selectType'"
            else -> "select * from $TABLE_NAME where date='$selectDate' and type='$selectType'"
        }

        val cursor = sqLiteDatabase.rawQuery(sql, null)
        var sum = 0f

        while (cursor.moveToNext()) {
            val money = cursor.getFloat(cursor.getColumnIndex(COLUMN_MONEY))
            sum += money
        }

        tvShow.text = sum.toString()
        cursor.close()
    }

    @SuppressLint("Range")
    private fun selectData() {
        // 自定义查询的sql语句
        val sql = when {
            TextUtils.isEmpty(selectDate) && TextUtils.isEmpty(selectType) -> "select * from $TABLE_NAME"
            !TextUtils.isEmpty(selectDate) && TextUtils.isEmpty(selectType) -> "select * from $TABLE_NAME where date='$selectDate'"
            TextUtils.isEmpty(selectDate) && !TextUtils.isEmpty(selectType) -> "select * from $TABLE_NAME where type='$selectType'"
            else -> "select * from $TABLE_NAME where date='$selectDate' and type='$selectType'"
        }

        val cursor = sqLiteDatabase.rawQuery(sql, null)
        val list = ArrayList<Map<String, String>>()

        if (cursor.count == 0) {
            Toast.makeText(applicationContext, "无数据", Toast.LENGTH_SHORT).show()
        } else {
            listView.visibility = View.VISIBLE
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                val type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE))
                val money = cursor.getFloat(cursor.getColumnIndex(COLUMN_MONEY))
                val state = cursor.getString(cursor.getColumnIndex(COLUMN_STATE))

                val map = mapOf(
                    "id" to id.toString(),
                    "date" to date,
                    "type" to type,
                    "money" to money.toString(),
                    "state" to state
                )
                list.add(map)
            }

            val simpleAdapter = SimpleAdapter(
                applicationContext,
                list,
                R.layout.record_item_layout,
                arrayOf("id", "date", "type", "money", "state"),
                intArrayOf(R.id.list_id, R.id.list_date, R.id.list_type, R.id.list_money, R.id.list_state)
            )
            listView.adapter = simpleAdapter
        }
        cursor.close()
    }

    private fun initClick() {
        // 时间事件
        spinDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectDate = dateData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 类别事件
        spinType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectType = typeData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btn_search).setOnClickListener {
            selectData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_record)

        tvShow = findViewById(R.id.tv_show)

        try {
            // 打开数据库，如果是第一次会创建该数据库，模式为MODE_PRIVATE
            sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null)

            // 执行查询
            listView = findViewById(R.id.searchlistview)
            selectData()
        } catch (e: SQLException) {
            Toast.makeText(this, "数据库异常!", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateData).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinDate = findViewById(R.id.spin_date)
        spinDate.adapter = dateAdapter

        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeData).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinType = findViewById(R.id.spin_type)
        spinType.adapter = typeAdapter

        initClick()

        findViewById<Button>(R.id.btn_calc).setOnClickListener {
            selectSumMoney()
        }
    }
}