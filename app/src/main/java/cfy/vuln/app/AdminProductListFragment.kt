package cfy.vuln.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cfy.vuln.app.databinding.FragmentAdminProductListBinding
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class AdminProductListFragment : Fragment() {
 lateinit var adminProductListBinding: FragmentAdminProductListBinding
 lateinit var realm: Realm
    lateinit var appDB:AppDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        realm= Realm.getDefaultInstance()
        adminProductListBinding= FragmentAdminProductListBinding.inflate(layoutInflater)

        //Product Add
        adminProductListBinding!!.addProductButton.setOnClickListener {
            val  prodName=adminProductListBinding.productNameText.text.toString().trim()
            var prodPrice=0.0
            try {
                prodPrice = adminProductListBinding.priceText.text.toString().trim().toDouble()
            }catch (e:Exception){}
            val  prodURL=adminProductListBinding.productURLText.text.toString().trim()
            val prodDescription=adminProductListBinding.productDescription.text.toString().trim()
            appDB=AppDatabase.getDatabase(requireContext())

            if(prodName!=""&&prodPrice!=0.0&&prodURL!=""&&prodDescription!=""){
                writeData(prodName,prodPrice,prodURL,prodDescription)
            }else{
                Toast.makeText(requireContext(),"Enter All Details!",Toast.LENGTH_LONG).show()
            }
            if (MainAct().isDebug()) {
                Log.d(Logname, "$prodName,$prodPrice,$prodURL,$prodDescription")
            }
        }


        val sp=SharedPreference(requireContext())
        //Logout
        adminProductListBinding.logoutAdminAdd.setOnClickListener {

            val obj1 = realm.where(AllSessionsMgtNewNew::class.java).findAll()
            if (MainAct().isDebug()) {
                Log.d(Logname, obj1.toString() + ",," + sp.getValueString("sessionKey"))
            }
            realm.executeTransaction {
                val obj = realm.where(AllSessionsMgtNewNew::class.java).equalTo("id", sp.getValueInt("us_id")).findAll()
                obj?.apply {
                    realm.copyToRealmOrUpdate(this.onEach { it.status=0 })
                    findNavController().navigate(R.id.action_nav_prod_add_to_splash)
                    if (MainAct().isDebug()) {
                        Log.d(Logname, "Logout")
                    }
                    sp.clearSharedPreference()
                }
            }

        }




         return adminProductListBinding.root
    }

    //Insert data
    private fun writeData( prodName:String,prodPrice:Double,prodURL:String,prodDescription:String){
        val ioScope = CoroutineScope(Dispatchers.IO + Job())
        ioScope.launch {
            appDB.productItemDao(  ).insert(Product(null,prodName,prodPrice,prodURL,prodDescription))
            activity?.runOnUiThread(Runnable {
                //Your code

            adminProductListBinding.productNameText.setText("")

             adminProductListBinding.priceText.setText("")

           adminProductListBinding.productURLText.setText("")
            adminProductListBinding.productDescription.setText("")

            Toast.makeText(requireContext(),"Product added successfully ",Toast.LENGTH_LONG).show()
            })
        }
    }

}