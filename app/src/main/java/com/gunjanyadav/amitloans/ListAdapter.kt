package com.gunjanyadav.amitloans

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListAdapter(val context: Context, list: ArrayList<Loan>): RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    val loans = list
    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
         var loanAmount: TextView
         var interest: TextView
         var isLocked:ImageView
        init {

            loanAmount= view.findViewById(R.id.loanAmount)
            interest= view.findViewById(R.id.interest)
            isLocked = view.findViewById((R.id.isLocked))

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.loan_list_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Loan = loans.get(position)

        holder.loanAmount.text ="Rs. "+ item.loanAmount.toString()+"/-"
        holder.interest.text = item.loanInterest.toString()+" Interest Rate is applicable on this loan and must be returned in ${item.returnIn} days."
        holder.isLocked.setImageResource(if(item.isUnlocked == true)R.drawable.unlocked_loan else R.drawable.locked_loan)
        holder.itemView.setOnClickListener{


            val auth = FirebaseAuth.getInstance().currentUser
            if(auth == null){
                val intent = Intent(context, UserLogin::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(intent)
            }else{
                if(item.isUnlocked == true)
                {
                    FirebaseFirestore.getInstance().collection("users").document(auth.uid).get().addOnSuccessListener {user->
                        if(user.data!!.get("status").toString() == "approved"){
                            FirebaseFirestore.getInstance().collection("loan_request").document(auth.uid).get().addOnSuccessListener {
                                if(!it.exists()){
                                    val i = Intent(context,RequestLoanActivity::class.java)
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    i.putExtra("amount",item.loanAmount.toString())
                                    i.putExtra("interest_rate",item.loanInterest)
                                    context.startActivity(i)
                                }else{
                                    Toast.makeText(context,"You already have a loan.",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }else{
                            Toast.makeText(context,"Your Account is not approved.",Toast.LENGTH_SHORT).show()
                        }
                    }

                } else Toast.makeText(context,"This loan is unlocked",Toast.LENGTH_SHORT).show()
            }


        }
    }

    override fun getItemCount(): Int = loans.size
}