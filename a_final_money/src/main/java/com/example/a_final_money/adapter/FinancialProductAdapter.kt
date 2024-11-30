package com.example.a_final_money.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.a_final_money.R
import com.example.a_final_money.model.FinancialProduct

class FinancialProductAdapter(
    private val context: Context,
    private val products: List<FinancialProduct>
) : BaseAdapter() {

    override fun getCount(): Int = products.size

    override fun getItem(position: Int): FinancialProduct = products[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_financial_product, parent, false)

        val product = getItem(position)

        // 绑定视图到数据
        view.findViewById<TextView>(R.id.tvProductName).text = product.productName
        view.findViewById<TextView>(R.id.tvInvestmentAmount).text =
            "投资金额：${product.investmentAmount}"
        view.findViewById<TextView>(R.id.tvExpectedProfit).text =
            "预计收益：${product.calculateExpectedProfit()}"

        return view
    }
}
