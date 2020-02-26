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

import android.app.Activity
import android.app.DatePickerDialog

import kotlinx.android.synthetic.main.activity_bookmark_details.*
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.TypeConverter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageView
import com.raywenderlich.placebook.MainContext
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import yoyo.jassie.labtest2.util.ImageUtils
import yoyo.jassie.labtest2.viewmodel.PhotoOptionDialogFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class BookmarkDetailsActivity : AppCompatActivity(),PhotoOptionDialogFragment.PhotoOptionDialogListener, AdapterView.OnItemSelectedListener {

  var isNew = true
  var cal = Calendar.getInstance()
  var bookid = 0L
  var selGender = ""
  var list_of_items = arrayOf("Select Gender", "Male", "Female")

  private var photoFile: File? = null
  private var bookmarkRepo: BookmarkRepo = BookmarkRepo(
          MainContext.getAppContext())
  private lateinit var mapsViewModel: MapsViewModel
  private lateinit var bookmarkDetailsViewModel:
      BookmarkDetailsViewModel
  private var bookmarkDetailsView:
      BookmarkDetailsViewModel.BookmarkDetailsView? = null
  override fun onCaptureClick() {

    photoFile = null
    try {

      photoFile = ImageUtils.createUniqueImageFile(this)

    } catch (ex: java.io.IOException) {
      return
    }

    photoFile?.let { photoFile ->

      val photoUri = FileProvider.getUriForFile(this,
              "com.raywenderlich.placebook.fileprovider",
              photoFile)

      val captureIntent =
              Intent(MediaStore.ACTION_IMAGE_CAPTURE)

      captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
              photoUri)

      val intentActivities = packageManager.queryIntentActivities(
              captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
      intentActivities.map { it.activityInfo.packageName }
              .forEach { grantUriPermission(it, photoUri,
                      Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }

      startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

    }

  }

  override fun onPickClick() {
    val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int,
                                data: Intent?) {

      if (requestCode == 0) {
          if (resultCode == Activity.RESULT_OK) {
              val message = data!!.getStringExtra("message")
              //   Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
              editTextAddress.setText(message)
          }
      }
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == android.app.Activity.RESULT_OK) {

      when (requestCode) {

        REQUEST_CAPTURE_IMAGE -> {

          val photoFile = photoFile ?: return

          val uri = FileProvider.getUriForFile(this,
                  "com.raywenderlich.placebook.fileprovider",
                  photoFile)
          revokeUriPermission(uri,
                  Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

          val image = getImageWithPath(photoFile.absolutePath)
          image?.let { updateImage(it) }
        }

        REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
          val imageUri = data.data
          val image = getImageWithAuthority(imageUri!!)
          image?.let { updateImage(it) }
        }
      }
    }
  }

  private fun getImageWithAuthority(uri: Uri): Bitmap? {
    return ImageUtils.decodeUriStreamToSize(uri,
            resources.getDimensionPixelSize(
                    R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                    R.dimen.default_image_height),
            this)
  }

  private fun updateImage(image: Bitmap) {
    if(!isNew){
      val bookmarkView = bookmarkDetailsView ?: return
      imageViewPlace.setImageBitmap(image)
      bookmarkView.setImage(image,this)
    }else{
      imageViewPlace.setImageBitmap(image)
    }
  }

  private fun getImageWithPath(filePath: String): Bitmap? {
    return ImageUtils.decodeFileToSize(filePath,
            resources.getDimensionPixelSize(
                    R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                   R.dimen.default_image_height))
  }

  private fun replaceImage() {
    val newFragment = PhotoOptionDialogFragment.newInstance(this)
    newFragment?.show(supportFragmentManager, "photoOptionDialog")
  }
  override fun onCreate(savedInstanceState:
                        android.os.Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bookmark_details)
    setupToolbar()
    setupViewModel()
    getIntentData()

    imageViewPlace?.setOnClickListener {
      replaceImage()
    }


    val dateSetListener = object : DatePickerDialog.OnDateSetListener {
      override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                             dayOfMonth: Int) {
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthOfYear)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView()
      }
    }

    // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
    editTextNotes!!.setOnClickListener(object : View.OnClickListener {
      override fun onClick(view: View) {
        DatePickerDialog(this@BookmarkDetailsActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
      }

    })

      editTextAddress.setOnClickListener(View.OnClickListener {
          val intent = Intent(this, CountryAcitivity::class.java)
          startActivityForResult(intent, 0)
      })
    spinner!!.setOnItemSelectedListener(this)
    val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, list_of_items)
    // Set layout to use when the list of choices appear
    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    // Set Adapter to Spinner
    spinner!!.setAdapter(aa)
  }

  override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
    // use position to know the selected item
   selGender  = list_of_items[position]

  }

  override fun onNothingSelected(arg0: AdapterView<*>) {

  }


  private fun updateDateInView() {
    val myFormat = "MM/dd/yyyy" // mention the format you need
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    editTextNotes!!.setText(sdf.format(cal.getTime()).toString())
  }
