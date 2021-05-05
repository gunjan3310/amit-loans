package com.gunjanyadav.amitloans

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LoanStatusActivity : AppCompatActivity() {
    lateinit var imageBitmap: Bitmap
    val imageUriForDeletion:ArrayList<Uri> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_status)
        val image:ImageView = findViewById(R.id.loanStatusSentProofImage)
        val amountText = findViewById<TextView>(R.id.loanStatusAmount)
        val infoText = findViewById<TextView>(R.id.loanStatusInfo)


        FirebaseFirestore.getInstance().collection("loan_request").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
           val requestedOn = it.data!!.get("requested_on").toString()



            val amount = it.data!!.get("amount").toString().toInt()

            val sentOn = it.data!!.get("dispatched_on").toString()
            val sentDate = SimpleDateFormat("dd-MM-yyyy").parse(sentOn)
            val daysToPayIn = it.data!!.get("return_in").toString().toInt()
            val today = Date()
            val daysLoaned = (today.time - sentDate.time)/(1000*60*60*24)

            val interestRate = it.data!!.get("interest_rate").toString().toFloat()

            var totalToPay:Float = amount + (amount*daysLoaned*interestRate)/(100*daysToPayIn)
            var totalInPayableDays = amount + (amount*interestRate)/100

            amountText.text = amount.toString()
            infoText.text = "Sent On: ${it.data!!.get("dispatched_on")}\n"+ "Interest rate: $interestRate\n"+ "Total to Pay till today will be: $totalToPay\n"+
                    "By the end of your payment period: $totalInPayableDays\n"

            val pd:ProgressDialog = ProgressDialog(this)
            pd.setTitle("Downloading Loan Sent Proof")
            pd.setMessage("wait till we find your proof")
            pd.setCanceledOnTouchOutside(false)
            FirebaseStorage.getInstance().reference.child("loan_request/${FirebaseAuth.getInstance().currentUser!!.uid}/${it.data!!.get("requested_on").toString()}/dispatch_proof.jpg")
                    .getBytes(5*1024*1024 ).addOnSuccessListener {
                        pd.dismiss()
                        imageBitmap = BitmapFactory.decodeByteArray(it,0,it.size)
                        image.setImageBitmap(ThumbnailUtils.extractThumbnail(imageBitmap,700,1000))
                        image.setOnClickListener{
                            showPicture(imageBitmap,requestedOn)
                        }
                    }
        }


    }

    private fun showPicture(image:Bitmap,date_dispatch_proof:String){
        val i = Intent()

        i.setType("image/*")
        i.action = Intent.ACTION_VIEW

        val uriPath = MediaStore.Images.Media.insertImage(contentResolver, image, "${date_dispatch_proof}", null)
        i.data = Uri.parse(uriPath)

        imageUriForDeletion.add(Uri.parse(uriPath))
        startActivity(i)
        Log.d("debug:","file path is '$uriPath'")

    }
}