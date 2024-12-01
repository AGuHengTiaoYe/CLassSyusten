package com.example.a_final_money.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.R

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.a_final_money.DBHelper
import com.example.a_final_money.FinancialProductInitializer
import com.example.a_final_money.manager.UserManager

// 登录页面逻辑
class MainActivity : AppCompatActivity() {
    private lateinit var edtId: EditText
    private lateinit var edtPwd: EditText
    private lateinit var userManager: UserManager


    private val needInitial = false
    @RequiresApi(Build.VERSION_CODES.O)
    fun initialProducts(){
        if(needInitial){
            FinancialProductInitializer(DBHelper(this)).initializeProducts()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userManager = UserManager.getInstance(this)
        initialProducts()
        edtId = findViewById(R.id.edt_uid)
        edtPwd = findViewById(R.id.edt_upwd)




        // 登录按键
        val btnLogin: Button = findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
            try {
                val userId = edtId.text.toString()
                val userPwd = edtPwd.text.toString()

                val user = userManager.login(userId, userPwd)

                // 登录成功跳转对应类型界面
                if (user != null) {
                    Toast.makeText(
                        applicationContext,
                        "${user.userId} 登录成功",
                        Toast.LENGTH_SHORT
                    ).show()

                    val list = arrayListOf(user)
                    val intent = Intent(applicationContext, UserContentActivity::class.java).apply {
                        putParcelableArrayListExtra("LoginUser", list)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "登录失败，密码错误或账号不存在！",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "数据库异常",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 注册按键
        val btnRegister: Button = findViewById(R.id.btn_register)
        btnRegister.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}