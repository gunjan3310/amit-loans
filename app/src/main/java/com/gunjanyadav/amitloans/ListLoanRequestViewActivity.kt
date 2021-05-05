package com.gunjanyadav.amitloans

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gunjanyadav.amitloans.returnLoan.uid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

lateinit var info:TextView
lateinit var userSpecificCitizenFront:ImageView
lateinit var userSpecificCitizenBack:ImageView
lateinit var loanSpecificCitizenFront:ImageView
lateinit var loanSpecificCitizenBack:ImageView
lateinit var loanSpecificStdentIDFront:ImageView
lateinit var loanSpecificStudentBack:ImageView

lateinit var confirmBtn:Button
lateinit var denyBtn:Button

var imagesForDeletion:ArrayList<Uri> = ArrayList()
var documentsURLs:HashMap<String,String> = HashMap() // url of images
var images:HashMap<String,Bitmap> = HashMap()
lateinit var uid:String
val keys = arrayOf("Citizen Front","Citizen Back","Citizen Front in Loan","Citizen Back in Loan","Student Id Front in Loan","Student Id Back in Loan")

class ListLoanRequestViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_loan_request_view)

        uid = FirebaseAuth.getInstance().currentUser!!.uid

        info = findViewById(R.id.listLoanRequestInfo)
        userSpecificCitizenFront = findViewById(R.id.listLoanRequestUserSpecificCitizenFrontImg)
        userSpecificCitizenBack = findViewById(R.id.listLoanRequestUserSpecificCitizenBackImg)

        loanSpecificCitizenFront = findViewById(R.id.listLoanRequestLoanSpecificCitizenFrontImg)
        loanSpecificCitizenBack = findViewById(R.id.listLoanRequestLoanSpecificCitizenBackImg)

        loanSpecificStdentIDFront = findViewById(R.id.listLoanRequestLoanSpecificStudentIDFrontImg)
        loanSpecificStudentBack = findViewById(R.id.listLoanRequestLoanSpecificStudentIDBackImg)

        confirmBtn = findViewById(R.id.listLoanRequestConfirmButton)
        denyBtn = findViewById(R.id.listLoanRequestDenyButton)

        GlobalScope.launch(Dispatchers.IO) {

            FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.getString("uid").toString()).get().addOnSuccessListener {loan->
                FirebaseFirestore.getInstance().collection("users").document(intent.extras!!.getString("uid").toString()).get().addOnSuccessListener {user->


                           documentsURLs.put("Citizen Front",user.data!!.get("ctznF").toString())
                           documentsURLs.put("Citizen Back",user.data!!.get("ctznB").toString())
                           documentsURLs.put("Citizen Front in Loan",loan.data!!.get("ctznF").toString())
                           documentsURLs.put("Citizen Back in Loan",loan.data!!.get("ctznB").toString())
                           documentsURLs.put("Student Id Front in Loan",loan.data!!.get("stdIdF").toString())
                           documentsURLs.put("Student Id Back in Loan",loan.data!!.get("stdIdB").toString())


                    downloadDocuments(0, userSpecificCitizenFront)
                    downloadDocuments(1, userSpecificCitizenBack)
                    downloadDocuments(2, loanSpecificCitizenFront)
                    downloadDocuments(3, loanSpecificCitizenBack)
                    downloadDocuments(4, loanSpecificStdentIDFront)
                    downloadDocuments(5, loanSpecificStudentBack)

                    userSpecificCitizenFront.setOnClickListener{
                        showPicture(0)
                    }
                    userSpecificCitizenBack.setOnClickListener{
                        showPicture(1)
                    }
                    loanSpecificCitizenFront.setOnClickListener{
                        showPicture(2)
                    }
                    loanSpecificCitizenBack.setOnClickListener{
                        showPicture(3)
                    }
                    loanSpecificStdentIDFront.setOnClickListener{
                        showPicture(4)
                    }
                    loanSpecificStudentBack.setOnClickListener{
                        showPicture(5)
                    }



                }
            }




        }
        confirmBtn.setOnClickListener {
            confirmBtn.isEnabled = false
                            val alertBox = AlertDialog.Builder(this)
                alertBox.setTitle("CONFIRMATION")
                alertBox.setMessage("What do you want to do?")
                alertBox.setPositiveButton("Approve"){positive, which->
                    FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.getString("uid").toString())
                            .update(mapOf("approved_by" to uid, "status" to "approved")).addOnSuccessListener {
                                //Log.d("debug:","approced by $uid UID")
                                Toast.makeText(this,"Loan Approved!!", Toast.LENGTH_SHORT).show()
                            }


                }
                alertBox.setNegativeButton("Show Ok"){negative, which->
                    Toast.makeText(this,"Loan Denied!!", Toast.LENGTH_SHORT).show()
                    //FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.getString("uid").toString()).update(mapOf("denied_by" to "$uid", "status" to "denied"))


                }
                alertBox.setCancelable(false)
                alertBox.create().show()

        }
        denyBtn.setOnClickListener {
            denyBtn.isEnabled = false
            //delete loan_request data entry
            //delete storage data for this loan
            //send notice that loan for the amount is deleted
            FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.getString("uid").toString()).get().addOnSuccessListener {loan->
                Log.d("debug:","/loan_request/${intent.extras!!.get("uid").toString()}/${loan.data!!.get("requested_on").toString()}")
                    deleteDocumentsFromStorage(loan.data!! as HashMap<String,String>)
                    FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.get("uid").toString()).delete().addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("notice").document(intent.extras!!.get("uid").toString()).set(
                                mapOf("message" to "Your loan request is denied.","status" to "negative")
                        ).addOnSuccessListener {
                            Toast.makeText(this,"Successfully deleted the loan request",Toast.LENGTH_SHORT).show()

                        }
                    }

            }
        }

    }
    private fun downloadDocuments(index:Int,imageView:ImageView){

            val pd:ProgressDialog = ProgressDialog(this)
            pd.setTitle("Downloading ${keys[index]}")
            pd.setCanceledOnTouchOutside(false)
            pd.show()

            val ref = FirebaseStorage.getInstance().reference.child(documentsURLs.get(keys[index])!!)
            val FIVE_MB:Long = 5*1024*1024

            ref.getBytes(FIVE_MB).addOnSuccessListener {
                images.put(keys[index] ,BitmapFactory.decodeByteArray(it,0,it.size))
                imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(images.get(keys[index]),800,600))
                pd.dismiss()
            }



    }

    private fun showPicture(index: Int){

        val intent = Intent()

        intent.setType("image/*")
        intent.action = Intent.ACTION_VIEW

        val uriPath = MediaStore.Images.Media.insertImage(
            contentResolver,
            images.get(keys[index]),
            "${uid}_${keys[index]}",
            null
        )
        intent.data = Uri.parse(uriPath)

        imagesForDeletion.add(Uri.parse(uriPath))
        startActivity(intent)

    }

    override fun onDestroy() {
        super.onDestroy()
        for(image in imagesForDeletion){
            contentResolver.delete(image, null,null)
        }
    }

    private fun deleteDocumentsFromStorage(data:HashMap<String,String>){
        val keysForFilesToDelete = arrayOf("ctznF","ctznB","stdIdF","stdIdB")
        keysForFilesToDelete.forEach {
            FirebaseStorage.getInstance().reference.child("/loan_request/${uid}/${data.get("requested_on").toString()}/${it}")
        }
    }
}
