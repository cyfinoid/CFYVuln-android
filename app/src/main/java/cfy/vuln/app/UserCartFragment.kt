package cfy.vuln.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cfy.vuln.app.databinding.DeliverystatusLayoutBinding
import cfy.vuln.app.databinding.FragmentUserCartBinding
import coil.load
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat


internal var productArrayList = ArrayList<ProductDetails>()


val df = DecimalFormat("0.00")



class UserCartFragment : Fragment(), SearchView.OnQueryTextListener {
    var realm: Realm?=null

lateinit var fragmentUserCartBinding: FragmentUserCartBinding
lateinit var appDB:AppDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        fragmentUserCartBinding= FragmentUserCartBinding.inflate(layoutInflater)



        fragmentUserCartBinding.recyclerview.setHasFixedSize(true)
        fragmentUserCartBinding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        appDB=AppDatabase.getDatabase(requireContext())
        realm=Realm.getDefaultInstance()
        val prodList=appDB.productItemDao().getAll()
        productArrayList=ArrayList()
        prodList.forEach { list->
            productArrayList.add(ProductDetails(list.id!!,list.productName,list.productPrice,0,list.productImageURl,list.productDescription))

        }





        //if no products were added initially
        if(productArrayList.size==0){
            writeData("Apple",10.0,"https://cdn.pixabay.com/photo/2016/01/05/13/58/apple-1122537_1280.jpg"," Apples for Sale")
            writeData("Orange",20.0,"https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?"," Oranges for Sale")
            writeData("Mango",40.0,"https://images.unsplash.com/photo-1553279768-865429fa0078"," Mangoes for Sale")
        }
        else{
            fragmentUserCartBinding.noProd.visibility=View.GONE
        }




        //adapter setup
        fragmentUserCartBinding.recyclerview.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
           val  selectProductAdapter =UserProductAdapter(productArrayList,requireContext(),"UserProduct")

            adapter = selectProductAdapter
            selectProductAdapter.setOnItemClickListenerSelectedImage(object :
                UserProductAdapter.OnItemClickListenerImage {
                override fun onItemClickImage(
                    item: String
                ) {
                    val bundle = Bundle()
                    bundle.putString("searchURL", "https://www.google.com/search?q="+item.replace(" ","+"))
                    findNavController().navigate(R.id.action_nav_cart_add_to_imageview,bundle)
                }
            })

