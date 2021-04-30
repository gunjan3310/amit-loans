package com.gunjanyadav.amitloans

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListLoanRequestListAdapter(val context: Context,list:ArrayList<LoanRequestObject>): RecyclerView.Adapter<ListLoanRequestListAdapter.ViewHolder>() {
    val list = list
    val uid = FirebaseAuth.getInstance().currentUser!!.uid


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var info:TextView
        init {
            info = view.findViewById(R.id.loanRequestInfo)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListLoanRequestListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.loan_request_item,parent,false)
        return ListLoanRequestListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        FirebaseFirestore.getInstance().collection("users").document(list.get(position).uid).get().addOnCompleteListener {

            holder.info.text = "Name:${it.result!!.get("name").toString()}\n"+
                                "Phone Number: ${it.result!!.get("number").toString()}\n"+
                                "Loan Request number:${list.get(position).requestPhone}\n"+
                                "Bank: ${list.get(position).bankName}\n"+
                                "Branch:${list.get(position).branch}\n"+
                                "Amount: Rs. ${list.get(position).amount}\n" +
                                "Requested oN: ${list.get(position).requestedOn}"
            val loanRequester:String = list.get(position).uid
            holder.itemView.setOnClickListener {

                val alertBox = AlertDialog.Builder(context)
                alertBox.setTitle("CONFIRMATION")
                alertBox.setMessage("What do you want to do?")
                alertBox.setPositiveButton("Approve"){positive, which->
                    Toast.makeText(context,"Loan Approved!!", Toast.LENGTH_SHORT).show()
                    FirebaseFirestore.getInstance().collection("loan_request").document(loanRequester).update(mapOf("approved_by" to uid, "status" to "approved"))
                    

                }
                alertBox.setNegativeButton("Deny"){negative, which->
                    Toast.makeText(context,"Loan Denied!!", Toast.LENGTH_SHORT).show()
                    FirebaseFirestore.getInstance().collection("loan_request").document(loanRequester).update(mapOf("denied_by" to "$uid", "status" to "denied"))


                }
                alertBox.setCancelable(false)
                alertBox.create().show()

            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
}