package com.gunjanyadav.amitloans

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream




class UserRegister : AppCompatActivity() {
    private val GET_PP: Int = 100
    private val GET_CTZN_BACK: Int=101
    private val GET_CTZN_FRONT: Int=102
    private val STUDENT_ID_FRONT: Int = 103

    private val STUDENT_ID_BACK: Int = 104
    lateinit var fullName:EditText
    lateinit var email: EditText
    lateinit var password:EditText
    lateinit var repassword:EditText
    lateinit var phoneNumber:EditText
    lateinit var DOB:EditText
    lateinit var ctznPictureFront:ImageView
    lateinit var ctznPictureBack:ImageView
    lateinit var profilePicture:ImageView
    lateinit var studentIdFront:ImageView
    lateinit var studentIdBack:ImageView
    lateinit var termAgreement:CheckBox
    lateinit var register: Button
    var user:UserData = UserData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_register)

        fullName = findViewById(R.id.fullName)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        repassword = findViewById(R.id.repassword)
        phoneNumber = findViewById(R.id.phoneNumber)
        DOB = findViewById(R.id.DOB)
        
        ctznPictureBack = findViewById(R.id.ctznBack)
        ctznPictureFront = findViewById(R.id.ctznFront)
        profilePicture = findViewById(R.id.profilePicture)
        studentIdFront = findViewById(R.id.studentIdFront)
        studentIdBack = findViewById(R.id.studentIdBack)

        termAgreement = findViewById(R.id.termAgreement)

        findViewById<Button>(R.id.btnPP).setOnClickListener{
            choosePicture("Select Profile Picture",GET_PP)
        }
        findViewById<Button>(R.id.btnCtznBack).setOnClickListener{
            choosePicture("Select Citizenship Back",GET_CTZN_BACK)
        }
        findViewById<Button>(R.id.btnCtznFront).setOnClickListener{
            choosePicture("Select Citizenship Front",GET_CTZN_FRONT)
        }
        findViewById<Button>(R.id.btnStudentIdFront).setOnClickListener{
            choosePicture("Select Student ID Front",STUDENT_ID_FRONT)
        }
        findViewById<Button>(R.id.btnStudentIdBack).setOnClickListener{
            choosePicture("Select Student ID Back",STUDENT_ID_BACK)
        }
        register = findViewById(R.id.register)
        register.setOnClickListener{
            val validated: Boolean = validateTextFields(fullName) && validateTextFields(email) && validateTextFields(password) &&
                    validateTextFields(repassword) && validateTextFields(phoneNumber) && validateTextFields(DOB)
                    && validatePicturesSelected(ctznPictureFront) && validatePicturesSelected(ctznPictureBack) && validatePicturesSelected(studentIdFront) && validatePicturesSelected(studentIdBack)
                    && validatePicturesSelected(profilePicture) &&  validateTermsAndConditions(termAgreement)
            if(validated==true)createNewUserAndUploadDocuments()
            setContentView(R.layout.registration_finished)
            val homeButton = findViewById<Button>(R.id.btnHome)
            homeButton.setOnClickListener {
                val intent = Intent(this,MainActivity::class.java)
                startActivityIfNeeded(intent,0)
                finish()
            }


        }





    }

    private fun validateTermsAndConditions(view:CheckBox):Boolean{
        if(!view.isChecked){
            Toast.makeText(this,"Do accept Terms and Conditions", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun validatePicturesSelected(view:ImageView): Boolean{
        if(view.tag != null && view.tag.toString().equals("image") ) return true
        else{
            Toast.makeText(this,"All pictures needs to be Selected", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun validateTextFields(view:EditText): Boolean {
        if(view.text.toString().isNotEmpty())return true
        else{
         view.error = "This has to be filled"
        }
        return false

    }

    private fun choosePicture(s: String, requestCode:Int) {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(i,requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                GET_PP ->{
                    val imageUri: Uri? = data!!.data
//                    val image:String? = imageUri!!.path
//                    Log.d("Image Path",image!!)
                    val image = imageForView(imageUri)
                    profilePicture.setImageBitmap(ThumbnailUtils.extractThumbnail(image,200,200))
                    profilePicture.setTag("image")
                    user.profilePictureUri = imageUri!!

                }
                GET_CTZN_FRONT ->{
                    val image = imageForView(data!!.data)
                    ctznPictureFront.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                    ctznPictureFront.setTag("image")
                    user.ctznFUri = data!!.data!!

                }
                GET_CTZN_BACK ->{
                    val image = imageForView(data!!.data)
                    ctznPictureBack.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                    ctznPictureBack.setTag("image")
                    user.ctznBUri = data!!.data!!
                }
                STUDENT_ID_FRONT->{
                    val image = imageForView(data!!.data)
                    studentIdFront.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                    studentIdFront.setTag("image")
                    user.stdIdFUri = data!!.data!!

                }
                STUDENT_ID_BACK->{
                    val image = imageForView(data!!.data)
                    studentIdBack.setImageBitmap(ThumbnailUtils.extractThumbnail(image,500,300))
                    studentIdBack.setTag("image")
                    user.stdIdBUri = data!!.data!!
                }
            }
        }else{
            Toast.makeText(applicationContext,"Image not Picked",Toast.LENGTH_LONG).show()
        }
    }



    private fun imageForView(imageUri:Uri?):Bitmap?{

        val imageStream:InputStream? = contentResolver.openInputStream(imageUri!!)
        val imageBitmap: Bitmap = BitmapFactory.decodeStream(imageStream)

        return imageBitmap
    }





    private fun uploadUserData(uid: String){
        val pictures = arrayListOf("Profile Picture","Citizenship Front","Citizenship Back","Student Id Front", "Student Id Back")
        val registrationPictureUpload = UserPictureUploader(user.uid.toString())
        registrationPictureUpload.firebaseUploadPicture(this,user.profilePictureUri, "profilePicture",pictures.get(0))
        registrationPictureUpload.firebaseUploadPicture(this, user.ctznFUri, "ctznFront",pictures.get(1))
        registrationPictureUpload.firebaseUploadPicture(this, user.ctznBUri, "ctznBack",pictures.get(2))
        registrationPictureUpload.firebaseUploadPicture(this, user.stdIdFUri, "stdIdF",pictures.get(3))
        registrationPictureUpload.firebaseUploadPicture(this, user.stdIdBUri, "stdIdB",pictures.get(4))



    }

    class UserPictureUploader(val uid:String){
            fun firebaseUploadPicture(context: Context, uri: Uri, filename: String,dialogTitle:String){
                var picture = FirebaseStorage.getInstance().reference.child("new_registration/$uid/${filename}.jpg")
                val pd:ProgressDialog
                pd = ProgressDialog(context)
                pd.setTitle("Uploading ${dialogTitle}")
                pd.setCancelable(false)
                pd.setCanceledOnTouchOutside(false)
                pd.show()

                picture.putFile(uri).addOnProgressListener {task ->
                    pd.setMessage( "${100 * task.bytesTransferred / task.totalByteCount }% of 100% completed")
                }.addOnCompleteListener{
                    pd.dismiss()

                }
            }

    }



    private fun createNewUserAndUploadDocuments(){
        user.number = phoneNumber.text.toString()
        user.email = email.text.toString()
        user.DOB = DOB.text.toString()



        var auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email, "qwdfgsdfgsdfgertyuiop[]")
                .addOnCompleteListener(this){

                    if(!it.isSuccessful)Toast.makeText(this, "Email \"${user.email}\" already used", Toast.LENGTH_LONG).show() else{
                        user.uid = it.result?.user?.uid!!
                        addUserRegistrationToDatabase(user.uid)
                        uploadUserData(user.uid)


                    }
                }


    }

    private fun addUserRegistrationToDatabase(uid: String){
        var registration = FirebaseFirestore.getInstance().collection("new_registration")
        var newRegister = HashMap<String,String>()
        newRegister.put("uid",user.uid)
        newRegister.put("accountStatus","Unapproved")
        newRegister.put("email",user.email)
        newRegister.put("number",user.number)
        newRegister.put("dob",user.DOB)
        newRegister.put("profilePicture","new_registration/$uid/profilePicture")
        newRegister.put("ctznFront","new_registration/$uid/ctznFront")
        newRegister.put("ctznBack","new_registration/$uid/ctznBack")
        newRegister.put("stdIdF","new_registration/$uid/stdIdF")
        newRegister.put("stdIdB","new_registration/$uid/stdIdB")

        registration.document(user.uid).set(newRegister).addOnCompleteListener {
            Toast.makeText(this,"Database added",Toast.LENGTH_SHORT).show()
        }

    }






}