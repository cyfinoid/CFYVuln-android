package cfy.vuln.app

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri


open class SqlLiteHelper(context: Context):SQLiteOpenHelper(context, DATABASE_Name,null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_Name = "overalldatabase.db"
        const val TBL_USE = "users"
        private const val id = "id"
        private const val uname = "name"
        private const val upass = "pass"
        const val uemail = "email"



    }

    //SQL lite initialization and query
    override fun onCreate(db: SQLiteDatabase?) {
        val creaTBl =
            ("CREATE TABLE " + TBL_USE + " ( " + id + " INTEGER PRIMARY KEY, " + uname + " TEXT, " + upass + " TEXT," + uemail + " TEXT" + " )")
        db?.execSQL(creaTBl)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_USE")
        onCreate(db)
    }

    fun insertUser(us: UserModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(id, us.id)
        contentValues.put(uname, us.name)
        contentValues.put(upass, us.upass)
        contentValues.put(uemail, us.uemail)
        val sucess = db.insert(TBL_USE, null, contentValues)
        db.close()
        return sucess
    }

    fun getUnameCheck(name: String): ArrayList<UserModel> {
        val usList: ArrayList<UserModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_USE WHERE $uname='$name'"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()

        }
        var id: Int = 0
        var name: String = ""
        var pass: String = ""
        var email: String = ""
        if (cursor.moveToFirst()) {
            do {

                val id1: Int = cursor.getColumnIndex("id")
                id = cursor.getInt(id1)
                val name1: Int = cursor.getColumnIndex(uname)
                name = cursor.getString(name1)
                val pass1: Int = cursor.getColumnIndex(upass)
                pass = cursor.getString(pass1)
                val email1: Int = cursor.getColumnIndex(uemail)
                email = cursor.getString(email1)


                val us = UserModel(id = id, name = name, upass = pass, uemail = email)
                usList.add(us)
            } while (cursor.moveToNext())
        }
        return usList


    }

    fun getID(name: String): ArrayList<UserModel> {
        val usList: ArrayList<UserModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_USE WHERE $uname='$name'"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()

        }
        var id: Int = 0
        var name: String = ""
        var pass: String = ""
        var email: String = ""
        if (cursor.moveToFirst()) {
            do {

                val id1: Int = cursor.getColumnIndex("id")
                id = cursor.getInt(id1)
                val name1: Int = cursor.getColumnIndex(uname)
                name = cursor.getString(name1)
                val pass1: Int = cursor.getColumnIndex(upass)
                pass = cursor.getString(pass1)
                val email1: Int = cursor.getColumnIndex(uemail)
                email = cursor.getString(email1)


                val us = UserModel(id = id, name = name, upass = pass, uemail = email)
                usList.add(us)
            } while (cursor.moveToNext())
        }
        return usList


    }

    fun getDeepLinkUser(idReceived: String): ArrayList<UserModel> {
        val usList: ArrayList<UserModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_USE WHERE $id='$idReceived'"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()

        }
        var id: Int = 0
        var name: String = ""
        var pass: String = ""
        var email: String = ""
        if (cursor.moveToFirst()) {
            do {

                val id1: Int = cursor.getColumnIndex("id")
                id = cursor.getInt(id1)
                val name1: Int = cursor.getColumnIndex(uname)
                name = cursor.getString(name1)
                val pass1: Int = cursor.getColumnIndex(upass)
                pass = cursor.getString(pass1)
                val email1: Int = cursor.getColumnIndex(uemail)
                email = cursor.getString(email1)


                val us = UserModel(id = id, name = name, upass = pass, uemail = email)
                usList.add(us)
            } while (cursor.moveToNext())
        }
        return usList


    }


    fun getUEmailCheck(name: String): ArrayList<UserModel> {
        val usList: ArrayList<UserModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_USE WHERE $uemail='$name'"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()

        }
        var id: Int = 0
        var name: String = ""
        var pass: String = ""
        var email: String = ""
        if (cursor.moveToFirst()) {
            do {

                val id1: Int = cursor.getColumnIndex("id")
                id = cursor.getInt(id1)
                val name1: Int = cursor.getColumnIndex(uname)
                name = cursor.getString(name1)
                val pass1: Int = cursor.getColumnIndex(upass)
                pass = cursor.getString(pass1)
                val email1: Int = cursor.getColumnIndex(uemail)
                email = cursor.getString(pass1)

                val us = UserModel(id = id, name = name, upass = pass, uemail = email)
                usList.add(us)
            } while (cursor.moveToNext())
        }
        return usList


    }




}





