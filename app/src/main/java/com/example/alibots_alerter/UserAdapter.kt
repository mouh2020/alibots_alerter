package com.example.alibots_alerter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class UserAdapter (context: Context, val users: ArrayList<User>): ArrayAdapter<User>(context,0,users){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.user_list, parent, false)
        val userPosition = getItem(position)!!
        val id = view.findViewById<TextView>(R.id.id)
        val firstname = view.findViewById<TextView>(R.id.firstname)
        val sent = view.findViewById<CheckBox>(R.id.sent)

        // Populate data
        id.text = userPosition.id
        firstname.text = userPosition.firstname

        // Temporarily remove the listener to avoid unnecessary triggers
        sent.setOnCheckedChangeListener(null)
        sent.isChecked = userPosition.sent

        // Set a new listener
        sent.setOnCheckedChangeListener { _, isChecked ->
            userPosition.sent = isChecked
            if (position == 0) {
                toggleCheckBoxes(isChecked)
            }
        }

        return view
    }

    private fun toggleCheckBoxes(isChecked:Boolean){
        for (user in users){
            user.sent = isChecked
        }
        notifyDataSetChanged()
    }
}