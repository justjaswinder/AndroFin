package com.raywenderlich.placebook.adapter

import android.arch.persistence.room.TypeConverter
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mad.kotlin_navigation_drawer.Fragment2.Companion.imageModelArrayList
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.raywenderlich.placebook.model.Bookmark

import android.widget.Filter


/**
 * Created by ThinkSoft on 20/12/2017.
 */
class CountryRecyclerAdapter(contacts: ArrayList<String>, listener: OnItemClickListener) : RecyclerView.Adapter<CountryRecyclerAdapter.RecyclerViewHolder>() {
    override fun getItemCount(): Int {
        return listContacts.size
    }

    override fun onBindViewHolder(p0: CountryRecyclerAdapter.RecyclerViewHolder, p1: Int) {


        var nameContact  = listContacts[p1]

        p0.mName.text = nameContact
        p0.bind(nameContact, listenerContact)

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerViewHolder {

    return RecyclerViewHolder(LayoutInflater.from(p0!!.context).inflate(com.raywenderlich.placebook.R.layout.country_item_list, p0,
            false))
}



   //  var isEmpty = false

    private var listContacts: ArrayList<String> = contacts

    private var listenerContact: OnItemClickListener = listener

    interface OnItemClickListener {

        fun onItemClick(contact: String)
    }








    fun addContacts(listContacts: ArrayList<String>) {
        this.listContacts = listContacts
       // this.filteredlistContacts = listContacts
        notifyDataSetChanged()
    }


    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



       var mName = itemView.findViewById<TextView>(R.id.name_contact)!!

        fun bind(contact:String, listener: OnItemClickListener) {
            itemView.setOnClickListener {
                listener.onItemClick(contact)
            }
        }

    }


}