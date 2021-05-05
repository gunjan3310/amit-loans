package com.gunjanyadav.amitloans

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.media.Image
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gunjanyadav.amitloans.returnLoan.dateRequested
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RequestLoanActivity : AppCompatActivity() {
    val CITIZEN_FRONT = 0
    val CITIZEN_BACK = 1
    val STD_ID_F = 2
    val STD_ID_B = 3

    val keys = arrayOf("ctznF","ctznB","stdIdF","stdIdB")
    val titles = arrayOf("Citizenship Front","Citizenship Back","Student ID Front","Student ID Back")
    lateinit var requestedDate:String
    lateinit var imagesUri:HashMap<String,Uri>
    lateinit var imgCtznF:ImageView
    lateinit var imgCtznB:ImageView
    lateinit var imgStdIdF:ImageView
    lateinit var imgStdIdB:ImageView

    lateinit var ctznNo :TextInputLayout
    lateinit var bankName:TextInputLayout
    lateinit var bankBranch:TextInputLayout
    lateinit var acNum:TextInputLayout
    lateinit var reAcNum:TextInputLayout
    lateinit var phone:TextInputLayout

    fun validateFields():Boolean{
        return ctznNo.editText!!.text.isNotEmpty() &&
                bankName.editText!!.text.isNotEmpty()&&
                bankBranch.editText!!.text.isNotEmpty()&&
                acNum.editText!!.text.isNotEmpty()&&
                reAcNum.editText!!.text.isNotEmpty()&&
                phone.editText!!.text.isNotEmpty()
    }

    private fun writeToDatabase() {

        //add date parameter in loan request database and all other places
        FirebaseFirestore.getInstance().collection("offered_loans").document(intent.extras!!.getString("amount").toString()).get().addOnSuccessListener {offeredLoan->
            val data = HashMap<String,String>()
            data.put("uid",uid)
            data.put("amount",intent.extras!!.getString("amount").toString())
            data.put("ctznNo",ctznNo.editText!!.text.toString())
            data.put("bankName",bankName.editText!!.text.toString())
            data.put("bankBranch",bankBranch.editText!!.text.toString())
            data.put("acNum",acNum.editText!!.text.toString())
            data.put("reAcNum",reAcNum.editText!!.text.toString())
            data.put("phone",phone.editText!!.text.toString())
            data.put("interest_rate",offeredLoan.data!!.get("interest_rate").toString())
            data.put("return_in",offeredLoan.data!!.get("amount").toString())
            data.put("status","unapproved")
            data.put("requested_on",requestedDate)
            data.put("approved_by","")
            data.put("dispatched_by","")
            data.put("dispatched_on","Not Dispatched Yet")
            data.put("returned_recieved_by","")
            data.put(keys[0],"loan_request/$uid/$requestedDate/${keys[0]}.jpg")
            data.put(keys[1],"loan_request/$uid/$requestedDate/${keys[1]}.jpg")
            data.put(keys[2],"loan_request/$uid/$requestedDate/${keys[2]}.jpg")
            data.put(keys[3],"loan_request/$uid/$requestedDate/${keys[3]}.jpg")
            FirebaseFirestore.getInstance().collection("loan_request").document("$uid").set(data).addOnSuccessListener {
                Toast.makeText(this,"Loan requested successfully.",Toast.LENGTH_SHORT).show()
            }
        }
    }
    lateinit var uid:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_loan)
        val dateFormat = SimpleDateFormat("dd-MM-YYYY")
        requestedDate = dateFormat.format(Date())

        findViewById<TextView>(R.id.loanTitle).setText("Rs. "+intent.extras!!.getString("amount"))

        ctznNo = findViewById(R.id.loanRequestCtznNo)
        bankName = findViewById(R.id.loanRequestBankName)
        bankBranch = findViewById(R.id.loanRequestBankBranch)
        acNum = findViewById(R.id.loanRequestAc)
        reAcNum = findViewById(R.id.loanRequestReAc)
        phone = findViewById(R.id.loanRequestNumber)
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        imagesUri = HashMap()

        imgCtznF = findViewById(R.id.loanReqCtznFront)
        imgCtznB = findViewById(R.id.loanReqCtznBack)
        imgStdIdF = findViewById(R.id.loanReqStudentIdFront)
        imgStdIdB = findViewById(R.id.loanReqStudentIdBack)

        val ctznF = findViewById<Button>(R.id.btnCtznFront)
        val ctznB = findViewById<Button>(R.id.btnCtznBack)
        val stdIdF = findViewById<Button>(R.id.btnStudentIdFront)
        val stdIdB = findViewById<Button>(R.id.btnStudentIdBack)
        val requestLoan = findViewById<Button>(R.id.btnRequestLoan)
        val cancel = findViewById<Button>(R.id.btnCancelLoanRequest)

        ctznF.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent,CITIZEN_FRONT)
        }

        ctznB.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent,CITIZEN_BACK)
        }

        stdIdF.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent,STD_ID_F)
        }

        stdIdB.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent,STD_ID_B)
        }

        requestLoan.setOnClickListener {
            requestLoan.isEnabled = false
            FirebaseFirestore.getInstance().collection("loan_request").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {loan->
                if(loan.exists()) {
                    Toast.makeText(this, "You already applied for the loan.", Toast.LENGTH_SHORT).show()
                }else{

                    if(validateFields()){
                        writeToDatabase()
                        uploadPictureToFirebase(0)

                    }
                    else{
                        requestLoan.isEnabled = true
                        Toast.makeText(this,"Fill all the Fields",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        cancel.setOnClickListener{
            val alertBox = AlertDialog.Builder(this)
            alertBox.setTitle("CONFIRMATION")
            alertBox.setMessage("What do you want to do?")
            alertBox.setPositiveButton("Continue Loan"){positive, which->

            }
            alertBox.setNegativeButton("Cancel Loan"){negative, which->

               finish()

            }
            alertBox.setCancelable(false)
            alertBox.create().show()

        }



    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            CITIZEN_FRONT ->{
                val image = imageForView(data!!.data)
                imgCtznF.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                imgCtznF.setTag("image")
                imagesUri.put(keys[0],data!!.data!!)
            }
            CITIZEN_BACK ->{
                val image = imageForView(data!!.data)
                imgCtznB.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                imgCtznB.setTag("image")
                imagesUri.put(keys[1],data!!.data!!)

            }
            STD_ID_F->{
                val image = imageForView(data!!.data)
                imgStdIdF.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                imgStdIdF.setTag("image")
                imagesUri.put(keys[2],data!!.data!!)
            }
            STD_ID_B ->{
                val image = imageForView(data!!.data)
                imgStdIdB.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                imgStdIdB.setTag("image")
                imagesUri.put(keys[3],data!!.data!!)

            }
        }


    }

    private fun imageForView(imageUri:Uri?): Bitmap?{

        val imageStream: InputStream? = contentResolver.openInputStream(imageUri!!)
        val imageBitmap: Bitmap = BitmapFactory.decodeStream(imageStream)

        return imageBitmap
    }

    fun uploadPictureToFirebase(n:Int){

        val pd = ProgressDialog(this)
        pd.setTitle("Uploading Documents")
        pd.setMessage("Please hold on")
        pd.setCanceledOnTouchOutside(false)
        FirebaseStorage.getInstance().reference.child("loan_request/$uid/$requestedDate/${keys[n]}.jpg").putFile(imagesUri.get(keys[n])!!).addOnProgressListener {
            pd.show()
            pd.setTitle(titles[n])
            pd.setMessage("${ it.bytesTransferred * 100 / it.totalByteCount }% of 100%")
        }.addOnSuccessListener {
            pd.dismiss()
            if(n<=2){
                uploadPictureToFirebase(n+1)
            }else{
                setContentView(R.layout.request_loan_success)
                findViewById<Button>(R.id.btnLoanrequestfinishedGoBack).setOnClickListener {
                    finish()
                }
            }
        }

    }


}