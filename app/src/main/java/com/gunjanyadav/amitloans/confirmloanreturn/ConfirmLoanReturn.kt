package com.gunjanyadav.amitloans.confirmloanreturn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.gunjanyadav.amitloans.NewRegistrationActionListAdapter
import com.gunjanyadav.amitloans.R
lateinit var recyclerView:RecyclerView
class ConfirmLoanReturn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_loan_return)
        recyclerView = findViewById(R.id.confirmLoanReturnList)
        FirebaseFirestore.getInstance().collection("confirm_return_recieve").get().addOnSuccessListener {
            val list:ArrayList<HashMap<String,String>> = ArrayList()
            for (document in it.documents){
                val data = document.data!! as HashMap<String, String>
                list.add(data)

            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = ConfirmLoanReturnListAdapter(this,list)
            recyclerView.adapter = adapter
        }
    }
}