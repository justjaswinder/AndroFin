package com.mad.kotlin_navigation_drawer


import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.ContactRecyclerAdapter
import com.raywenderlich.placebook.ui.BookmarkDetailsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.fragment_fragment2.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class Fragment2 : android.support.v4.app.Fragment(), ContactRecyclerAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    override fun onQueryTextSubmit(query: String?): Boolean {
        recyclerViewAdapter!!.getFilter().filter(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
       // imageModelArrayList = recyclerViewAdapter!!.filter(newText)
        recyclerViewAdapter!!.getFilter().filter(newText)
       // recyclerViewAdapter!!.addContacts(recyclerViewAdapter!!.getFilter().filter(newText))
        return true
    }

    override fun onItemClick(contact: MapsViewModel.BookmarkMarkerView) {
        val intent = Intent(activity, BookmarkDetailsActivity::class.java)
        intent.putExtra("isnew", false)
        intent.putExtra(Fragment1.EXTRA_BOOKMARK_ID, contact.id)
        startActivity(intent)
    }



    private lateinit var mapsViewModel: MapsViewModel
    internal var textlength = 0
    private var viewSearch: SearchView? = null
    private var contactRecyclerView: RecyclerView? = null
    private var recyclerViewAdapter: ContactRecyclerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater!!.inflate(R.layout.fragment_fragment2, container, false)

        contactRecyclerView = view.findViewById(R.id.recycler_view)
        viewSearch = view.findViewById(R.id.search_bar) as SearchView
//        recyclerViewAdapter = ContactRecyclerAdapter(arrayListOf(), this)
//
//        contactRecyclerView!!.layoutManager = LinearLayoutManager(activity)
//        contactRecyclerView!!.adapter = recyclerViewAdapter
        mapsViewModel =
                ViewModelProviders.of(this).get(MapsViewModel::class.java)


        createBookmarkMarkerObserver()

        var toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("List")
        val btn =  toolbar?.findViewById(R.id.add) as Button
        btn.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, BookmarkDetailsActivity::class.java)
            intent.putExtra("isnew", true)
            intent.putExtra(Fragment1.EXTRA_BOOKMARK_ID, 0)
            startActivity(intent)
        })

        viewSearch!!.setOnQueryTextListener(this)

        return  view
    }


    private fun createBookmarkMarkerObserver() {
        mapsViewModel.getBookmarkMarkerViews()?.observe(
                this, android.arch.lifecycle
                .Observer<List<MapsViewModel.BookmarkMarkerView>> {

                    it?.let {
                        displayAllBookmarks(it)
                    }
                })
    }

    private fun displayAllBookmarks(
            bookmarks: List<MapsViewModel.BookmarkMarkerView>) {
        //imageModelArrayList = List<MapsViewModel.BookmarkMarkerView>
       // imageModelArrayList.toMutableList().addAll(bookmarks!!)
        recyclerViewAdapter = ContactRecyclerAdapter(bookmarks, this)

        contactRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        contactRecyclerView!!.adapter = recyclerViewAdapter
   // recyclerViewAdapter!!.addContacts(bookmarks)
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater!!.inflate(com.raywenderlich.placebook.R.menu.menu_bookmark, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            com.raywenderlich.placebook.R.id.action_add -> {
                //addPlaceMark()
                val intent = Intent(activity, BookmarkDetailsActivity::class.java)
                intent.putExtra("isnew", true)
                intent.putExtra(Fragment1.EXTRA_BOOKMARK_ID, 0)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
         var imageModelArrayList: List<MapsViewModel.BookmarkMarkerView>? = null
    }
}// Required empty public constructor
