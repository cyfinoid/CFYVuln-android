package cfy.vuln.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cfy.vuln.app.databinding.FragmentLoginBinding
import io.realm.Realm


class LoginFragment : Fragment(R.layout.fragment_login) {

    lateinit var fragmentLoginBinding: FragmentLoginBinding
    var realm: Realm?=null
    var loginStatus=0

    lateinit var appDB:AppDatabase
    private var mDB: SQLiteDatabase? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        fragmentLoginBinding = FragmentLoginBinding.inflate(inflater,container,false);
        realm=Realm.getDefaultInstance()
        appDB=AppDatabase.getDatabase(requireContext())



        //Login Function
        fragmentLoginBinding!!.loginButton.setOnClickListener {
            val usname=fragmentLoginBinding!!.userNameText.text.toString()
            val pass=fragmentLoginBinding!!.passwordText.text.toString()
            if (MainAct().isDebug()) {
                Log.d(Logname, "$usname,$pass")
            }

            ///Login Check
            if (!TextUtils.isEmpty(fragmentLoginBinding.userNameText.text.toString().trim { it <= ' ' }) && !TextUtils.isEmpty(fragmentLoginBinding.passwordText.text.toString().trim { it <= ' ' })    ) {

                when {
                    fragmentLoginBinding.userNameText.text.toString().trim { it <= ' ' }
                        .contains(" ") -> {
                        showSnackbar(getString(R.string.sign_up_user_name_no_spaces),requireContext())
                        fragmentLoginBinding.userNameText.requestFocus()
                    }
                    fragmentLoginBinding.userNameText.text.toString().length < 3 -> {
                        showSnackbar(
                            getString(R.string.sign_up_user_name_have_at_least_char),
                            requireContext()
                        )
                        fragmentLoginBinding.userNameText.requestFocus()
                    }
                    fragmentLoginBinding.passwordText.text.toString().trim { it <= ' ' }
                        .contains(" ") -> {
                        showSnackbar(
                            getString(R.string.sign_up_password_no_spaces),
                            requireContext()
                        )
                        fragmentLoginBinding.passwordText.requestFocus()
                    }
                    fragmentLoginBinding.passwordText.text.toString().length < 3 -> {
                        showSnackbar(
                            getString(R.string.sign_up_password_have_at_least_char),
                            requireContext()
                        )
                        fragmentLoginBinding.passwordText.requestFocus()
                    }
                    else -> {
                    //If all conditions satisfy

                        //Reading Db based on the username
                        val readLogin:List<UserDB> = realm!!.where(UserDB::class.java).equalTo("uname",fragmentLoginBinding.userNameText.text.toString().trim()).findAll()

                        if (MainAct().isDebug()) {
                            Log.d(Logname + "-", readLogin.toString())

                        }
                        if(readLogin.isEmpty()){
                            showSnackbar("Login Failed!", requireContext())
                        }
                        val sp=SharedPreference(requireContext())
                        for(iterate in readLogin.indices){
                            if (MainAct().isDebug()) {
                            Log.d(Logname,"${readLogin[iterate].uname==fragmentLoginBinding.userNameText.text.toString()}")
                            Log.d(Logname,"${readLogin[iterate].upass==fragmentLoginBinding.passwordText.text.toString()}")
                            }
                                //Admin login rights
                                if(loginStatus==5&&readLogin[iterate].status==3) {
                                if (readLogin[iterate].uname == fragmentLoginBinding.userNameText.text.toString() && readLogin[iterate].upass == fragmentLoginBinding.passwordText.text.toString()) {
                                    val sessionDB = AllSessionsMgtNewNew()
                                    sessionDB.id = readLogin[iterate].id
                                    sessionDB.status=readLogin[iterate].status
                                    val token= getRandomString(12)
                                    sessionDB.token = token
                                    sp.save("us_id",readLogin[iterate].id)
                                    sp.save("sessionKey",token)


                                    //Encrypting and saving in shared Preference
                                    val obj = Utils()
                                    val encName= obj.encrypt(readLogin[iterate].uname,  obj.key)
                                    println("Encrypted text: $encName")
                                    val encPass = obj.encrypt(readLogin[iterate].upass,  obj.key)
                                    println("Encrypted text: $encPass")
                                    sp.save("us_name",encName)
                                    sp.save("us_pass",encPass)




                                    realm!!.executeTransactionAsync { realm ->
                                        realm.copyToRealm(sessionDB )
                                    }
                       findNavController().popBackStack()
                        findNavController().navigate(R.id.nav_product_add)
                                }
                            }

                                //Normal user rights
                                else if(loginStatus!=5&&readLogin[iterate].status!=3){
                                if (readLogin[iterate].uname == fragmentLoginBinding.userNameText.text.toString() && readLogin[iterate].upass == fragmentLoginBinding.passwordText.text.toString()) {
                                    val sessionDB = AllSessionsMgtNewNew()
                                    sessionDB.id = readLogin[iterate].id
                                    val token= getRandomString(12)
                                    sessionDB.token = token

                                    //Encrypting and saving in shared Preference
                                    sp.save("us_id",readLogin[iterate].id)
                                    sp.save("sessionKey",token)
                                    val obj = Utils()
                                    val encName= obj.encrypt(readLogin[iterate].uname,  obj.key)
                                    println("Encrypted text: $encName")
                                    val encPass = obj.encrypt(readLogin[iterate].upass,  obj.key)
                                    println("Encrypted text: $encPass")
                                    sp.save("us_name",encName)
                                    sp.save("us_pass",encPass)

                                    realm!!.executeTransactionAsync { realm ->
                                        realm.copyToRealm(sessionDB )
                                    }
                                    findNavController().popBackStack()
                                    findNavController().navigate(R.id.nav_user_cart_add)
                                }else{
                                    showSnackbar("Login Failed!", requireContext())
                                }
                            }else{
                                    if (MainAct().isDebug()) {
                                        Log.d(Logname, "Login Failed")
                                    }
                                showSnackbar("Login Failed!", requireContext())
                            }
                        }
                    }
                }
            }
                else{

                    when {
                        TextUtils.isEmpty(fragmentLoginBinding.userNameText.text.toString()) -> {
                            fragmentLoginBinding.userNameText.requestFocus()
                        }
                        TextUtils.isEmpty(fragmentLoginBinding.passwordText.text.toString()) -> {
                            fragmentLoginBinding.passwordText.requestFocus()
                        }

                    }
                    showSnackbar(getString(R.string.sign_up_enter_all_details), requireContext())
                }
            ///Login Check
        }


