package com.gunjanyadav.amitloans.returnLoan

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gunjanyadav.amitloans.R
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

lateinit var returnLoanTitle:TextView
lateinit var loanDate:TextView
lateinit var paymentMethodRadioGroup: RadioGroup
lateinit var transactionId:EditText
lateinit var depositedBy:EditText
lateinit var proofImage:ImageView
lateinit var choosePictureBtn:Button
lateinit var returnLoanBtn:Button

lateinit var uid:String
lateinit var loanAmount:String
lateinit var dateRequested:String
lateinit var proofImageUri:Uri
private val PICK_PROOF: Int = 100
class ReturnLoanActivity : AppCompatActivity() {

    private var loanAmount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_loan)

        returnLoanTitle = findViewById(R.id.returnLoanTitle)
        loanDate = findViewById(R.id.returnLoanOfDate)
        FirebaseFirestore.getInstance().collection("loan_request").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
            if(it.exists()){

                returnLoanTitle.text = returnLoanTitle.text.toString() + it.data!!.get("amount").toString()
                dateRequested = it.data!!.get("requested_on").toString()
                loanDate.text = dateRequested
                uid = it.data!!.get("uid").toString()
            }else{
                //open history page showing no loans to pay
                finish()
            }
        }
        returnLoanBtn = findViewById(R.id.returnLoanPayButton)
        paymentMethodRadioGroup = findViewById(R.id.returnLoanPaymentMethodRadioGroup)
        transactionId = findViewById(R.id.returnLoanTransactionId)
        depositedBy = findViewById(R.id.returnLoanDepositedBy)
        proofImage = findViewById(R.id.returnLoanProofImage)
        choosePictureBtn = findViewById(R.id.returnedLoanChooseProof)

        choosePictureBtn.setOnClickListener {
            val i = Intent()
            i.setType("image/*")
            i.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(i, PICK_PROOF)
        }

        paymentMethodRadioGroup.setOnCheckedChangeListener { group, selectedOption ->
            Log.d("ReturnLoanActivity:","$selectedOption is selected")
            when(selectedOption){
                R.id.returnLoanByEsewa -> {
                    //esewa
                    transactionId.isEnabled = true
                    transactionId.hint = "E-Sewa transaction ID"
                }

                R.id.returnLoanByBank -> {
                    //bank
                    transactionId.isEnabled = false
                    transactionId.hint = "(--Not Required--)"

                }

                R.id.returnLoanByIME -> {
                    //other IME
                    transactionId.isEnabled = true
                    transactionId.hint = "Global IME Code"

                }
            }
        }

        returnLoanBtn.setOnClickListener {
            val selectedRadioButton = paymentMethodRadioGroup.checkedRadioButtonId
            when(selectedRadioButton){
                R.id.returnLoanByEsewa -> {
                    Log.d("ReturnLoanActivity:", "$selectedRadioButton is selected")
                    if (proofImage.tag.equals("return_proof") && depositedBy.text.toString().isNotEmpty() && transactionId.text.toString().isNotEmpty()) {

                        //get data for loan request document with status == SENT
                        FirebaseFirestore.getInstance().collection("loan_request").whereEqualTo("status", "SENT").get().addOnSuccessListener { loan ->
                            val returnedOn = loan.documents.get(0).data!!.get("requested_on").toString()
                            val data = loan.documents.get(0).data!! as HashMap<String, String>

                            //upload loan return proof
                            val pd: ProgressDialog = ProgressDialog(this)
                            pd.setTitle("Uploading Payment Proof")
                            pd.show()
                            FirebaseStorage.getInstance().reference.child("loan_request/${loan.documents.get(0).data!!.get("uid").toString()}/${returnedOn}/return_proof.jpg").putFile(proofImageUri).addOnProgressListener {
                                pd.setMessage("${(it.bytesTransferred / it.totalByteCount) * 100}% of 100%")
                            }.addOnSuccessListener {
                                pd.dismiss()
                                //set status to DO_CONFIRM_RETURN
                                data.put("return_proof_path", "loan_request/$uid/$returnedOn/return_proof.jpg")
                                data.put("status", "DO_CONFIRM_RETURN")
                                data.put("payed_on", SimpleDateFormat("dd-MM-YYYY G").format(Date()))

                                FirebaseFirestore.getInstance().collection("confirm_return_recieve").document(uid).set(data).addOnSuccessListener {
                                    Toast.makeText(this, "Sent for Confirmation", Toast.LENGTH_SHORT).show()

                                }
                            }
                        }

                    } else {
                        Toast.makeText(this, "Fill the mentioned entries.", Toast.LENGTH_SHORT).show()
                    }
                }

                R.id.returnLoanByBank -> {
                    if(proofImage.tag.equals("return_proof") && depositedBy.text.toString().isNotEmpty() ){

                    }
                }

                R.id.returnLoanByIME -> {
                    if(proofImage.tag.equals("return_proof") && depositedBy.text.toString().isNotEmpty() && transactionId.text.toString().isNotEmpty()){

                    }
                }
            }
        }





    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_PROOF){
                proofImageUri = data!!.data!!
                val imageStream: InputStream? = contentResolver.openInputStream(proofImageUri!!)
                val imageBitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
                proofImage.setImageBitmap(ThumbnailUtils.extractThumbnail(imageBitmap,200,200))
                proofImage.tag = "return_proof"

            }
        }
    }
}