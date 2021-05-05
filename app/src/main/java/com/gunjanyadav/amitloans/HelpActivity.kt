package com.gunjanyadav.amitloans

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val info :TextView = findViewById(R.id.helpInfo)
        val newVersion:TextView = findViewById(R.id.helpNewVersion)

        FirebaseFirestore.getInstance().collection("help").document("info").get().addOnSuccessListener {
            var infoText = ""
            for (data in it.data!!){
                Log.d("debug:","The data is ${data.value}")
                infoText  = infoText + data.value.toString() + "\n"
            }
            info.text = infoText
        }
        FirebaseFirestore.getInstance().collection("help").document("links").get().addOnSuccessListener {
            for(data in it.data!!){

            }
        }
        newVersion.movementMethod = LinkMovementMethod.getInstance()

    }
}