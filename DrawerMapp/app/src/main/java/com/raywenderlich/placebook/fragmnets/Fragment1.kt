package com.mad.kotlin_navigation_drawer


import android.Manifest
import android.app.Fragment
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.Button
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.ui.BookmarkDetailsActivity
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import com.google.android.gms.maps.MapView
import com.raywenderlich.placebook.MainContext
import com.raywenderlich.placebook.ui.MainActivity
import kotlinx.android.synthetic.main.app_bar_main.*


/**
 * A simple [Fragment] subclass.
 */
class Fragment1 : android.support.v4.app.Fragment() {
//    override fun onMapReady(p0: GoogleMap?) {
//        map = p0!!
//
//        setupMapListeners()
//        setupViewModel()
//        getCurrentLocation()
//    }


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapsViewModel: MapsViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val v =inflater!!.inflate(com.raywenderlich.placebook.R.layout.fragment_fragment1, container, false)
        val mapFragment = childFragmentManager.findFragmentById(com.raywenderlich.placebook.R.id.mapp) as SupportMapFragment?

//          Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
//        ((AppCompatActivity)getActivity()).getSupportActionBar(toolbar);

        mapFragment?.getMapAsync( OnMapReadyCallback {
            map = it!!

            setupMapListeners()
            setupViewModel()
            getCurrentLocation()
        })

        var toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("MAP")
      val btn =  toolbar?.findViewById(R.id.add) as Button
        btn.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, BookmarkDetailsActivity::class.java)
            intent.putExtra("isnew", true)
            intent.putExtra(EXTRA_BOOKMARK_ID, 0)
            startActivity(intent)
        })
       setupLocationClient()
        return v
    }


    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this!!.activity!!))

        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == Fragment1.REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e("", "Location permission denied")
            }
        }
    }

    private fun setupViewModel() {
        mapsViewModel =
                ViewModelProviders.of(this).get(MapsViewModel::class.java)

        // createHandler()

        createBookmarkMarkerObserver()
    }

    private fun setupLocationClient() {
       fusedLocationClient = LocationServices.getFusedLocationProviderClient(this!!.activity!!)
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(activity, BookmarkDetailsActivity::class.java)
        intent.putExtra("isnew", false)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
//      is MapsActivity.PlaceInfo -> {
//      //  val placeInfo = (marker.tag as PlaceInfo)
//        if (placeInfo.place != null) {
//          GlobalScope.launch {
//            mapsViewModel.addBookmarkFromPlace()
//          }
//        }
//        marker.remove();
//      }
            is MapsViewModel.BookmarkMarkerView -> {
                val bookmarkMarkerView = (marker.tag as
                        MapsViewModel.BookmarkMarkerView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun createBookmarkMarkerObserver() {
        mapsViewModel.getBookmarkMarkerViews()?.observe(
                this, android.arch.lifecycle
                .Observer<List<MapsViewModel.BookmarkMarkerView>> {

                    map.clear()

                    it?.let {
                        displayAllBookmarks(it)
                    }
                })
    }

    private fun displayAllBookmarks(
            bookmarks: List<MapsViewModel.BookmarkMarkerView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun addPlaceMarker(
            bookmark: MapsViewModel.BookmarkMarkerView): Marker? {
        val marker = map.addMarker(MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.country)
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_ORANGE))
                .alpha(0.8f))
        marker.tag = bookmark
        var latLng: LatLng? = null
        latLng = LatLng(bookmark.location.latitude, bookmark.location.longitude)

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        return marker
    }
    //37.4394482403
    //   -122.100479492
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this!!.activity!!, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Log.e("", "No location found")
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
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
                intent.putExtra(EXTRA_BOOKMARK_ID, 0)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this!!.activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION)
    }

    companion object {
        const val EXTRA_BOOKMARK_ID =
                "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
    }
}// Required empty public constructor
