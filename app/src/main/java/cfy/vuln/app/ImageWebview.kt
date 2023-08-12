package cfy.vuln.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cfy.vuln.app.databinding.FragmentImageWebviewBinding


class ImageWebview : Fragment() {
    lateinit var fragmentImageWebviewBinding: FragmentImageWebviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        fragmentImageWebviewBinding=   FragmentImageWebviewBinding.inflate(layoutInflater)


        val imageURL= requireArguments().getString("searchURL")
//        val imageURL= "https://www.google.com/search?q=prod+A"
        if (MainAct().isDebug()) {
            Log.d(Logname, imageURL.toString())
        }


        fragmentImageWebviewBinding.webview.settings.javaScriptEnabled = true;
        fragmentImageWebviewBinding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        }
        fragmentImageWebviewBinding.webview.loadUrl(imageURL!!)


        fragmentImageWebviewBinding.closeImageview.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.nav_user_cart_add)
        }



        return fragmentImageWebviewBinding.root


    }
}
