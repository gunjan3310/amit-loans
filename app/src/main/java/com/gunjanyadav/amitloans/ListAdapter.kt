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

        holder.loanAmount.setText(item.loanAmount.toString())
        holder.interest.setText(item.loanInterest.toString())
        holder.isLocked.setImageResource(if(item.isUnlocked == true)R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_background)
        holder.itemView.setOnClickListener{


            val auth = FirebaseAuth.getInstance().currentUser
            if(auth == null){
                val intent = Intent(context, UserLogin::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(intent)
            }else{
                if(item.isUnlocked == true)
                {
                    val i = Intent(context,RequestLoanActivity::class.java)
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.putExtra("loanItem",holder.loanAmount.text.toString())
                    context.startActivity(i)

                } else Toast.makeText(context,"This loan is unlocked",Toast.LENGTH_SHORT).show()
            }


        }
    }

    override fun getItemCount(): Int = loans.size
}