package cfy.vuln.app

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import cfy.vuln.app.databinding.ActivityMainBinding
import io.realm.Realm
import java.io.File


internal var Logname="vulnLog"
class MainAct : AppCompatActivity()  {

var activityMainBinding:ActivityMainBinding?=null
    private lateinit var navController: NavController
    var realm: Realm?=null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding!!.root)

        //Root and Emulator Detection
        if(isRooted(applicationContext)||isEmulator(applicationContext)){
            finish()
        }else {

            realm = Realm.getDefaultInstance()

            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
            navController = navHostFragment.navController
//        setupActionBarWithNavController(this,navController)
            activityMainBinding!!.bottomNavigationView.visibility = View.VISIBLE
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.nav_sign_in) {

                    activityMainBinding!!.bottomNavigationView.visibility = View.GONE
                }
                if (destination.id == R.id.nav_delivery_status || destination.id == R.id.nav_product_add) {
                    activityMainBinding!!.bottomNavigationView.visibility = View.VISIBLE
                }
            }



            //Bottom Navigation handling
            activityMainBinding!!.bottomNavigationView.setOnItemSelectedListener {
                if (MainAct().isDebug()) {
                    Log.d(Logname, navController.currentDestination?.id.toString())
                }
                    when (it.itemId) {

                    R.id.addProductMenu -> {
                        if (navController.currentDestination?.id != R.id.nav_product_add) {
                            navController.navigate(R.id.action_nav_delivery_to_add)
                        }
                    }
                    R.id.deliveryStatusMenu -> {
                        if (navController.currentDestination?.id != R.id.nav_delivery_status) {
                            navController.navigate(R.id.action_nav_prod_add_to_delivery)
                        }
                    }
                    R.id.shoppingMenu -> {
                        if (navController.currentDestination?.id != R.id.nav_user_cart_add) {
                            navController.navigate(R.id.action_nav_cart_ordered_to_card_add)
                        }
                    }
                    R.id.orderedMenu -> {
                        if (navController.currentDestination?.id != R.id.nav_user_cart_ordered) {
                            navController.navigate(R.id.action_nav_cart_add_to_ordered)
                        }
                    }
                }
                true
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.nav_splash, R.id.nav_sign_in, R.id.nav_sign_up, R.id.nav_delivery_status, R.id.nav_product_add, R.id.nav_user_cart_ordered, R.id.nav_user_cart_add, R.id.nav_user_cart_summary -> {

                        if (destination.id == R.id.nav_splash) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                            activityMainBinding!!.bottomNavigationView.visibility = View.INVISIBLE
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                        }
                        if (destination.id == R.id.nav_user_cart_summary) {
                            activityMainBinding!!.bottomNavigationView.visibility = View.INVISIBLE
                        }
                        if (destination.id == R.id.nav_delivery_status || destination.id == R.id.nav_product_add || destination.id == R.id.nav_user_cart_ordered || destination.id == R.id.nav_user_cart_add) {
                            activityMainBinding!!.bottomNavigationView.visibility = View.VISIBLE
                        }
                        if (destination.id == R.id.nav_user_cart_ordered || destination.id == R.id.nav_user_cart_add) {
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.addProductMenu).isVisible =
                                false
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.deliveryStatusMenu).isVisible =
                                false
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.orderedMenu).isVisible =
                                true
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.shoppingMenu).isVisible =
                                true

                        } else if (destination.id == R.id.nav_delivery_status || destination.id == R.id.nav_product_add) {
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.orderedMenu).isVisible =
                                false
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.shoppingMenu).isVisible =
                                false
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.addProductMenu).isVisible =
                                true
                            activityMainBinding!!.bottomNavigationView.menu.findItem(R.id.deliveryStatusMenu).isVisible =
                                true
                        }


                    }
                }
            }
        }




    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() ||super.onSupportNavigateUp()
    }



    //Checking if the phone is rooted
    fun isRooted(context: Context): Boolean {
        val isEmulator = isEmulator(context)
        val buildTags = Build.TAGS
        return if (!isEmulator && buildTags != null && buildTags.contains("test-keys")) {
            true
        } else {
            var file = File("/system/app/Superuser.apk")
            if (file.exists()) {
                true
            } else {
                file = File("/system/xbin/su")
                !isEmulator && file.exists()
            }
        }
    }

    //Checking if the device is rooted
    fun isEmulator(context: Context): Boolean {
        val androidId = Secure.getString(context.getContentResolver(), "android_id")
        return "sdk" == Build.PRODUCT || "google_sdk" == Build.PRODUCT || androidId == null
    }

    //Checking if the build is in Debug or Release mode
    fun isDebug():Boolean{
        return BuildConfig.DEBUG
    }
}
