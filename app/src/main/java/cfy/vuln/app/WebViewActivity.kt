package cfy.vuln.app


import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException


class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val extras = intent.extras
        var value=""
        var localFile=""
        if (extras != null) {
            //To read product name and search in google
            value = extras.getString("name","")
            //To read data from local file
            localFile = extras.getString("system","")

        }
       val  wb = findViewById<WebView>(R.id.webView1)
       val  close = findViewById<ImageView>(R.id.closeAct)
        wb.settings.also { jj->
            jj.javaScriptEnabled = true
            jj.domStorageEnabled = true
            jj.setSupportZoom(true)
            jj.allowUniversalAccessFromFileURLs=true
            jj.defaultTextEncodingName = "utf-8"
        }
        wb.webViewClient = CustomWebViewClient()


        wb.settings.allowFileAccess = true;
        wb.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        close.setOnClickListener {
            finish()
        }

        wb.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                if (MainAct().isDebug()) {
                    Log.d(Logname, message)
                }

                return true
            }
        }



        if(localFile!="") {

            val file = File(localFile)
            wb.loadUrl("file:///$file")
        }else if(value!=""){
            wb.loadUrl("https://www.google.com/search?q=$value")
        }


    }
}


class CustomWebViewClient : WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        // Allow file access
        if (request.url.scheme == "file") {
            try {
                return WebResourceResponse(
                    "application/javascript",
                    "UTF-8",
                    view.context.assets.open(request.url.path!!.substring(1))
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        // Handle error here
        if (MainAct().isDebug()) {
            Log.d(Logname, description.toString())
            Log.d(Logname, errorCode.toString())
        }
    }
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, er: SslError?) {
        handler.proceed()
        if (MainAct().isDebug()) {
            Log.d(Logname, er.toString())
        }
        // Ignore SSL certificate errors
    }

}