// And override this method
//    @Override
//    public boolean onNavigateUp(){
//      finish();
//      return true;
//    }

  override fun onCreateOptionsMenu(menu: android.view.Menu):
      Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu_bookmark_details, menu)
    var itemToHide = menu.findItem(R.id.action_delete)
    if(isNew){
        itemToHide.setVisible(false);
    }else{
      itemToHide.setVisible(true);
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_save -> {
        if(isNew){
          createHandler()
        }else {
          saveChanges()
        }
        return true
      }

      R.id.action_delete -> {
        delete()
        return true
      }

      else -> return super.onOptionsItemSelected(item)
    }
  }

  private fun addNew() {
    val name = editTextName.text.toString()
    if (name.isEmpty()) {
      return
    }

    val bookmark = bookmarkRepo.createBookmark()
    // bookmark.placeId = "123"

      bookmark.name = editTextName.text.toString()
var datee = editTextNotes.text.toString()
    val myFormat = "MM/dd/yyyy" // mention the format you need
    val sdf = SimpleDateFormat(myFormat, Locale.US)

    var date1 = fromDate(sdf.parse(datee))

    bookmark.birthday = date1

      bookmark.latitude = java.lang.Double.parseDouble(editTextLat.text.toString())
      bookmark.longitude = java.lang.Double.parseDouble(editTextLong.text.toString())
      bookmark.country = editTextAddress.text.toString()
      bookmark.gender = selGender//editTextPhone.text.toString()

      mapsViewModel =
              ViewModelProviders.of(this).get(MapsViewModel::class.java)
    var drawable = imageViewPlace.getDrawable() as BitmapDrawable
    var bitmap = drawable.getBitmap()
      mapsViewModel.addBookmarkFromPlace(bookmark, bitmap)

    finish()
  }

  private fun createHandler() {
    val thread = object:Thread() {
      public override fun run() {
        Looper.prepare()
        val handler = Handler()
        handler.postDelayed(object:Runnable {
          public override fun run() {

            //  mapsViewModel.addBookmarkFromPlace()

            addNew()


            handler.removeCallbacks(this)
            Looper.myLooper().quit()
          }
        }, 2000)
        Looper.loop()
      }
    }
    thread.start()
  }

  private fun delete(){

    bookmarkDetailsView?.let { bookmarkView ->
      bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
      bookmarkDetailsViewModel.getBookmark(bookid)?.removeObservers(this);
    }
    finish()
  }




  private fun saveChanges() {
    val name = editTextName.text.toString()
    if (name.isEmpty()) {
      return
    }
    bookmarkDetailsView?.let { bookmarkView ->
      bookmarkView.name = editTextName.text.toString()
     // bookmarkView.birthday = editTextNotes.text.toString() as Long

      var date = editTextNotes.text.toString()

      val myFormat = "MM/dd/yyyy" // mention the format you need
      val sdf = SimpleDateFormat(myFormat, Locale.US)

      bookmarkView.birthday = fromDate(sdf.parse(date))
      bookmarkView.latitude = java.lang.Double.parseDouble(editTextLat.text.toString())
      bookmarkView.longitude = java.lang.Double.parseDouble(editTextLong.text.toString())
      bookmarkView.country = editTextAddress.text.toString()
      bookmarkView.gender = selGender// editTextPhone.text.toString()
      bookmarkDetailsViewModel.updateBookmark(bookmarkView)
    }
    finish()
  }

  private fun getIntentData() {

     isNew = intent.getBooleanExtra("isnew", true)

    if(!isNew) {
      val bookmarkId = intent.getLongExtra(
              MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)

      bookid = bookmarkId

      bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
              this, Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {

        it?.let {
          bookmarkDetailsView = it
          // Populate fields from bookmark
          populateFields()
        populateImageView()
        }
      })


    }
  }

  private fun setupViewModel() {
    bookmarkDetailsViewModel =
        ViewModelProviders.of(this).get(
            BookmarkDetailsViewModel::class.java)
  }

  private fun setupToolbar() {


    setSupportActionBar(toolbar)
    supportActionBar?.setHomeButtonEnabled(true)

     //  setDisplayHomeAsUpEnabled(true);
  //toolbar.set
  }

  private fun populateFields() {
    bookmarkDetailsView?.let { bookmarkView ->
      editTextName.setText(bookmarkView.name)
      editTextPhone.setText(bookmarkView.gender)
      editTextLat.setText(bookmarkView.latitude.toString())
      editTextLong.setText(bookmarkView.longitude.toString())
      var date = toDate(bookmarkView.birthday!!)

      if(bookmarkView.gender == "Male"){
        spinner?.setSelection(1,true)
      }else{
        spinner?.setSelection(2,true)
      }
      val myFormat = "MM/dd/yyyy" // mention the format you need
      val sdf = SimpleDateFormat(myFormat, Locale.US)
      editTextNotes.setText(sdf.format(date).toString())
      editTextAddress.setText(bookmarkView.country)
    }
  }


  @TypeConverter
  fun fromDate(date: Date): Long {
    return (date!!.getTime()).toLong()
  }
  @TypeConverter
  fun toDate(dateLong: Long): Date {
    return  Date(dateLong)
  }

  private fun populateImageView() {
    bookmarkDetailsView?.let { bookmarkView ->
      val placeImage = bookmarkView.getImage(this)
      placeImage?.let {
        imageViewPlace?.setImageBitmap(placeImage)
      }
    }
    imageViewPlace?.setOnClickListener {
      replaceImage()
    }
  }

  companion object {
    private const val REQUEST_CAPTURE_IMAGE = 1
    private const val REQUEST_GALLERY_IMAGE = 2
  }

}
