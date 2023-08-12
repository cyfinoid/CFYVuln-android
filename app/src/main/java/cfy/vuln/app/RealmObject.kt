package cfy.vuln.app

import android.util.Log
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/*@RealmClass
open class Person: RealmObject, RealmModel {
    @PrimaryKey
    var id: Int = nextIdFun()
    var name: String = ""
    var age: Int = 0
}*/

//Realm Db structure

@RealmClass
open class UserDB:RealmObject,RealmModel{
    @PrimaryKey  var id:Int= nextIdFun()
    var uname: String = ""
    var upass: String = ""
    var email: String = ""
    var phone: Int =0
    var status:Int=1


}



@RealmClass
open class AllSessionsMgtNewNew:RealmObject,RealmModel{
    @PrimaryKey var id:Int=0
    var token:String=""
    var time=getCurrentTime()
    var status=1

}



fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
    return sdf.format(Date())

}

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}


fun nextIdFun():Int{
    val nextId=Random.nextInt(100, 50000)
    return nextId
}

