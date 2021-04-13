package com.gunjanyadav.amitloans

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class UserLogin : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.user_login)


        actionBar?.setTitle("Registration")
        val email:EditText = findViewById(R.id.loginEmail)
        val password:EditText = findViewById(R.id.loginPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString(),password.text.toString()).addOnSuccessListener {
                val intent = Intent(this,MainActivity::class.java)
                startActivityIfNeeded(intent,0)
                finish()
            }.addOnFailureListener {
                Log.d("debug: ","Login failed with ${it.message}")
            }
        }


        val btnRegister: Button = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener{
            val intent = Intent(this,UserRegister::class.java)
            startActivity(intent)
        }

    }

}