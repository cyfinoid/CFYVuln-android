package cfy.vuln.app

import java.util.*


//Usermodel data class
data class UserModel(
    var id:Int=getAutoId(),
    var name:String="",
    var upass:String="",
    var uemail:String=""
) {

    companion object {
        fun getAutoId(): Int {
            val random = Random()
            return random.nextInt(10000)
        }
    }
}