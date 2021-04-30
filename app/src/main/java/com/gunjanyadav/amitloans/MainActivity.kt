package com.gunjanyadav.amitloans

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.gunjanyadav.amitloans.returnLoan.ReturnLoanActivity
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var loansListAdapter: ListAdapter
     var user:FirebaseUser? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val connectivity : ConnectivityManager? = this.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(connectivity != null){
            val info : NetworkInfo? = connectivity!!.activeNetworkInfo
            if(info != null){
                if(info!!.state == NetworkInfo.State.CONNECTED ){
                    Toast.makeText(this,"Connected to Internet!!",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Not connected to Internet!!",Toast.LENGTH_LONG).show()
            }
        }


        val loanslist:ArrayList<Loan> = ArrayList()

        FirebaseFirestore.getInstance()
                .collection("${if(FirebaseAuth.getInstance().currentUser == null)"/offered_loans" else "users/${FirebaseAuth.getInstance().currentUser!!.uid}/offered_loans"}")
                .get().addOnCompleteListener {myOfferedLoans->
            FirebaseFirestore.getInstance().collection("offered_loans").get().addOnCompleteListener {mainOfferedLoansList->
                var map:Map<String,Objects>
                for((my,main) in (myOfferedLoans.result!!).zip(mainOfferedLoansList.result!!)){
                    Log.d("debug:","Individual id = ${my.data!!.get("id").toString()} and Main id = ${main.data!!.get("id").toString()}")
                    if(my.data!!.get("id").toString() == main.data!!.get("id").toString()){

                        val loanItem: Loan = Loan(
                                main.data!!.get("amount").toString().toInt(),
                                main.data!!.get("interest_rate").toString().toFloat(),
                                my.data!!.get("is_unlocked").toString().toBoolean()
                        )

                        loanslist.add(loanItem)
                    }
                    //Log.d("Loan Item","${document.id}")

                }
                recyclerView = findViewById(R.id.loanslist)
                recyclerView.layoutManager = LinearLayoutManager(this)
                loansListAdapter = ListAdapter(applicationContext,loanslist)
                recyclerView.adapter = loansListAdapter
            }

        }

        var status = ""
        GlobalScope.launch(Dispatchers.IO){
            val user:FirebaseUser? = FirebaseAuth.getInstance().currentUser
            if(user != null){
                val uid = user.uid
                var data =HashMap<String,String>()
                FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener {
                   data = it.data as HashMap<String, String>
                   status = data.get("status")!!.toString()
                   //Log.d("debug: ","Account Status is $status")
                    val actionBar = supportActionBar
                    actionBar!!.setTitle("Amit Loans (${status.toUpperCase()})")
                }

            }else{
                val actionBar = supportActionBar
                actionBar!!.setTitle("Amit Loans(Logged out)")
            }
        }






    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main,menu)



        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        var user :FirebaseUser? = FirebaseAuth.getInstance().currentUser


        var isAdmin:Boolean = false
        val signOutBtn = menu!!.findItem(R.id.signout)
        val adminPanelBtn = menu!!.findItem(R.id.adminPanel)
        val returnLoanBtn = menu!!.findItem(R.id.returnLoanMenuItem)

            try{
                var admin:Boolean = false
                    FirebaseFirestore.getInstance().collection("/admin").document(user!!.uid /*"ultimate_admin_uid"*/).get().addOnCompleteListener {
                        admin = it.result!!["isAdmin"].toString().toBoolean()
                        adminPanelBtn.isVisible = admin

                    }



            }catch (e:Exception){
            isAdmin = false
            }
        if (user != null) {
            returnLoanBtn.setVisible(true)
            signOutBtn.setVisible(true)
           // Log.d("debug:", "user not null and isAdmin is $isAdmin")


        } else {
            returnLoanBtn.setVisible(false)
            signOutBtn.setVisible(false)
            adminPanelBtn.isVisible = false
        }



        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.signout -> {

                GlobalScope.launch(Dispatchers.IO){
                    FirebaseAuth.getInstance().signOut()
                    withContext(Dispatchers.Main){
                        item.setVisible(false)
                    }
                }
            finish()
             startActivity(intent)
            }
            R.id.adminPanel ->{
                val intent = Intent(this,AdminActivity::class.java)
                startActivity(intent)
            }
            R.id.returnLoanMenuItem ->{
                val i = Intent(this,ReturnLoanActivity::class.java)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}