        //Admin login initialization
        fragmentLoginBinding.loginLogo.setOnClickListener {
            loginStatus++
            if(loginStatus==5) {
                fragmentLoginBinding.userLogo.visibility = View.GONE
                fragmentLoginBinding.adminLogo.visibility = View.VISIBLE
            }else{
                if(loginStatus==6) {
                    loginStatus=0
                }
                fragmentLoginBinding.userLogo.visibility = View.VISIBLE
                fragmentLoginBinding.adminLogo.visibility = View.GONE
            }
            if (MainAct().isDebug()) {
                Log.d(Logname, loginStatus.toString())
            }

        }

        //New register Page
        fragmentLoginBinding!!.newRegisterText.setOnClickListener {

            val read:List<UserDB> = realm!!.where(UserDB::class.java).findAll()
            if (MainAct().isDebug()) {
                Log.d(Logname, read.toString())
            }
             findNavController().navigate(R.id.action_nav_sign_in_to_nav_sign_up)



        }

        readData()

        return fragmentLoginBinding!!.root
     }
    private fun showSnackbar(message: String, requireContext: Context) {

        //new
        try {
            requireActivity().window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try{

            Toast.makeText(requireContext,message, Toast.LENGTH_LONG).show()
        }catch (e:java.lang.Exception){}



    }


    //reading if product are available on the DB
    private fun readData(){
        if (MainAct().isDebug()) {
            Log.d(Logname, appDB.productItemDao().getAll().toString())
        }
    }

}