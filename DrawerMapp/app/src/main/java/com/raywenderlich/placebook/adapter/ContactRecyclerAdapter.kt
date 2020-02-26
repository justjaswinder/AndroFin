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
import android.widget.ImageView
import com.raywenderlich.placebook.MainContext


/**
 * Created by ThinkSoft on 20/12/2017.
 */
class ContactRecyclerAdapter(contacts: List<MapsViewModel.BookmarkMarkerView>, listener: OnItemClickListener) : RecyclerView.Adapter<ContactRecyclerAdapter.RecyclerViewHolder>() {
    override fun onBindViewHolder(holder: RecyclerViewHolder, p1: Int) {

        if(!isEmpty) {
            var currentContact: MapsViewModel.BookmarkMarkerView = filteredlistContacts[p1]

            holder!!.mCountry.visibility = View.VISIBLE
            holder!!.logo_user_image_view.visibility = View.VISIBLE
            holder!!.mGender.visibility = View.VISIBLE
            holder!!.mBirth.visibility = View.VISIBLE
            var nameContact = currentContact.name
            var gender = currentContact.gender
            var country = currentContact.country
            var date = toDate(currentContact.birth)

            holder!!.mName.text = nameContact
            holder!!.mCountry.text = country.toString()
            holder!!.mGender.text = gender.toString()
            holder!!.logo_user_image_view.setImageBitmap(currentContact.getImage(MainContext.getAppContext()))
//
            val myFormat = "MM/dd/yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)

            holder!!.mBirth.text = sdf.format(date).toString()
//
            holder.bind(currentContact, listenerContact)
        }else{
            holder!!.mName.text = "NO Results FOUND!"
          holder!!.mCountry.visibility = View.GONE
            holder!!.logo_user_image_view.visibility = View.GONE
            holder!!.mGender.visibility = View.GONE
            holder!!.mBirth.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerViewHolder {
if(isEmpty){
    return RecyclerViewHolder(LayoutInflater.from(p0!!.context).inflate(com.raywenderlich.placebook.R.layout.empty_item_list, p0,
            false))
}else{
    return RecyclerViewHolder(LayoutInflater.from(p0!!.context).inflate(com.raywenderlich.placebook.R.layout.item_list, p0,
            false))
}


    }
     var isEmpty = false

    private var listContacts: List<MapsViewModel.BookmarkMarkerView> = contacts
    private var filteredlistContacts: List<MapsViewModel.BookmarkMarkerView> = contacts

    private var listenerContact: OnItemClickListener = listener

    interface OnItemClickListener {
        fun onItemClick(contact: MapsViewModel.BookmarkMarkerView)
    }


    override fun getItemCount(): Int {
      //  var i: Int = filteredlistContacts.size
        if(isEmpty){
            return 1
        }else {
            return filteredlistContacts.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
        if(isEmpty)
            return 0
        else {
            return 1
        }

    }


    fun getFilter(): Filter {
        return object : Filter() {
            protected override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredlistContacts = results.values as List<MapsViewModel.BookmarkMarkerView>
                if(filteredlistContacts.size <= 0){
                    isEmpty = true

                }else {
                    isEmpty = false}
                Log.e("SIZEEEE",filteredlistContacts.size.toString())
                notifyDataSetChanged()
            }

            protected override fun performFiltering(constraint: CharSequence): FilterResults {
                var filteredResults: List<MapsViewModel.BookmarkMarkerView>? = null
                if (constraint.length == 0) {
                    filteredResults = listContacts
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase())
                }

                val results = FilterResults()
                results.values = filteredResults

                return results
            }
        }
    }

    protected fun getFilteredResults(constraint: String): List<MapsViewModel.BookmarkMarkerView> {
        val results: MutableList<MapsViewModel.BookmarkMarkerView> = ArrayList()

        for (item in listContacts) {
            if (item.name.toLowerCase().contains(constraint)) {
                results.add(item)
            }
        }
        return results
    }


    fun addContacts(listContacts: List<MapsViewModel.BookmarkMarkerView>) {
        this.listContacts = listContacts
        this.filteredlistContacts = listContacts
        notifyDataSetChanged()
    }


    @TypeConverter
    fun toDate(dateLong: Long): Date {
        return  Date(dateLong)
    }
    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



       var mName = itemView.findViewById<TextView>(R.id.name_contact)!!
        var mGender = itemView.findViewById<TextView>(R.id.number_contact)!!
        var mCountry = itemView.findViewById<TextView>(R.id.tution_contact)!!
        var logo_user_image_view = itemView.findViewById<ImageView>(R.id.logo_user_image_view)!!
        var mBirth = itemView.findViewById<TextView>(R.id.date_contact)!!

        fun bind(contact: MapsViewModel.BookmarkMarkerView, listener: OnItemClickListener) {
            itemView.setOnClickListener {
                listener.onItemClick(contact)
            }
        }

    }


}