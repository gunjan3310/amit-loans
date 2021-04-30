package com.gunjanyadav.amitloans.confirmloanreturn

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.gunjanyadav.amitloans.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ConfirmLoanReturnListAdapter(val context: Context, list:ArrayList<HashMap<String,String>>):RecyclerView.Adapter<ConfirmLoanReturnListAdapter.ViewHolder>() {
    val listItems = list
    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val info:TextView = view.findViewById(R.id.confirmLoanReturnItemInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.confirm_loan_return_list_item,parent,false)
        return ConfirmLoanReturnListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        FirebaseFirestore.getInstance().collection("users").document(listItems.get(position).get("uid").toString()).get().addOnSuccessListener {
            val date = SimpleDateFormat("dd-MM-YYYY G").format(Date())
            val loanTaker = it.data!!.get("name").toString()
            holder.info.text = "Loan Taker: ${loanTaker}\n"+
                    "Loan Phone: ${listItems.get(position).get("phone").toString()}\n"+
                    "Returnded Payed on Date: ${if( listItems.get(position).get("returned_on").toString().equals(date) ) "!! Today !!" else listItems.get(position).get("payed_on").toString()}\n"+
                    "Loan Amount: ${listItems.get(position).get("amount").toString()}"
            holder.itemView.setOnClickListener {
                val i = Intent(context,ConfirmReturnLoanViewActivity::class.java)
                i.putExtra("uid","${listItems.get(position).get("uid").toString()}")
                //Log.d("debug:","UID is ${listItems.get(position).get("uid").toString()}")
                i.putExtra("loan_taker",loanTaker)
                context.startActivity(i)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }
}