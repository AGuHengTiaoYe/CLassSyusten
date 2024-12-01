package com.example.a_final_money.model

import android.os.Parcel
import android.os.Parcelable
import java.math.BigDecimal


// 使用 data class 和主构造函数
data class User(
    val userId: String,
    val userPwd: String,
    var userName: String = "",
    var accountBalance: Double = 0.0, // Double更简单
    var ownedFinancialProducts: MutableList<FinancialProduct> = mutableListOf() // 持有的理财产品
) : Parcelable {

    // 伴生对象中定义 CREATOR
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }

    // 从 Parcel 反序列化的构造函数
    private constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    // 实现 Parcelable 接口方法
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(userId)
        dest.writeString(userPwd)
    }
}