            selectProductAdapter.setOnItemClickListenerSelected(object :
                UserProductAdapter.OnItemClickListener {
                override fun onItemClick(
                    item: String,
                    format: String,
                    qrtNew: Int,
                ) {

                    try {
                        if (qrtNew > 0) {
                            fragmentUserCartBinding.tvItemsPrice.text = item
                            fragmentUserCartBinding.viewCartMine.visibility =
                                View.VISIBLE

                        } else {
                            fragmentUserCartBinding.tvItemsPrice.text =
                                "Your cart is Empty!"
                            fragmentUserCartBinding.viewCartMine.visibility =
                                View.GONE
                        }

                        fragmentUserCartBinding.ivShoppingCart.startAnimation(
                            AnimationUtils.loadAnimation(context,
                                R.anim.shake))

                        fragmentUserCartBinding.viewCartMine.setOnClickListener {
                            findNavController().navigate(R.id.nav_user_cart_summary)

                        }

                    } catch (e: java.lang.Exception) {
                    }

                }
            })

        }


        val sp=SharedPreference(requireContext())

        //Logout
        fragmentUserCartBinding.cartLogout.setOnClickListener {

            val obj1 = realm!!.where(AllSessionsMgtNewNew::class.java).findAll()
            if (MainAct().isDebug()) {
                Log.d(Logname, obj1.toString() + ",," + sp.getValueString("sessionKey"))
            }
            realm!!.executeTransaction {
                val obj = realm!!.where(AllSessionsMgtNewNew::class.java).equalTo("id", sp.getValueInt("us_id")).findAll()
                obj?.apply {
                    realm.copyToRealmOrUpdate(this.onEach { it.status=0 })
                    findNavController().navigate(R.id.action_nav_cart_add_to_splash)
                    if (MainAct().isDebug()) {
                        Log.d(Logname, "Logout")
                    }
                    sp.clearSharedPreference()
                }
            }

        }

        //Deeplink share link
        fragmentUserCartBinding.shareUser.setOnClickListener {
            val  sqlLiteHelper=SqlLiteHelper(requireContext())
            val id=sqlLiteHelper.getID(Utils().decrypt(sp.getValueString("us_name")!!,Utils().key))
            if (MainAct().isDebug()) {
                Log.d(Logname, sp.getValueString("us_name") + "--" + id.toString())
            }
            val shareText="http://cfyuln/?refer=${id[0].id}"
            setClipboard(requireContext(),shareText)
        }

        fragmentUserCartBinding.searchView.setOnQueryTextListener(this)


        return fragmentUserCartBinding.root
    }

    //Copying Deeplink to click board
    private fun setClipboard(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            clipboard.text = text
            Toast.makeText(requireContext(),"Copied to Clipboard",Toast.LENGTH_LONG).show()
        } else {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(),"Copied to Clipboard",Toast.LENGTH_LONG).show()
        }
    }


    //Inserting Products
    private fun writeData( prodName:String,prodPrice:Double,prodURL:String,prodDescription:String){
        val ioScope = CoroutineScope(Dispatchers.IO + Job())
        ioScope.launch {
            appDB.productItemDao(  ).insert(Product(null,prodName,prodPrice,prodURL,prodDescription))
            activity?.runOnUiThread(Runnable {
                //Your code



                Toast.makeText(requireContext(),"Product added successfully ",Toast.LENGTH_LONG).show()
            })
        }
    }




    override fun onQueryTextSubmit(query: String?): Boolean {

        return true
    }



    override fun onQueryTextChange(newText: String?): Boolean {
        searchLogic(newText)
        return false
    }


    //Filter function to search product
    private fun searchLogic(searchString: String?) {
        fragmentUserCartBinding.recyclerview.apply {
            if (searchString?.isNotEmpty() == true) {

                val filterProdList = appDB.productItemDao().findByName(searchString)
                val filProdList=ArrayList<ProductDetails>()
                filterProdList.forEach { list->
                            productArrayList.forEach {
                                if(it.productName==list.productName){
                            filProdList.add(ProductDetails(list.id!!,list.productName,list.productPrice,it.quantity,list.productImageURl,list.productDescription))
                                  }
                                }

                }
                if (MainAct().isDebug()) {
                    Log.d(Logname, filProdList.toString())
                }
              val   selectProductAdapter=UserProductAdapter(filProdList,requireContext(),"UserProduct")
                adapter = selectProductAdapter
                selectProductAdapter.setOnItemClickListenerSelectedImage(object :
                    UserProductAdapter.OnItemClickListenerImage {
                    override fun onItemClickImage(
                        item: String
                    ) {
                        val bundle = Bundle()
                        bundle.putString("searchURL", "https://www.google.com/search?q="+item.replace(" ","+"))
                        findNavController().navigate(R.id.action_nav_cart_add_to_imageview,bundle)
                    }
                })

                selectProductAdapter.setOnItemClickListenerSelected(object :
                    UserProductAdapter.OnItemClickListener {
                    override fun onItemClick(
                        item: String,
                        format: String,
                        qrtNew: Int,
                    ) {

                        try {
                            if (qrtNew > 0) {
                                fragmentUserCartBinding.tvItemsPrice.text = item
                                fragmentUserCartBinding.viewCartMine.visibility =
                                    View.VISIBLE

                            } else {
                                fragmentUserCartBinding.tvItemsPrice.text =
                                    "Your cart is Empty!"
                                fragmentUserCartBinding.viewCartMine.visibility =
                                    View.GONE
                            }

                            fragmentUserCartBinding.ivShoppingCart.startAnimation(
                                AnimationUtils.loadAnimation(context,
                                    R.anim.shake))

                            fragmentUserCartBinding.viewCartMine.setOnClickListener {
                                findNavController().navigate(R.id.nav_user_cart_summary)

                            }

                        } catch (e: java.lang.Exception) {
                        }

                    }
                })


            } else {

                val   selectProductAdapter=UserProductAdapter(productArrayList,requireContext(),"UserProduct")
                 adapter = selectProductAdapter
                selectProductAdapter.setOnItemClickListenerSelectedImage(object :
                    UserProductAdapter.OnItemClickListenerImage {
                    override fun onItemClickImage(
                        item: String
                    ) {
                        val bundle = Bundle()
                        bundle.putString("searchURL", "https://www.google.com/search?q="+item.replace(" ","+"))
                        findNavController().navigate(R.id.action_nav_cart_add_to_imageview,bundle)
                    }
                })

                selectProductAdapter.setOnItemClickListenerSelected(object :
                    UserProductAdapter.OnItemClickListener {
                    override fun onItemClick(
                        item: String,
                        format: String,
                        qrtNew: Int,
                    ) {

                        try {
                            if (qrtNew > 0) {
                                fragmentUserCartBinding.tvItemsPrice.text = item
                                fragmentUserCartBinding.viewCartMine.visibility =
                                    View.VISIBLE

                            } else {
                                fragmentUserCartBinding.tvItemsPrice.text =
                                    "Your cart is Empty!"
                                fragmentUserCartBinding.viewCartMine.visibility =
                                    View.GONE
                            }

                            fragmentUserCartBinding.ivShoppingCart.startAnimation(
                                AnimationUtils.loadAnimation(context,
                                    R.anim.shake))

                            fragmentUserCartBinding.viewCartMine.setOnClickListener {
                                findNavController().navigate(R.id.nav_user_cart_summary)

                            }

                        } catch (e: java.lang.Exception) {
                        }

                    }
                })

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 300) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Calendar permission is granted
                // Perform your calendar-related operations here
                if (MainAct().isDebug()) {
                    Log.d(Logname, "Permission Granted")
                }


            } else {
                // Calendar permission is denied
                // Handle the scenario when permission is denied
                if (MainAct().isDebug()) {
                    Log.d(Logname, "Permission not Granted")
                }
            }
        }
    }


}


