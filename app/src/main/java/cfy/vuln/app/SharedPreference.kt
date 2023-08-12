package cfy.vuln.app

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(context: Context) {
    private val prefName = "CFYVuln"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    //********************************************************************************************** save all
    //To Store String data
    fun save(KEY_NAME: String, text: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, text)
        editor.apply()
    }
    //..............................................................................................
    //To Store Int data
    fun save(KEY_NAME: String, value: Int) {

        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(KEY_NAME, value)
        editor.apply()
    }

    //..............................................................................................
    //To Store Floating data
    fun save(KEY_NAME: String, value: Float) {

        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putFloat(KEY_NAME, value)
        editor.apply()
    }


    //..............................................................................................
    //To Store Boolean data
    fun save(KEY_NAME: String, status: Boolean) {

        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.apply()
    }
    //********************************************************************************************** retrieve selected
    //********************************************************************************************** retrieve selected
    //To Retrieve String
    fun getValueString(KEY_NAME: String): String? {

        return    sharedPref.getString(KEY_NAME, "")

    }

    //..............................................................................................
    //To Retrieve Int
    fun getValueInt(KEY_NAME: String): Int {

        return sharedPref.getInt(KEY_NAME, 0)
    }

    //..............................................................................................
    // To Retrieve Boolean
    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {

        return sharedPref.getBoolean(KEY_NAME, defaultValue)
    }
    //..............................................................................................

    //To Retrieve String : need to change double

    //********************************************************************************************** delete all
    // To clear all data
    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }
    //********************************************************************************************** delete selected

}