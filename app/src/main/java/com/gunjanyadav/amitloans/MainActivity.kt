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
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.core.view.isVisible
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
        var noticeCard:CardView = findViewById(R.id.mainActivityNoticeCard)
        val msgTextView = findViewById<TextView>(R.id.mainActivityNoticeCardMsgTextView)
        val user:FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if(user != null){
            FirebaseFirestore.getInstance().collection("notice").document(user.uid).get().addOnSuccessListener {
                if(it.exists()){
                    msgTextView.text = it.data!!.get("message").toString()

                }else{
                    msgTextView.text = "Message will appear here in case of problem with payment."

                }
            }

        }else {
            noticeCard.isVisible = false
        }


        val loanslist:ArrayList<Loan> = ArrayList()

        FirebaseFirestore.getInstance()
                .collection("${if(FirebaseAuth.getInstance().currentUser == null)"/offered_loans" else "users/${FirebaseAuth.getInstance().currentUser!!.uid}/offered_loans"}")
                .get().addOnCompleteListener {myOfferedLoans->
            FirebaseFirestore.getInstance().collection("offered_loans").get().addOnCompleteListener {mainOfferedLoansList->
                var map:Map<String,Objects>
                for((my,main) in (myOfferedLoans.result!!).zip(mainOfferedLoansList.result!!)){
                    //Log.d("debug:","Individual id = ${my.data!!.get("id").toString()} and Main id = ${main.data!!.get("id").toString()}")
                    if(my.data!!.get("id").toString() == main.data!!.get("id").toString()){

                        val loanItem: Loan = Loan(
                                main.data!!.get("amount").toString().toInt(),
                                main.data!!.get("interest_rate").toString().toFloat(),
                                my.data!!.get("is_unlocked").toString().toBoolean(),
                                main.data!!.get("return_in").toString().toInt()
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
        val loanStatus = menu!!.findItem(R.id.loanStatusMenuItem)
        val help = menu!!.findItem(R.id.helpMenuItem)

            try{
                var admin:Boolean = false
                    FirebaseFirestore.getInstance().collection("/admin").document(user!!.uid /*"ultimate_admin_uid"*/).get().addOnCompleteListener {
                        admin = it.result!!["isAdmin"].toString().toBoolean()
                        //Log.d("debug:","Admin is $admin and uid is ${user!!.uid}")
                        adminPanelBtn.isVisible = admin

                    }



            }catch (e:Exception){
            isAdmin = false
            }
        if (user != null) {

            FirebaseFirestore.getInstance().collection("loan_request").document(user.uid).get().addOnCompleteListener {

                try{
                    if (it.result!!.exists() && it.result!!.data!!.get("status").toString() == "SENT") {
                        returnLoanBtn.setVisible(true)
                        loanStatus.setVisible((true))
                        Log.d("debug:", "Loan status is sent")
                    } else if (it.result!!.exists() && it.result!!.data!!.get("status").toString() == "DO_CONFIRM_RETURN") {
                        returnLoanBtn.isEnabled = false
                        loanStatus.isEnabled = false
                        Log.d("debug:", "loan status is do confirm return")
                    } else {
                        returnLoanBtn.isEnabled = false
                        loanStatus.isEnabled = false
                    }
                }catch(e:Exception) {
                    returnLoanBtn.isEnabled = false
                    loanStatus.isEnabled = false
                }

            }.addOnFailureListener{
                returnLoanBtn.isEnabled = false
                loanStatus.isEnabled = false
            }



            signOutBtn.setVisible(true)
            Log.d("debug:", "user not null and isAdmin is $isAdmin")


        } else {
            returnLoanBtn.setVisible(false)
            signOutBtn.setVisible(false)
            adminPanelBtn.isVisible = false
            loanStatus.setVisible(false)

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
            R.id.loanStatusMenuItem ->{
                val i = Intent(this,LoanStatusActivity::class.java)
                startActivity(i)
            }
            R.id.helpMenuItem ->{
                val i = Intent(this,HelpActivity::class.java)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}