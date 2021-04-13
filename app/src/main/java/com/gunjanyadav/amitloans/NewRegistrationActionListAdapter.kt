package com.gunjanyadav.amitloans

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.options
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NewRegistrationActionListAdapter(val context: Context, list: ArrayList<RegistrationObject>):RecyclerView.Adapter<NewRegistrationActionListAdapter.ViewHolder>() {
    val list = list

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var info:TextView


        init {
            info = view.findViewById(R.id.registration_candidate_info)


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewRegistrationActionListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.registration_action_list,parent,false)
        return NewRegistrationActionListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(context,NewRegistrationActionViewActivity::class.java)
            intent.putExtra("uid",list.get(position).uid)
            context.startActivity(intent)
        }
        holder.info.text = "${list.get(position).email}\n${list.get(position).name}\n${list.get(position).address}"

    }

    override fun getItemCount(): Int {
        return list.size
    }
}