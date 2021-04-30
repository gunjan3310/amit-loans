package com.gunjanyadav.amitloans.confirmloanreturn

import android.app.AlertDialog
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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gunjanyadav.amitloans.Loan
import com.gunjanyadav.amitloans.R
import com.gunjanyadav.amitloans.returnLoan.loanAmount
import com.gunjanyadav.amitloans.returnLoan.uid
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException

class ConfirmReturnLoanViewActivity : AppCompatActivity() {
    val imageUriForDeletion = ArrayList<Uri>()
    lateinit var requestedOn:String
    lateinit var proofImage:Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_loan_return_view)
        val proofImageView:ImageView
        proofImageView = findViewById(R.id.confirmLoanReturnProofImage)
        val confirm: Button = findViewById(R.id.confirmLoanReturnIndividualConfirmBtn)
        val review:Button = findViewById(R.id.confirmLoanReturnIndividualReviewBtn)
        val info:TextView = findViewById(R.id.confirmLoanReturnIndividualInfo)



        FirebaseFirestore.getInstance().collection("confirm_return_recieve").document(intent.extras!!.getString("uid").toString()).get().addOnSuccessListener {
            Log.d("debug:","${it.data as HashMap<String,String>}")
            requestedOn = it.data!!.get("requested_on").toString()
            val data = it.data!! as HashMap<String, String>
            //format info text
            val information = "Name: ${intent.extras!!.getString("loan_taker")}\n"+
                    "Phone: ${it.data!!.get("phone").toString()}\n"+
                    "Requested On: ${it.data!!.get("requested_on").toString()}\n"+
                    "Loan Returned on: ${it.data!!.get("payed_on").toString()}\n"+
                    "Amount: ${it.data!!.get("amount").toString()}"

            info.text = information
            val FIVE_MB: Long = 1024 * 1024 * 5
            //download return loan proof image
            val pd:ProgressDialog = ProgressDialog(this)
            pd.setTitle("Downloading Proof")
            pd.show()
            FirebaseStorage.getInstance().reference.child(it.data!!.get("return_proof_path").toString()).getBytes(FIVE_MB).addOnSuccessListener {
                proofImage = BitmapFactory.decodeByteArray(it,0,it.size)
                proofImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(proofImage,700,1000))
                proofImageView.setOnClickListener{
                    showPicture(proofImage)
                }
                pd.dismiss()

            }
            //alert box for final confirmation
            confirm.setOnClickListener {
                val alertBox = AlertDialog.Builder(this)
                alertBox.setTitle("CONFIRM LOAN RECEIVED")
                alertBox.setMessage("Proof of payment is correct?")
                alertBox.setPositiveButton("Confirmed"){positive, which->
                    data.put("status","TRANSACTION_COMPLETE")
                    FirebaseFirestore.getInstance().collection("users/${intent.extras!!.getString("uid").toString()}/history/").document(requestedOn).set(data).addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("confirm_return_recieve").document(intent.extras!!.getString("uid").toString()).delete().addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.getString("uid").toString()).delete()
                        FirebaseFirestore.getInstance().collection("offered_loans").get().addOnSuccessListener {loanMainList ->
                            FirebaseFirestore.getInstance().collection("users/${intent.extras!!.getString("uid").toString()}/offered_loans").get().addOnSuccessListener {myOfferedLoans->
                               if((myOfferedLoans.size() + 1 ) <= loanMainList.size()){
                                   val newLoanUpgrade = loanMainList.documents.get(myOfferedLoans.size() ).data!! as HashMap<String,String>
                                   newLoanUpgrade.put("is_unlocked","true")
                                   FirebaseFirestore.getInstance().collection("users/${intent.extras!!.getString("uid").toString()}/offered_loans")
                                           .document(loanMainList.documents.get(myOfferedLoans.size() ).data!!.get("amount").toString())
                                           .set(newLoanUpgrade)
                               }else{
                                   Toast.makeText(this,"Already at higest loan",Toast.LENGTH_SHORT).show()
                               }
                            }

                        }
                        }
                    }


                }
                alertBox.setNegativeButton("Problem"){negative, which->
                    //Toast.makeText(this,"Loan Denied!!", Toast.LENGTH_SHORT).show()
                    //message the problem with payment


                }
                alertBox.setCancelable(false)
                alertBox.create().show()
            }


        }.addOnFailureListener {
            Log.d("debug:","Failed to retrieve data")
        }

    }



    private fun showPicture(image:Bitmap){
        val i = Intent()

        i.setType("image/*")
        i.action = Intent.ACTION_VIEW
        Log.d("debug:debug","uid for image uri is ${intent.extras!!.getString("uid").toString()}")
        val uriPath = MediaStore.Images.Media.insertImage(contentResolver,image,"loan_return_proof_${intent.extras!!.getString("uid").toString()}",null)
        i.data = Uri.parse(uriPath)

        imageUriForDeletion.add(Uri.parse(uriPath))
        startActivity(i)
        Log.d("debug:","file path is '$uriPath'")

    }

    override fun onDestroy() {
        super.onDestroy()
        for(image in imageUriForDeletion){
            contentResolver.delete(image,null,null)
        }
    }
}