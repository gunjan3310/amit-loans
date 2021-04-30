package com.gunjanyadav.amitloans.dispatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gunjanyadav.amitloans.R

class DispatchListActivity : AppCompatActivity() {
    lateinit var listLayout:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispatch_list)
         listLayout = findViewById(R.id.dispatchList)
        listLayout.layoutManager = LinearLayoutManager(this)
        val list = ArrayList<DispatchObject>()
        if(FirebaseAuth.getInstance().currentUser!!.uid != null){
            FirebaseFirestore.getInstance().collection("loan_request").whereIn("status", listOf("approved","SENT")).get().addOnCompleteListener {
                for (document in it.result!!){



                           // n = name.result!!.documents.get(0).get("name").toString()
                            //a = approvedBy.result!!.get("name").toString()
                            //Log.d("debug:","${a} $n")
                            val item = DispatchObject(
                                    document.data!!.get("uid").toString(),
                                    "",
                                    document.data!!.get("acNum").toString(),
                                    document.data!!.get("bankName").toString(),
                                    document.data!!.get("requested_on").toString(),
                                    "",

                                    document.data!!.get("status").toString())
                     Log.d("debug:","$item")
                    list.add(item)



                }
                //list.add(DispatchObject("sdfs","gunjan yadav","8787687","laxmi","w34r23","amit"))
                val adapter = DispatchListAdapter(this,list)
                listLayout.adapter = adapter
            }
        }
    }
}