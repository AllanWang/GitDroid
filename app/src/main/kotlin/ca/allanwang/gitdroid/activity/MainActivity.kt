package ca.allanwang.gitdroid.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.databinding.ActivityMainBinding
import ca.allanwang.kau.utils.snackbar
import com.google.android.material.navigation.NavigationView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var bindings: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(bindings.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, bindings.drawerLayout, bindings.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        bindings.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        bindings.navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (bindings.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            bindings.drawerLayout.closeDrawer(GravityCompat.START)
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
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            else -> snackbar("Coming soon!")
        }
        bindings.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
