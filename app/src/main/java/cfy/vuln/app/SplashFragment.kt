package cfy.vuln.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cfy.vuln.app.databinding.FragmentSplashBinding
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    lateinit var splashBinding: FragmentSplashBinding
    private var handler: Handler = Handler(Looper.myLooper()!!)
    var runnable: Runnable? = null
    lateinit var realm: Realm
    lateinit var appDB:AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        splashBinding= FragmentSplashBinding.inflate(layoutInflater)
        realm= Realm.getDefaultInstance()
        val sp=SharedPreference(requireContext())

        //Logs in if the user id is present in Sharedpreference and the login status =1 in AllSessionsMgtNewNew
        var readSessionList: List<AllSessionsMgtNewNew>?=null
        try {
              readSessionList =
                realm.where(AllSessionsMgtNewNew::class.java).equalTo("id", sp.getValueInt("us_id"))
                    .findAll()!!
            if (MainAct().isDebug()) {
                Log.d(Logname + "-", readSessionList.toString())
            }
        }catch (e:Exception){}



         handler.postDelayed(Runnable {
            try{
                var stat=false
                readSessionList?.forEach { readSession ->
                    if (readSession.status == 1) {
                        sp.save("sessionKey", readSession.token)
                        stat = true

                    } else if (readSession.status == 0) {

                        stat = false
                    }
                }


                //Redirection to login page/ user order page
                if(stat){

                    if (findNavController().currentDestination!!.id == R.id.nav_splash) {
                        findNavController().navigate(R.id.action_nav_splash_to_user_add)
                    }
                }else  {
                    if (findNavController().currentDestination!!.id == R.id.nav_splash) {
                        findNavController().navigate(R.id.action_nav_splash_to_nav_sign_in)
                    }
                }
            }catch (e: Exception){
                if (MainAct().isDebug()) {
                    Log.d(Logname, "splash : " + e.message.toString())
                }
            }
        }.also { runnable = it }, 5000)

         appDB=AppDatabase.getDatabase(requireContext())
        val prodList=appDB.productItemDao().getAll()
        val productArrayList=ArrayList<ProductDetails>()
        prodList.forEach { list->
            productArrayList.add(ProductDetails(list.id!!,list.productName,list.productPrice,0,list.productImageURl,list.productDescription))

        }

        //Adding product for first time users
        if(productArrayList.size==0){
            writeData("Apple",10.0,"https://cdn.pixabay.com/photo/2016/01/05/13/58/apple-1122537_1280.jpg"," Apples for Sale")
            writeData("Orange",20.0,"https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?"," Oranges for Sale")
            writeData("Mango",40.0,"https://images.unsplash.com/photo-1553279768-865429fa0078"," Mangoes for Sale")
        }


        // Inflate the layout for this fragment
        return splashBinding.root
    }


    //Inserting products
    private fun writeData( prodName:String,prodPrice:Double,prodURL:String,prodDescription:String){
        val ioScope = CoroutineScope(Dispatchers.IO + Job())
        ioScope.launch {
            appDB.productItemDao(  ).insert(Product(null,prodName,prodPrice,prodURL,prodDescription))
            requireActivity().runOnUiThread {
                if (MainAct().isDebug()) {
                    Log.d(Logname, "Product Added successfully, initial add")
                }
            }

        }
    }


}