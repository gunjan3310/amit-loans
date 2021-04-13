package com.gunjanyadav.amitloans

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class NewRegistrationAction : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_new_registration)

        var list:ArrayList<RegistrationObject> = ArrayList()
        val recyclerView = findViewById<RecyclerView>(R.id.newRegistrationActionList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        FirebaseFirestore.getInstance().collection("new_registration").get().addOnCompleteListener {
            for(request in it.result!!){
                val item = request.data as Map<String,Objects>
                val r = RegistrationObject(uid = item.get("uid").toString(),
                                            name = item.get("fullname").toString(),
                                            email = item.get("email").toString(),
                                            address = item.get("address").toString())
                list.add(r)
            }
            val recyclerViewApapter = NewRegistrationActionListAdapter(this,list)
            recyclerView.adapter = recyclerViewApapter
        }



    }
}