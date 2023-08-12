package cfy.vuln.app

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView


class ReferralActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referal)


        if(intent.data!!.getQueryParameter("refer")!=""&&intent.data!!.getQueryParameter("refer")!=null){

            if (MainAct().isDebug()) {
                Log.d(Logname, intent.data!!.getQueryParameter("refer")!!)
            }
            val sqlLiteHelper=SqlLiteHelper(applicationContext)

            val userName=sqlLiteHelper.getDeepLinkUser(intent.data!!.getQueryParameter("refer")!!.toString())
            if (MainAct().isDebug()) {
                Log.d(Logname, userName.toString())
            }


        val refText= findViewById<MaterialTextView>(R.id.referalText)
            try {
                refText.text = "Congratulations you have been referred by ${userName[0].name}"
            }catch (e:Exception){
                refText.text ="ERROR!!!"
            }
        }
        val close=findViewById<ImageView>(R.id.closeRef)
        close.setOnClickListener {
            finish()
        }
    }
}