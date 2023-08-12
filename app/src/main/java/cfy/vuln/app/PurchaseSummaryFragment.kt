package cfy.vuln.app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cfy.vuln.app.databinding.FragmentPurchaseSummaryBinding
import cfy.vuln.app.databinding.QuantitySummaryLayoutBinding
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class PurchaseSummaryFragment : Fragment() {

    lateinit var fragmentPurchaseSummaryBinding: FragmentPurchaseSummaryBinding
    lateinit var appDB: AppDatabase
    var usdPrice=0.0
    var ethPrice=0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentPurchaseSummaryBinding = FragmentPurchaseSummaryBinding.inflate(layoutInflater)
        fragmentPurchaseSummaryBinding.summaryRecyclerviewNew.setHasFixedSize(true)
        fragmentPurchaseSummaryBinding.summaryRecyclerviewNew.layoutManager =
            LinearLayoutManager(requireContext())
        appDB = AppDatabase.getDatabase(requireContext())

        val summaryList = ArrayList<ProductDetails>()


        var totalPrice = 0.0
        productArrayList.forEach {
            if (it.quantity != 0) {
                summaryList.add(it)
                totalPrice += it.productPrice * it.quantity
            }
        }
        getEstimatedPrice(totalPrice)

        val jsArray = JSONArray()
        summaryList.forEach {
            val jsonObject = JSONObject()
            jsonObject.put("productId", it.productId)
            jsonObject.put("productName", it.productName)
            jsonObject.put("productPrice", it.productPrice)
            jsonObject.put("quantity", it.quantity)
            jsonObject.put("productDescription", it.productDescription)
            jsArray.put(jsonObject)
        }
        fragmentPurchaseSummaryBinding.summaryRecyclerviewNew.adapter =
            PurchaseSummaryAdapter(summaryList, requireContext(), "deliveryStatus")


        fragmentPurchaseSummaryBinding.pageClose.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.nav_user_cart_add)
        }

        fragmentPurchaseSummaryBinding.purchaseAmount.text = "$ ${df.format(totalPrice)}"

        fragmentPurchaseSummaryBinding.btnSubmitProceed.setOnClickListener {
            if(usdPrice>120) {
                createEthPayment(usdPrice, ethPrice, getRandomString(18),jsArray.toString(),requireContext())
            }else{
                Toast.makeText(requireContext(),"Cannot purchase less that $120",Toast.LENGTH_SHORT).show()
            }

        }
        return fragmentPurchaseSummaryBinding.root
    }

    //Eth equivalent price calculation using API
    private fun getEstimatedPrice(price: Double ) {
        val estimatedPriceURL =
            "https://api-sandbox.nowpayments.io/v1/estimate?amount=$price&currency_from=usd&currency_to=eth"

        ///
        val requestBody = ""
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET , estimatedPriceURL, null, { response ->
                    if (MainAct().isDebug()) {
                        Log.d(Logname, response.toString())
                    }
                    try {


                        ethPrice = response.getString("estimated_amount").toDouble()
                        usdPrice = response.getString("amount_from").toDouble()
                        fragmentPurchaseSummaryBinding.purchaseAmount.text="$ $usdPrice ($ethPrice ETH)"
                        fragmentPurchaseSummaryBinding.btnSubmitProceed.text="Pay $ethPrice ETH"
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
        MySingleton.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest)
        ///
    }


    //Creating Payment Request for the selected amount of product
    private fun createEthPayment(price: Double,ethAmount:Double,orderID:String,orderDescription:String,context: Context) {
        val createPaymentEth ="https://api-sandbox.nowpayments.io/v1/payment"
        val jsonObject=JSONObject()
        try {
            jsonObject.put("price_amount", price)
            jsonObject.put("price_currency", "usd")
            jsonObject.put("pay_amount", ethAmount)
            jsonObject.put("pay_currency", "eth")
            jsonObject.put("order_id", orderID)
            jsonObject.put("order_description", "testing")
            jsonObject.put("case", "success")
            jsonObject.put(  "ipn_callback_url","https://nowpayments.io",)


        }catch(e:JSONException){}
        if (MainAct().isDebug()) {
            Log.d(Logname, jsonObject.toString())
        }



        ////
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(
                 Request.Method.POST, createPaymentEth, jsonObject, { response ->
                    if (MainAct().isDebug()) {
                        Log.d(Logname, response.toString())
                    }
                    try {


                        val paymentId = response.getString("payment_id")
                        val payAddress = response.getString("pay_address")
                        val purchaseId = response.getString("purchase_id")
                        val sp = SharedPreference(requireContext())
                        val ioScope = CoroutineScope(Dispatchers.IO + Job())
                        if (MainAct().isDebug()) {
                            Log.d(Logname, paymentId.toString())
                            Log.d(Logname, payAddress.toString())
                            Log.d(Logname, purchaseId.toString())
                        }
                        ioScope.launch {
                            appDB.productItemDao().insertOrder(
                                OrderedItems(
                                    null,sp.getValueString("sessionKey")!!, sp.getValueInt("us_id"),
                                    orderID, getCurrentTime(), 1, usdPrice, orderDescription,paymentId,purchaseId)
                            )
                            activity?.runOnUiThread(Runnable {
                                if (MainAct().isDebug()) {
                                    Log.d(Logname, "Product ordered")
                                }
                                findNavController().popBackStack()
                                findNavController().navigate(R.id.nav_user_cart_add)
                            })
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        if (MainAct().isDebug()) {
                            Log.d(Logname, e.toString())
                        }
                    }
                },
                Response.ErrorListener { error ->
                    if (MainAct().isDebug()) {
                        Log.i(Logname, "error = " + error.message)
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
        MySingleton.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest)
        ////
    }

}


class PurchaseSummaryAdapter(
    var prodList: ArrayList<ProductDetails>,
    var requireContext: Context,
    pageName: String,
) : RecyclerView.Adapter<PurchaseSummaryAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: QuantitySummaryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            QuantitySummaryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)




        return ViewHolder(binding)
    }






    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            binding.productName.text = prodList[position].productName
            binding.productCount.text = prodList[position].quantity.toString()
            binding.productPrice.text = "$ ${df.format(prodList[position].productPrice*prodList[position].quantity)}"



        }
    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return prodList.size
    }
}

