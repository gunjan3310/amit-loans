package com.gunjanyadav.amitloans

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class ListLoanRequestsActivity : AppCompatActivity() {
    lateinit var user:FirebaseUser;
    lateinit var recyclerView:RecyclerView
    lateinit var listAdapter:ListLoanRequestListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_loan_requests)
        var list:ArrayList<LoanRequestObject> = ArrayList()

        recyclerView = findViewById(R.id.loanRequestList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        FirebaseFirestore.getInstance().collection("loan_request").get().addOnCompleteListener {
            for(document in it.result!!){
                if(document.data.get("denied_by").toString() == "null"){
                    val item = LoanRequestObject(
                        document.id,
                        document.data.get("bankName").toString(),
                        document.data.get("phone").toString(),
                        document.data.get("bankBranch").toString()
                    )
                    list.add(item)
                }
            }

            val listAdapter = ListLoanRequestListAdapter(this,list)
            recyclerView.adapter = listAdapter

        }




    }
}