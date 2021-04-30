package com.gunjanyadav.amitloans.dispatch

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
import com.gunjanyadav.amitloans.R
import java.io.InputStream

class DispatchItemViewActivity : AppCompatActivity() {
    var imageUriForDeletion: ArrayList<Uri> = ArrayList()
    private val SEND_PROOF: Int = 100
    lateinit var proofImage:Uri
    lateinit var  text:TextView
    lateinit var btnProof:Button
    lateinit var proofImageView:ImageView
    lateinit var done:Button

    lateinit var requesterUID:String
     lateinit var imageUri:Uri

     override fun onCreate(savedInstanceState: Bundle?) {
         val requester  = intent.extras!!
         requesterUID = requester.getString("uid").toString()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispatch_item_view)

        text = findViewById<TextView>(R.id.dispatchItemViewTitle)
        btnProof = findViewById(R.id.dispatchItemViewProofButton)
        proofImageView = findViewById(R.id.dispatchItemViewProofImage)
        done = findViewById(R.id.dispatchItemViewConfirmPaymentButton)

        val name:TextView = findViewById(R.id.dispatchItemViewName)
        val acNum:TextView = findViewById(R.id.dispatchItemViewAcNumber)
        val bankName:TextView = findViewById(R.id.dispatchItemViewBankName)
        val requestedOn:TextView = findViewById(R.id.dispatchItemViewRequestedOn)
        val approvedBy:TextView = findViewById(R.id.dispatchItemViewApprovedBy)

        name.text =name.text.toString() +  requester.getString("name")
        acNum.text =acNum.text.toString() + requester.getString("acNum")
        bankName.text =bankName.text.toString() + requester.getString("bankName")
        requestedOn.text =requestedOn.text.toString() +  requester.getString("requestedOn")
        approvedBy.text = approvedBy.text.toString() + requester.getString("approvedBy")
        val status = intent.extras!!.getString("status").toString()
        if(status.equals("SENT")){
            Log.d("debug:","Dispatch Activity View Status is $status")
            //download proof image
            val proofImageDownloadedUri:Uri
            val FIVE_MB:Long = 1024*1024*5
            val pd:ProgressDialog = ProgressDialog(this)
            pd.setTitle("Downloading Proof")
            pd.setCanceledOnTouchOutside(false)
            pd.show()
            FirebaseStorage.getInstance().reference.child("/loan_request/${requester.getString("uid")}/${requester.getString("requested_on").toString()}/dispatch_proof.jpg").getBytes(FIVE_MB).addOnSuccessListener {
                val image:Bitmap = BitmapFactory.decodeByteArray(it,0,it.size)
                showPicture(image)
                pd.dismiss()
            }
            done.isEnabled = false

            btnProof.isEnabled = false

        }else if(status.equals("returned")){
            done.isEnabled = false
            btnProof.isEnabled = false
            //show payment returned payment proof

        }
        else{
            Log.d("debug:","Dispatch Activity View Status isnt available")
            btnProof.setOnClickListener {
                choosePicture(SEND_PROOF)
            }
            done.setOnClickListener {
                if( proofImageView.tag != null && proofImageView.tag.toString().equals("proof") ){

                    val pd:ProgressDialog = ProgressDialog(this)
                    pd.setTitle("Uploading Payment Proof")
                    pd.show()

                    FirebaseStorage.getInstance().reference.child("/loan_request/${intent.extras!!.getString("uid")}/${requester.getString("requested_on").toString()}/dispatch_proof.jpg").putFile(imageUri).addOnProgressListener {
                        pd.setMessage("${(it.bytesTransferred/it.totalByteCount)*100}% of 100% uploaded")
                    }.addOnSuccessListener {
                        pd.dismiss()
                        FirebaseFirestore.getInstance().collection("loan_request").document(intent.extras!!.getString("uid")!!).update(mapOf<String,String>("status" to "SENT")).addOnSuccessListener {
                            finish()

                        }
                    }
                }else{
                    Toast.makeText(this,"Please Choose the Proof Picture",Toast.LENGTH_SHORT).show()
                }
            }
        }



    }
    private fun choosePicture( requestCode:Int) {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(i,requestCode)
    }


    private fun imageForView(imageUri: Uri?): Bitmap?{

        val imageStream: InputStream? = contentResolver.openInputStream(imageUri!!)
        val imageBitmap: Bitmap = BitmapFactory.decodeStream(imageStream)

        return imageBitmap
    }
    private fun showPicture(image:Bitmap){
        proofImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,400))
        proofImageView.setOnClickListener{
            //view in gallery intent
            val intent = Intent()

            intent.setType("image/*")
            intent.action = Intent.ACTION_VIEW

            val uriPath = MediaStore.Images.Media.insertImage(contentResolver,image,"proof_${requesterUID}",null)
            intent.data = Uri.parse(uriPath)

            imageUriForDeletion.add(Uri.parse(uriPath))
            startActivity(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SEND_PROOF && resultCode == RESULT_OK){
            Log.d("debug:","Payment proof captured")
            imageUri = data!!.data!!
            val image = imageForView(imageUri)
            proofImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,400))
            proofImageView.setTag("proof")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        for(uri in imageUriForDeletion){
            contentResolver.delete(uri,null,null)
        }
    }

}