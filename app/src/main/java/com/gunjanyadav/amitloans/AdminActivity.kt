package com.gunjanyadav.amitloans

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.gunjanyadav.amitloans.confirmloanreturn.ConfirmLoanReturn
import com.gunjanyadav.amitloans.dispatch.DispatchListActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user:FirebaseUser? = FirebaseAuth.getInstance().currentUser
        FirebaseFirestore.getInstance().collection("admin").document(user!!.uid).get().addOnCompleteListener {


            val admin :String = it.result!!.get("isAdmin").toString()
            //Log.d("debug:","Admin authentication check is $admin")
            if( admin == "true" ){
                setContentView(R.layout.activity_admin)

            val newRegistrationCard = findViewById<CardView>(R.id.newRegistrationCard)
            newRegistrationCard.setOnClickListener {
                startActivity(Intent(this,NewRegistrationAction::class.java))
            }

            val loanRequestAdminCard = findViewById<CardView>(R.id.loanRequestAdminCard)
            loanRequestAdminCard.setOnClickListener {
                startActivity(Intent(this,ListLoanRequestsActivity::class.java))
            }

            val dispatchListCard = findViewById<CardView>(R.id.dispatchCard)
            dispatchListCard.setOnClickListener {
                startActivity(Intent(this,DispatchListActivity::class.java))
            }

            val confirmLoanReturnCard = findViewById<CardView>(R.id.confirmLoanReturnCard)
            confirmLoanReturnCard.setOnClickListener {
                startActivity(Intent(this, ConfirmLoanReturn::class.java))
            }

            }
            else{
            finish()
            Toast.makeText(applicationContext,"Authentication Failed",Toast.LENGTH_LONG).show()
        }
        }


    }
}