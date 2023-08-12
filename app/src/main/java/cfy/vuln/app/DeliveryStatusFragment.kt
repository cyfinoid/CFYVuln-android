package cfy.vuln.app

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cfy.vuln.app.databinding.DeliverystatusLayoutBinding
import cfy.vuln.app.databinding.FragmentDeliveryStatusBinding
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.button.MaterialButton
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.util.HashMap


class DeliveryStatusFragment : Fragment() {

    lateinit var deliveryStatusBinding: FragmentDeliveryStatusBinding
    lateinit var appDB:AppDatabase
    lateinit var realm: Realm
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        deliveryStatusBinding=FragmentDeliveryStatusBinding.inflate(layoutInflater)

        // Add the following lines to create RecyclerView

        deliveryStatusBinding.recyclerview.setHasFixedSize(true)
        deliveryStatusBinding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        realm= Realm.getDefaultInstance()
        appDB=AppDatabase.getDatabase(requireContext())
        val productOrdered= appDB.productItemDao().getAllOrdered()

        //Order List
        if(productOrdered.isEmpty()){
            deliveryStatusBinding.noDelivery.visibility=View.VISIBLE
        }else{
            deliveryStatusBinding.noDelivery.visibility=View.GONE
        }
        deliveryStatusBinding.recyclerview.adapter = RvAdapter(productOrdered,requireContext(),"deliveryStatus")


        val sp=SharedPreference(requireContext())

        //Logout
        deliveryStatusBinding.deliveryLogout.setOnClickListener {

            val obj1 = realm.where(AllSessionsMgtNewNew::class.java).findAll()
            if (MainAct().isDebug()) {
                Log.d(Logname, obj1.toString() + ",," + sp.getValueString("sessionKey"))
            }
            realm.executeTransaction {
                val obj = realm.where(AllSessionsMgtNewNew::class.java)
                    .equalTo("id", sp.getValueInt("us_id")).findAll()
                obj?.apply {
                    realm.copyToRealmOrUpdate(this.onEach { it.status = 0 })

                    findNavController().navigate(cfy.vuln.app.R.id.action_nav_delivery_to_splash )
                    if (MainAct().isDebug()) {
                        Log.d(Logname, "Logout")
                    }
                    sp.clearSharedPreference()
                }
            }
        }


        return deliveryStatusBinding.root
    }


}


//List Adapter view
class RvAdapter(
    var value: List<OrderedItems>,
    var requireContext: Context,
    pageName: String,
) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    lateinit var appDB:AppDatabase
    // create an inner class with name ViewHolder
    inner class ViewHolder(val binding: DeliverystatusLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeliverystatusLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.quantityLayout.visibility=View.GONE
        binding.deliveryLayout.visibility=View.VISIBLE

        appDB=AppDatabase.getDatabase(requireContext)
        val spinner = binding.spinner

        val spinnerArrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext, R.layout.simple_spinner_item, parent.context.resources.getStringArray(cfy.vuln.app.R.array.Model)   )
        spinnerArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item) // The drop down view

        spinner.adapter = spinnerArrayAdapter
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){

            binding.productName.text = value[position].orderID
            binding.productPrice.text = "$"+df.format(value[position].totalPrice)
            val spinner = binding.spinner
            binding.checkPaymentStatus.visibility=View.VISIBLE
            binding.checkPaymentStatus.setOnClickListener {
                getPaymentStatus(value[position].paymentID,binding.checkPaymentStatus,binding.productPayment)
            }


            var products:String=""
            try {
                val jsArray = JSONArray(value[position].productList)
                for (i in 0 until jsArray.length()) {
                    val jsObj = jsArray.getJSONObject(i)
                    products += "${jsObj.getString("productName")} - ${jsObj.getInt("quantity")}"
                    if (i != jsArray.length() - 1) {
                        products += ","
                    }
                }
                binding.productDescription.text=products
            }catch (e:Exception){
                binding.productDescription.text=products
            }
            when (value[position].orderStatus) {
                1 -> {
                    spinner.setSelection(0)
                }
                2 -> {
                    spinner.setSelection(1)
                }
                3 -> {
                    spinner.setSelection(2)
                }
            }



            //Order Status Dropdown
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent!!.getItemAtPosition(position).toString()
                    if (MainAct().isDebug()) {
                        Log.d(Logname, selectedItem)
                    }
                    var deliveryMode=-1
                    if(selectedItem=="Processing"){
                        deliveryMode=1
                    }else if(selectedItem=="Ready to Deliver"){
                        deliveryMode=2
                    }else if(selectedItem=="Delivered"){
                        deliveryMode=3
                    }

                    val ioScope = CoroutineScope(Dispatchers.IO + Job())
                    if (MainAct().isDebug()) {
                        Log.d(Logname, "** ${holder.adapterPosition}")
                        Log.d(Logname, "** ${value[holder.adapterPosition].orderID!!}")
                        Log.d(Logname, "**" + appDB.productItemDao()
                            .findByOrderId(value[holder.adapterPosition].orderID!!)
                        )
                    }
                    ioScope.launch {


                        //changing order Status
                        appDB.productItemDao().updateOrderStatusOrderId(value[holder.adapterPosition].orderID!!,deliveryMode)

                    }



                } // to close the onItemSelected

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        }
    }

    //Payment status of ETH payment using api
    private fun getPaymentStatus(
        paymentID: String,
        checkPaymentStatus: MaterialButton,
        productPayment: TextView
    ) {
        val estimatedPriceURL ="https://api-sandbox.nowpayments.io/v1/payment/$paymentID"

        ///

        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET , estimatedPriceURL, null, { response ->
                    if (MainAct().isDebug()) {
                        Log.d(Logname, response.toString())
                    }
                    try {


                        checkPaymentStatus.text= response.getString("payment_status")
                        productPayment.visibility=View.VISIBLE
                        productPayment.text= "Payment for ${response.getString("pay_amount")} ETH"

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        if (MainAct().isDebug()) {
                            Log.d(Logname, e.toString())
                        }
                    }
                },
                Response.ErrorListener { error ->
                    if (MainAct().isDebug()) {
                        Log.i(Logname, "error = " + error)
                    }
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["x-api-key"] = "7T0WK2K-8TJ41JC-JPAHAMG-GH66X94"
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(50000 * 4, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)//9000*4
        MySingleton.getInstance(requireContext).addToRequestQueue(jsonObjectRequest)
        ///
    }


    // return the size of languageList
    override fun getItemCount(): Int {
        return value.size
    }
}

