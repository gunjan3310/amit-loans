package com.gunjanyadav.amitloans

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NewRegistrationActionViewActivity : AppCompatActivity() {
     lateinit var  uid:String
    lateinit var userData: HashMap<String,String>
    var images:HashMap<String,Bitmap> = HashMap()
    lateinit var pd:ProgressDialog
    val keys = arrayOf("profilePicture","ctznFront","ctznBack","stdIdF","stdIdB")
     lateinit var imageUriForDeletion:ArrayList<Uri>
    lateinit var img_temp:ImageView
    lateinit var imagesByteArray: HashMap<String,ByteArray>
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_registration_action_view)
        userData = HashMap()
        imageUriForDeletion = ArrayList()
        imagesByteArray = hashMapOf()
        pd= ProgressDialog(this)
        uid = intent.extras!!.getString("uid")!!


        val name:TextView = findViewById(R.id.nravName)
        val number:TextView = findViewById(R.id.nravNumber)
        val email:TextView = findViewById(R.id.nravEmail)
        val dob:TextView = findViewById(R.id.nravDOB)
        val ctznF: Button = findViewById(R.id.nravBtnCtznFront)
        val ctznB: Button = findViewById(R.id.nravBtnCtznBack)
        val stdIdF: Button = findViewById(R.id.nravBtnStdIdFront)
        val stdIdB: Button = findViewById(R.id.nravBtnStdIdBack)
        val profilePicture:Button = findViewById(R.id.nravBtnProfilePicture)
        val approve:Button = findViewById(R.id.nravBtnApprove)


            FirebaseFirestore.getInstance().collection("new_registration").document(uid.toString()).get().addOnSuccessListener {
                userData = it.data as HashMap<String, String>
                name.text = userData.get("fullname")
                email.text = userData.get("email")
                number.text = userData.get("number")
                dob.text = userData.get("dob")
                downloadDocuments(4)

            }






        img_temp = findViewById(R.id.temp_img)

        profilePicture.setOnClickListener {
            showPicture(0)
        }
        ctznF.setOnClickListener {
            showPicture(1)
        }
        ctznB.setOnClickListener {
            showPicture(2)
        }
        stdIdF.setOnClickListener {
            showPicture(3)
        }
        stdIdB.setOnClickListener {
            showPicture(4)
        }

        approve.setOnClickListener {
            //upload data to user/profile and mark user approved
            uploadPictureToUsersCollection(0)

            val user:HashMap<String,String> = hashMapOf("uid" to uid,
                                                        "name" to name.text.toString(),
                                                        "dob" to dob.text.toString(),
                                                        "email" to email.text.toString(),
                                                        "number" to number.text.toString()
                                                        ,"profilePicture" to "users/$uid/profile/profilePicture.jpg"
                                                        ,"ctznF" to "users/$uid/profile/ctznFront.jpg"
                                                        ,"ctznB" to "users/$uid/profile/ctznBack.jpg"
                                                        ,"stdIdF" to "users/$uid/profile/stdIdF.jpg"
                                                        ,"stdIdB" to "users/$uid/profile/stdIdB.jpg"
                                                        ,"status" to "approved"
            )


            FirebaseFirestore.getInstance().collection("users").document("$uid").set(user).addOnCompleteListener {

                FirebaseFirestore.getInstance().collection("new_registration").document("$uid").delete()

            }


        }





    }

    private fun uploadPictureToUsersCollection(n: Int){
        val progressDialog = ProgressDialog(this)

            val titles = arrayOf("Profile Picture","Citizenship Front","Citizenship Back","Student ID Back","Student ID Front")
            progressDialog.setTitle(titles[n])
            FirebaseStorage.getInstance().reference.child("users/$uid/profile/${keys[n]}.jpg").putBytes(imagesByteArray.get(keys[n])!!).addOnProgressListener {
                progressDialog.setTitle(titles[n])
                progressDialog.setMessage("${it.bytesTransferred *100 /it.totalByteCount}% of 100%")
                progressDialog.show()
            }.addOnSuccessListener {
                progressDialog.dismiss()
                if(n<4){
                    uploadPictureToUsersCollection(n+1)
                }
                else{
                    FirebaseStorage.getInstance().reference.child("new_registration/$uid").delete().addOnSuccessListener {

                        Toast.makeText(this,"All photoes Uploaded and cloud storage cleaned", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    }

    private fun showPicture(index:Int){
        val i = Intent()

        i.setType("image/*")
        i.action = Intent.ACTION_VIEW

         val uriPath = MediaStore.Images.Media.insertImage(contentResolver,images[keys[index]],"${uid}_${keys[index]}",null)
        i.data = Uri.parse(uriPath)

        imageUriForDeletion.add(Uri.parse(uriPath))
        startActivity(i)
        Log.d("debug:","file path is '$uriPath'")

    }



    fun downloadDocuments(n:Int){


            val titles = arrayOf("Profile Picture","Citizenship Front","Citizenship Back","Student ID Back","Student ID Front")

            for(n in 0..keys.size-1){
                //Log.d("debug: ","key is ${keys[n]} and value is ${userData.get(keys[n])}")
                val progressDialog:ProgressDialog = ProgressDialog(this)

                progressDialog.setTitle(titles[n].toString())
                progressDialog.show()
                //Log.d("debug:", "Firebase Storage location ${userData.get(keys.get(n))!!}")
                val ref = FirebaseStorage.getInstance().reference.child("${userData.get(keys[n])}.jpg")
                val FIVE_MB: Long = 1024 * 1024 * 5

                ref.getBytes(FIVE_MB).addOnSuccessListener {
                    Toast.makeText(this, "Image downloaded $n", Toast.LENGTH_SHORT).show()

                    imagesByteArray.put(keys[n],it)

                    images.put(keys[n], BitmapFactory.decodeByteArray(it, 0, it.size))
                    img_temp.setImageBitmap(images.get(keys[n]))
                    progressDialog.dismiss()


                }.addOnFailureListener {
                    Toast.makeText(this, "Error downloading", Toast.LENGTH_LONG).show()
                    Log.d("debug: ", "The Download Error message: ${it.message}")

                }

            }


    }
    override fun onDestroy() {
        super.onDestroy()
        imageUriForDeletion.forEach{
            contentResolver.delete(it,null,null)
        }
    }


}