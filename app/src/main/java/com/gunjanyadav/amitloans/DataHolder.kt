package com.gunjanyadav.amitloans

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore

class UserData() {
    lateinit var uid:String
    lateinit var fullname:String
    lateinit var email:String
    lateinit var number:String
    lateinit var profilePictureUri: Uri
    lateinit var ctznBUri:Uri
    lateinit var ctznFUri: Uri
    lateinit var stdIdFUri:Uri
    lateinit var stdIdBUri:Uri
    lateinit var DOB:String


}

data class LoanRequestData(var uid: String, var ctznBUri: Uri?,var ctznFUri: Uri?,var stdIdFUri: Uri?,var stdIdBUri: Uri?, var loanedOn:String, var returnedOn:String, var approvedBy:String, var dispachedBy:String, var returnRecievedBy:String){

}