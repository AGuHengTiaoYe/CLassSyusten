package com.example.a_final_money.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a_final_money.DBHelper
import com.example.a_final_money.R
import com.example.a_final_money.manager.UserManager
import com.example.a_final_money.model.User

// 注册页面逻辑
class RegisterActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        userManager = UserManager.getInstance(this)

        // 使用 lateinit 或直接声明
        val edtRid: EditText = findViewById(R.id.edt_rid)
        val edtRpwd: EditText = findViewById(R.id.edt_rpwd)

        // 注册按键
        val btnRegisterUser: Button = findViewById(R.id.btn_registeruser)
        btnRegisterUser.setOnClickListener {
            val userId = edtRid.text.toString()
            val userPwd = edtRpwd.text.toString()
            if(userId.isEmpty() || userPwd.isEmpty()){
                return@setOnClickListener
            }

            if (userManager.register(userId, userPwd) > 0) {
                Toast.makeText(applicationContext, "注册成功", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, MainActivity::class.java)
                finish()
                startActivity(intent)
            } else {
                Toast.makeText(
                    applicationContext,
                    "您已经注册过此账户，请返回登录",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}