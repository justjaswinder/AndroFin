package com.raywenderlich.placebook.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.mad.kotlin_navigation_drawer.Fragment1
import com.mad.kotlin_navigation_drawer.Fragment2
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.util.replaceFragmenty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       setSupportActionBar(toolbar)
        setContentView(R.layout.activity_main)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        replaceFragmenty(
                fragment = Fragment1(),
                allowStateLoss = true,
                containerViewId = R.id.mainContent
        )
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
                replaceFragmenty(
                        fragment = Fragment1(),
                        allowStateLoss = true,
                        containerViewId = R.id.mainContent
                )
              //  setTitle("Import")
            }
            R.id.nav_gallery -> {
                replaceFragmenty(
                        fragment = Fragment2(),
                        allowStateLoss = true,
                        containerViewId = R.id.mainContent
                )
              //  setTitle("Gallery")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
