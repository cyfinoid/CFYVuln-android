package cfy.vuln.app


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SimpleSQLiteQuery
import cfy.vuln.app.databinding.DeliverystatusLayoutBinding
import cfy.vuln.app.databinding.FragmentOrderedStatusBinding
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.button.MaterialButton
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


class OrderedStatusFragment : Fragment() {

    lateinit var deliveryStatusBinding: FragmentOrderedStatusBinding
    lateinit var appDB:AppDatabase
    lateinit var realm: Realm
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        deliveryStatusBinding=FragmentOrderedStatusBinding.inflate(layoutInflater)
        realm= Realm.getDefaultInstance()

        deliveryStatusBinding.recyclerview.setHasFixedSize(true)
        deliveryStatusBinding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        appDB=AppDatabase.getDatabase(requireContext())
        val sp=SharedPreference(requireContext())
        if (MainAct().isDebug()) {
            Log.d(Logname, "##${sp.getValueInt("us_id")}")
        }

        //get Ordered Details based on user
        val productOrdered= appDB.productItemDao().findByID(sp.getValueInt("us_id"))

        if(productOrdered.isEmpty()){
            deliveryStatusBinding.noOrder.visibility=View.VISIBLE
        }else{
            deliveryStatusBinding.noOrder.visibility=View.GONE
        }

        deliveryStatusBinding.recyclerview.adapter = OrderedStatusAdapter(productOrdered,requireContext(),"deliveryStatus")


        deliveryStatusBinding.orderLogout.setOnClickListener {

            val obj1 = realm.where(AllSessionsMgtNewNew::class.java).findAll()
            if (MainAct().isDebug()) {
                Log.d(Logname, obj1.toString() + ",," + sp.getValueString("sessionKey"))
            }
            realm.executeTransaction {
                val obj = realm.where(AllSessionsMgtNewNew::class.java)
                    .equalTo("id", sp.getValueInt("us_id")).findAll()
                obj?.apply {
                    realm.copyToRealmOrUpdate(this.onEach { it.status = 0 })

                        findNavController().navigate(R.id.action_nav_cart_ordered_to_splash )
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



//Ordered Items Adapter
class OrderedStatusAdapter(
    var value: List<OrderedItems>,
    var requireContext: Context,
    pageName: String,
) : RecyclerView.Adapter<OrderedStatusAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: DeliverystatusLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeliverystatusLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.quantityLayout.visibility=View.GONE
        binding.deliveryLayout.visibility=View.VISIBLE




      binding.spinner.visibility=View.GONE


        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
                binding.productDescription.text = "#"+value[position].orderID
                binding.productPrice.text ="$"+ df.format(value[position].totalPrice)
                binding.checkPaymentStatus.visibility=View.VISIBLE
                binding.checkPaymentStatus.setOnClickListener {
                    getPaymentStatus(value[position].paymentID,binding.checkPaymentStatus,binding.productPayment)
                }

            binding.productStatus.text= when (value[position].orderStatus) {
                1 -> {
                    "Processing"
                }
                2 -> {
                    "Ready to Deliver"
                }
                3 -> {
                    "Delivered"
                }
                else -> {"-"}
            }


            //extracting deetails of the Purchase
            var products:String=""
             val jsArray=JSONArray(value[position].productList)
             for(i in 0 until jsArray.length()){
                val jsObj=jsArray.getJSONObject(i)
                 products += "${jsObj.getString("productName")} - ${jsObj.getInt("quantity")} nos"
                 if(i!=jsArray.length()-1){
                     products+=","
                 }
             }


                binding.productName.text=products


                binding.spinner.visibility=View.GONE


        }
    }

    //Checking payment status using ETH payment in sandbox mode
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
                        Log.d(Logname, "error = " + error)
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

    override fun getItemCount(): Int {
        return value.size
    }
}

