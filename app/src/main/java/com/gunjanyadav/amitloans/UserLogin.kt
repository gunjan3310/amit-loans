package com.gunjanyadav.amitloans

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        val phone:EditText = findViewById(R.id.phoneNumber)
        val password:EditText = findViewById(R.id.password)
        val btnLogin: Button = findViewById(R.id.btnLogin)


        val btnRegister: Button = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener{
            val intent = Intent(this,UserRegister::class.java)
            startActivity(intent)
        }

    }

}