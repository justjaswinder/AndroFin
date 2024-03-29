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

package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import yoyo.jassie.labtest2.util.ImageUtils

class MapsViewModel(application: Application) : AndroidViewModel(application) {

  private val TAG = "MapsViewModel"

  private var bookmarkRepo: BookmarkRepo = BookmarkRepo(
      getApplication())
  private var bookmarks: LiveData<List<BookmarkMarkerView>>?
      = null

  fun addBookmarkFromPlace(bookmark: Bookmark,image: Bitmap) {

    val newId = bookmarkRepo.addBookmark(bookmark)
   image?.let { bookmark.setImage(it, getApplication()) }
    Log.i(TAG, "New bookmark $newId added to the database.")
  }

  fun getBookmarkMarkerViews() :
      LiveData<List<BookmarkMarkerView>>? {
    if (bookmarks == null) {
      mapBookmarksToMarkerView()
    }
    return bookmarks
  }

  private fun mapBookmarksToMarkerView() {
    bookmarks = Transformations.map(bookmarkRepo.allBookmarks) { repoBookmarks ->
      repoBookmarks.map { bookmark ->
        bookmarkToMarkerView(bookmark)
      }
    }
  }

  private fun bookmarkToMarkerView(bookmark: Bookmark):
      MapsViewModel.BookmarkMarkerView {
    return MapsViewModel.BookmarkMarkerView(
        bookmark.id,
        LatLng(bookmark.latitude, bookmark.longitude),
        bookmark.name,
            bookmark.gender,
            bookmark.birthday,
        bookmark.country)
  }

  data class BookmarkMarkerView(
      var id: Long? = null,
      var location: LatLng = LatLng(0.0, 0.0),
      var name: String = "",
      var gender: String = "",
      var birth: Long = 0L,
      var country: String = "") {
    fun getImage(context: Context): Bitmap? {
      id?.let {
        return ImageUtils.loadBitmapFromFile(context,
            Bookmark.generateImageFilename(it))
      }
      return null
    }
  }
}
