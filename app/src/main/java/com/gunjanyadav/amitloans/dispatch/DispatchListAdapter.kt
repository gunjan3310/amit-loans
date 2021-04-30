package com.gunjanyadav.amitloans.dispatch

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.gunjanyadav.amitloans.NewRegistrationActionListAdapter
import com.gunjanyadav.amitloans.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DispatchListAdapter(val context: Context,var list: ArrayList<DispatchObject>):RecyclerView.Adapter<DispatchListAdapter.ViewHolder>() {
    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {

        var name:TextView = view.findViewById(R.id.dispatchItemName)
        var acNumber:TextView = view.findViewById(R.id.dispatchItemAcNumber)
        var bankName:TextView = view.findViewById(R.id.dispatchItemBankName)
        var requestedOn : TextView = view.findViewById(R.id.dispatchItemRequestedOn)
        var approvedBy:TextView = view.findViewById(R.id.dispatchItemApprovedBy)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dispatch_list_item,parent,false)
        return DispatchListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("debug:","${list.get(position).uid } ${list.get(position).acNumber}")
        var name:String = ""
        var approvedBy:String = ""
        var status:String = ""

        holder.acNumber.text =  "Approved By: ${list.get(position).acNumber}"
        holder.bankName.text = "Bank Name: ${list.get(position).bankName}"
        holder.requestedOn.text =  "Requested On: ${list.get(position).requestedOn}"

        GlobalScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance().collection("users").document(list.get(position).uid).get().addOnCompleteListener {
                name = it.result!!.data!!.get("name").toString()
                holder.name.text = "Loan Taker: ${name}"
            }
            FirebaseFirestore.getInstance().collection("loan_request").document(list.get(position).uid).get().addOnCompleteListener {loan->
                FirebaseFirestore.getInstance().collection("users").document(loan.result!!.data!!.get("approved_by").toString()).get().addOnCompleteListener {
                    approvedBy = it.result!!.data!!.get("name").toString()
                    list.get(position).approvedBy = approvedBy

                    holder.approvedBy.text = "Approved By: ${approvedBy}"
                }

                holder.itemView.setOnClickListener {



                        val i = Intent(context, DispatchItemViewActivity::class.java)
                        i.putExtra("name",name)
                        i.putExtra("approvedBy",approvedBy)
                        i.putExtra("acNum",list.get(position).acNumber)
                        i.putExtra("bankName",list.get(position).bankName)
                        i.putExtra("requested_on",list.get(position).requestedOn)
                        i.putExtra("status",loan.result!!.getString("status").toString())
                        i.putExtra("uid",list.get(position).uid)
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)

                }

            }

        }


    }

    override fun getItemCount(): Int {
        return list.size
    }
}