//Product List
class UserProductAdapter(
    var prodList: ArrayList<ProductDetails>,
    var requireContext: Context,
    pageName: String,
) : RecyclerView.Adapter<UserProductAdapter.ViewHolder>() {

    private var mOnItemClickListenerSelected: OnItemClickListener? = null
    private var mOnItemClickListenerSelectedImage: OnItemClickListenerImage? = null
    interface OnItemClickListener {
        fun onItemClick(item: String, format: String, qrtNew: Int)
    }

    fun setOnItemClickListenerSelected(mItemClickListener: OnItemClickListener) {
        mOnItemClickListenerSelected = mItemClickListener
    }
    interface OnItemClickListenerImage {
        fun onItemClickImage(item: String )
    }

    fun setOnItemClickListenerSelectedImage(mItemClickListenerImage: OnItemClickListenerImage) {
        mOnItemClickListenerSelectedImage = mItemClickListenerImage
    }
    inner class ViewHolder(val binding: DeliverystatusLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeliverystatusLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)




         binding.quantityLayout.visibility=View.VISIBLE
         binding.deliveryLayout.visibility=View.GONE


        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserProductAdapter.ViewHolder, position: Int) {
        with(holder){
            binding.productName.text = prodList[position].productName.toString()
            binding.productPrice.text = "$"+df.format(prodList[position].productPrice)
            binding.productDescription.text = prodList[position].productDescription.toString()
            binding.productCount.text = prodList[position].quantity.toString()
            binding.productLogo.load(prodList[position].imagePath)
            binding.productLogo.setOnClickListener {

                //Google search redirection to know more about the product using Webview
                val intent = Intent(requireContext, WebViewActivity::class.java)
                intent.putExtra("name",prodList[position].productName)
                requireContext.startActivity(intent)
            }

            binding.minusProduct.setOnClickListener {
                try {


                    //overall qty
                    productArrayList.forEach {
                        if (it.productName == prodList[position].productName) {
                            if (it.quantity > 0) {
                                it.quantity--

                                binding.productCount.text = it.quantity.toString()
                                if (MainAct().isDebug()) {
                                    Log.d(Logname + "qty", it.quantity.toString())
                                }
                            }
                        }
                        if (MainAct().isDebug()) {
                            Log.d(Logname, position.toString())
                        }

                    }
                    countPrice()
                } catch (e: Exception) {
                }
            }
            binding.plusProduct.setOnClickListener {

                try {


                    //overall qty
                    productArrayList.forEach {
                        if (it.productName == prodList[position].productName) {
                            if (it.quantity >= 0)/*&& overallCountItemStatus*/ {
                                it.quantity++

                                binding.productCount.text = it.quantity.toString()
                            }
                            if (MainAct().isDebug()) {
                                Log.d(Logname + "qty", it.quantity.toString())
                            }
                        }
                    }
                 if (MainAct().isDebug()) {
                        Log.d(Logname, position.toString())
                    }





                    countPrice()
                } catch (e: Exception) {
                }

            }


        }
    }


    fun countPrice() {
        var totalPrice = 0.00
        var qrtNew = 0

        productArrayList.forEach {
            totalPrice += it.quantity * it.productPrice
            qrtNew += it.quantity
        }



        if (mOnItemClickListenerSelected != null) {
             mOnItemClickListenerSelected!!.onItemClick("$qrtNew items | $ ${df.format(totalPrice)}",
                df.format(totalPrice),qrtNew)
        }
        if (MainAct().isDebug()) {

            Log.d(Logname, totalPrice.toString())
        }
    }


    // return the size of languageList
    override fun getItemCount(): Int {
        return prodList.size
    }
}


data class ProductDetails(
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    var quantity: Int,
    var imagePath: String,
    var productDescription: String
)