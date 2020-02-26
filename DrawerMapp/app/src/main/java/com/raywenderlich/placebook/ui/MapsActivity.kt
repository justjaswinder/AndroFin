/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.placebook.ui

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.LatLng
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

  private lateinit var map: GoogleMap
 // private lateinit var placesClient: PlacesClient
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var mapsViewModel: MapsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(com.raywenderlich.placebook.R.layout.activity_maps)

    val mapFragment = supportFragmentManager
        .findFragmentById(com.raywenderlich.placebook.R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)



    setupLocationClient()
   // setupPlacesClient()
  }

  override fun onMapReady(googleMap: GoogleMap) {
    map = googleMap

    setupMapListeners()
    setupViewModel()
    getCurrentLocation()


  }

//  private fun setupPlacesClient() {
//    Places.initialize(getApplicationContext(), "AIzaSyAs2FL7yrXp4KrOoBhlz-A3wYDckhMDFJk");
//    placesClient = Places.createClient(this);
//  }

  private fun setupMapListeners() {
    map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))

    map.setOnInfoWindowClickListener {
      handleInfoWindowClick(it)
    }
  }

  
  override fun onRequestPermissionsResult(requestCode: Int,
                                          permissions: Array<String>,
                                          grantResults: IntArray) {
    if (requestCode == REQUEST_LOCATION) {
      if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        getCurrentLocation()
      } else {
        Log.e(TAG, "Location permission denied")
      }
    }
  }

//    private fun createHandler() {
//        val thread = object:Thread() {
//            public override fun run() {
//                Looper.prepare()
//                val handler = Handler()
//                handler.postDelayed(object:Runnable {
//                    public override fun run() {
//
//                      //  mapsViewModel.addBookmarkFromPlace()
//
//
//
//                        handler.removeCallbacks(this)
//                        Looper.myLooper().quit()
//                    }
//                }, 2000)
//                Looper.loop()
//            }
//        }
//        thread.start()
//    }

  private fun setupViewModel() {
    mapsViewModel =
        ViewModelProviders.of(this).get(MapsViewModel::class.java)

     // createHandler()

      createBookmarkMarkerObserver()
  }

  private fun setupLocationClient() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
  }

  private fun startBookmarkDetails(bookmarkId: Long) {
    val intent = Intent(this, BookmarkDetailsActivity::class.java)
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
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
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
          Log.e(TAG, "No location found")
        }
      }
    }
  }

    override fun onCreateOptionsMenu(menu: android.view.Menu):
            Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                //addPlaceMark()
                val intent = Intent(this, BookmarkDetailsActivity::class.java)
                intent.putExtra("isnew", true)
                intent.putExtra(EXTRA_BOOKMARK_ID, 0)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

  private fun requestLocationPermissions() {
    ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        REQUEST_LOCATION)
  }

  companion object {
    const val EXTRA_BOOKMARK_ID =
        "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
    private const val REQUEST_LOCATION = 1
    private const val TAG = "MapsActivity"
  }

 // class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}
//37.4143651161
//-122.085373291