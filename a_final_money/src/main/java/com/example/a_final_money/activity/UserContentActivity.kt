package com.example.a_final_money.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.R
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.model.User

// 用户个人中心页面逻辑
class UserContentActivity : AppCompatActivity() {
    private lateinit var list: ArrayList<User>
    private lateinit var userManager: UserManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_content)

        userManager = UserManager.getInstance(this)
        // 获取传递的用户信息
        list = intent.getParcelableArrayListExtra("LoginUser") ?: arrayListOf()
        val user = list.firstOrNull() ?: return

        val userName: TextView = findViewById(R.id.user_name)
        userName.text = user.userName.ifEmpty {
            user.userId
        }


        // 收支管理
        val btnRecordManage: ImageView = findViewById(R.id.btn_recordmanage)
        btnRecordManage.setOnClickListener {
            val intent = Intent(applicationContext, ManageActivity::class.java)
            startActivity(intent)
        }

        // 账户查询
        val btnSearchRecord: ImageView = findViewById(R.id.btn_searchrecord)
        btnSearchRecord.setOnClickListener {
            val intent = Intent(applicationContext, SearchRecordActivity::class.java)
            startActivity(intent)
        }

        // 收支统计
        val btnCalcMoney: ImageView = findViewById(R.id.user_info)
        btnCalcMoney.setOnClickListener {
            val intent = Intent(applicationContext, AccountInfoActivity::class.java)
            startActivity(intent)
        }

        // 理财产品
        val financialProduct: ImageView = findViewById(R.id.finance_product)
        financialProduct.setOnClickListener {
            val intent = Intent(applicationContext, ProductListActivity::class.java)
            startActivity(intent)
        }

        // 退出按键
        val btnExit: ImageView = findViewById(R.id.btn_exit)
        btnExit.setOnClickListener {
            // 使用 Kotlin 的 DSL 风格创建 AlertDialog
            AlertDialog.Builder(this)
                .setTitle("退出操作")
                .setMessage("确定退出？")
                .setPositiveButton("确定") { _, _ ->
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("继续留下！") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
}