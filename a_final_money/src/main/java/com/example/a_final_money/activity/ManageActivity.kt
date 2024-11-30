package com.example.a_final_money.activity

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.R
import java.util.*

class ManageActivity : AppCompatActivity() {
    private var sqLiteDatabase: SQLiteDatabase? = null
    private var selectId = -1
    private lateinit var edtDate: EditText
    private lateinit var edtType: EditText
    private lateinit var edtMoney: EditText
    private lateinit var edtState: EditText
    private lateinit var tvTest: TextView

    companion object {
        private const val DATABASE_NAME = "Test.db"
        private const val TABLE_NAME = "record"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_MONEY = "money"
        private const val COLUMN_STATE = "state"

        private val CREATE_TABLE = "create table if not exists $TABLE_NAME(" +
                "$COLUMN_ID integer primary key autoincrement," +
                "$COLUMN_DATE text," +
                "$COLUMN_TYPE text," +
                "$COLUMN_MONEY float," +
                "$COLUMN_STATE text)"
    }

    // 自定义的查询方法
    @SuppressLint("Range")
    private fun selectData() {
        // 遍历整个表
        val sql = "select * from $TABLE_NAME"
        // 把查询数据封装到Cursor
        val cursor = sqLiteDatabase?.rawQuery(sql, null)
        val list = ArrayList<Map<String, String>>()

        // 用 while 循环遍历 Cursor，再把它分别放到 map 中，最后统一存入 list 中，便于调用
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex(COLUMN_ID))
                val date = it.getString(it.getColumnIndex(COLUMN_DATE))
                val type = it.getString(it.getColumnIndex(COLUMN_TYPE))
                val money = it.getFloat(it.getColumnIndex(COLUMN_MONEY))
                val state = it.getString(it.getColumnIndex(COLUMN_STATE))

                val map = mapOf(
                    "id" to id.toString(),
                    "date" to date,
                    "type" to type,
                    "money" to money.toString(),
                    "state" to state
                )
                list.add(map)
            }
        }

        // 创建 SimpleAdapter
        val simpleAdapter = SimpleAdapter(
            applicationContext,
            list,
            R.layout.record_item_layout,
            arrayOf("id", "date", "type", "money", "state"),
            intArrayOf(R.id.list_id, R.id.list_date, R.id.list_type, R.id.list_money, R.id.list_state)
        )

        val listView: ListView = findViewById(R.id.recordlistview)
        // 绑定适配器
        listView.adapter = simpleAdapter

        // 设置 ListView 单击事件
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val tempList = parent as ListView
            val mView = tempList.getChildAt(position)

            val listId: TextView = mView.findViewById(R.id.list_id)
            val listDate: TextView = mView.findViewById(R.id.list_date)
            val listType: TextView = mView.findViewById(R.id.list_type)
            val listMoney: TextView = mView.findViewById(R.id.list_money)
            val listState: TextView = mView.findViewById(R.id.list_state)

            val rid = listId.text.toString()
            val date = listDate.text.toString()
            val type = listType.text.toString()
            val money = listMoney.text.toString()
            val state = listState.text.toString()

            tvTest.text = rid
            edtDate.setText(date)
            edtType.setText(type)
            edtMoney.setText(money)
            edtState.setText(state)
            selectId = rid.toInt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        try {
            sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null)
            sqLiteDatabase?.execSQL(CREATE_TABLE)
            // 执行查询
            selectData()
        } catch (e: SQLException) {
            Toast.makeText(this, "数据库异常!", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        tvTest = findViewById(R.id.tv_test)
        edtDate = findViewById(R.id.edt_date)
        edtType = findViewById(R.id.edt_type)
        edtMoney = findViewById(R.id.edt_money)
        edtState = findViewById(R.id.edt_state)

        // 新增按键
        val btnAdd: Button = findViewById(R.id.btn_add)
        btnAdd.setOnClickListener {
            if (edtDate.text.isEmpty() || edtType.text.isEmpty() ||
                edtMoney.text.isEmpty() || edtState.text.isEmpty()) {
                Toast.makeText(this, "数据不能为空!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val date = edtDate.text.toString()
            val type = edtType.text.toString()
            val money = edtMoney.text.toString()
            val state = edtState.text.toString()

            // 定义添加数据的 SQL 语句
            val sql = "insert into $TABLE_NAME($COLUMN_DATE,$COLUMN_TYPE,$COLUMN_MONEY,$COLUMN_STATE) " +
                    "values('$date','$type','$money','$state')"

            // 执行 SQL 语句
            sqLiteDatabase?.execSQL(sql)
            Toast.makeText(applicationContext, "新增数据成功!", Toast.LENGTH_LONG).show()

            // 刷新显示列表
            selectData()

            // 消除数据
            clearInputFields()
        }

        // 修改按键
        val btnUpdate: Button = findViewById(R.id.btn_update)
        btnUpdate.setOnClickListener {
            // 无选择提示
            if (selectId == -1) {
                Toast.makeText(applicationContext, "请选择要修改的行!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 判断是否有空数据
            if (edtDate.text.isEmpty() || edtType.text.isEmpty() ||
                edtMoney.text.isEmpty() || edtState.text.isEmpty()) {
                Toast.makeText(applicationContext, "数据不能为空!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val date = edtDate.text.toString()
            val type = edtType.text.toString()
            val money = edtMoney.text.toString()
            val state = edtState.text.toString()

            // 定义修改数据的 SQL 语句
            val sql = "update $TABLE_NAME set $COLUMN_DATE='$date'," +
                    "$COLUMN_TYPE='$type'," +
                    "$COLUMN_MONEY='$money'," +
                    "$COLUMN_STATE='$state' where $COLUMN_ID=$selectId"

            // 执行 SQL 语句
            sqLiteDatabase?.execSQL(sql)
            Toast.makeText(applicationContext, "修改数据成功!", Toast.LENGTH_LONG).show()

            // 刷新显示列表
            selectData()
            selectId = -1

            // 消除数据
            clearInputFields()
        }

        // 删除按键
        val btnDelete: Button = findViewById(R.id.btn_delete)
        btnDelete.setOnClickListener {
            // 无选择提示
            if (selectId == -1) {
                Toast.makeText(this, "请选择要删除的行!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 定义删除对话框
            AlertDialog.Builder(this)
                .setTitle("删除操作")
                .setMessage("确定删除？此操作不可逆，请慎重选择！")
                .setPositiveButton("确定") { _, _ ->
                    // 定义删除的 SQL 语句
                    val sql = "delete from $TABLE_NAME where $COLUMN_ID=$selectId"

                    // 执行 SQL 语句
                    sqLiteDatabase?.execSQL(sql)

                    // 刷新显示列表
                    Toast.makeText(applicationContext, "删除数据成功!", Toast.LENGTH_LONG).show()
                    selectData()
                    selectId = -1

                    // 清除数据
                    clearInputFields()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    // 清除输入字段的辅助方法
    private fun clearInputFields() {
        tvTest.text = ""
        edtDate.text.clear()
        edtType.text.clear()
        edtMoney.text.clear()
        edtState.text.clear()
    }
}