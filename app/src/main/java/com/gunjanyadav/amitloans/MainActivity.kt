package com.gunjanyadav.amitloans

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var loansListAdapter: ListAdapter





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loanslist:ArrayList<Loan> = ArrayList()
        FirebaseFirestore.getInstance().collection("offered_loans").get().addOnCompleteListener {
            var map:Map<String,Objects>
            for(document in it.result!!){
                Log.d("Loan Item","${document.id}")
                map = document.data as Map<String, Objects>
                val loanItem: Loan = Loan(document.id.toString().toInt(),map.get("interest_rate").toString().toFloat(),map.get("is_unlocked").toString().toBoolean())

                loanslist.add(loanItem)
            }
            recyclerView = findViewById(R.id.loanslist)
            recyclerView.layoutManager = LinearLayoutManager(this)
            loansListAdapter = ListAdapter(applicationContext,loanslist)
            recyclerView.adapter = loansListAdapter

        }

        /*
        var loanslist  = ArrayList<Loan>()
        loanslist.add(Loan(1000,1.5f,false))
        loanslist.add(Loan(3000,1.5f,true))
        loanslist.add(Loan(5000,1.5f,true))
        loanslist.add(Loan(7000,1.5f,true))
        loanslist.add(Loan(10000,1.5f,true))
        loanslist.add(Loan(1000,1.5f,false))
        loanslist.add(Loan(3000,1.5f,true))
        loanslist.add(Loan(5000,1.5f,true))
        loanslist.add(Loan(7000,1.5f,true))
        loanslist.add(Loan(10000,1.5f,true))

        recyclerView = findViewById(R.id.loanslist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loansListAdapter = ListAdapter(applicationContext,loanslist)
        recyclerView.adapter = loansListAdapter
        */
        actionBar?.setTitle("Amit Loans")






    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.signout -> {
                FirebaseAuth.getInstance().signOut()
                item.setVisible(false)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}