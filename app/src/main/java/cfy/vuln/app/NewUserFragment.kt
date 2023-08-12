package cfy.vuln.app

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import cfy.vuln.app.databinding.FragmentNewUserBinding
import io.realm.Realm
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern


class NewUserFragment : Fragment() {

    lateinit var fragmentNewUserBinding: FragmentNewUserBinding
    var realm: Realm?=null
    var registrationStatus=0
    lateinit var sqlLiteHelper: SqlLiteHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentNewUserBinding= FragmentNewUserBinding.inflate(inflater,container,false)
        realm=Realm.getDefaultInstance()
    sqlLiteHelper=SqlLiteHelper(requireContext())

        


        //New user ButtonClick
        fragmentNewUserBinding!!.NewUserButton.setOnClickListener {
            //Sign up check

            if (!TextUtils.isEmpty(fragmentNewUserBinding.userNameTextNewUser.text.toString().trim { it <= ' ' }) && !TextUtils.isEmpty(fragmentNewUserBinding!!.passwordTextNewUser.text.toString().trim { it <= ' ' }) && !TextUtils.isEmpty(fragmentNewUserBinding!!.emailTextNewUser.text.toString().trim { it <= ' ' }) && !TextUtils.isEmpty(fragmentNewUserBinding!!.phoneTextNewUser.text.toString().trim { it <= ' ' })) {

                when {

                    fragmentNewUserBinding.userNameTextNewUser.text.toString().length < 3 -> {
                        showSnackbar(getString(R.string.sign_up_user_name_have_at_least_char))
                        fragmentNewUserBinding.userNameTextNewUser.requestFocus()
                    }
                    fragmentNewUserBinding.passwordTextNewUser.text.toString().trim { it <= ' ' }.contains(" ") -> {
                        showSnackbar(getString(R.string.sign_up_password_no_spaces))
                        fragmentNewUserBinding.passwordTextNewUser.requestFocus()
                    }
                    fragmentNewUserBinding.passwordTextNewUser.text.toString().length < 3 -> {
                        fragmentNewUserBinding.passwordTextNewUser.requestFocus()
                        showSnackbar(getString(R.string.sign_up_password_have_at_least_char))
                    }


                    fragmentNewUserBinding.phoneTextNewUser.text.toString().length != 10 -> {
                        fragmentNewUserBinding.phoneTextNewUser.requestFocus()
                        showSnackbar(getString(R.string.sign_up_phone_have_at_least_digit))
                    }

                    !emailValidator(fragmentNewUserBinding.emailTextNewUser.text.toString())->{
                        showSnackbar(getString(R.string.sign_up_email_invalid))
                        fragmentNewUserBinding.emailTextNewUser.requestFocus()
                    }
                    else -> {
                        //If all conditions satisfy

                        val unameCheck = sqlLiteHelper.getUnameCheck( fragmentNewUserBinding.userNameTextNewUser.text.toString())
                        Log.d(Logname+"check",unameCheck.toString())
                        val emailCheck = sqlLiteHelper.getUEmailCheck(fragmentNewUserBinding.emailTextNewUser.text.toString())
                        if (MainAct().isDebug()) {
                            Log.d(Logname + "check", emailCheck.toString())
                            Log.d(Logname+"check",unameCheck.toString())

                        }

                        //if user doesn't already exist
                        if (unameCheck.size == 0 && emailCheck.size == 0){


                        ///
                        val userModel = UserDB()
                        userModel.uname = fragmentNewUserBinding.userNameTextNewUser.text.toString()
                        userModel.upass = fragmentNewUserBinding.passwordTextNewUser.text.toString()
                        userModel.email = fragmentNewUserBinding.emailTextNewUser.text.toString()
                        userModel.phone =
                            fragmentNewUserBinding.phoneTextNewUser.text.toString().toInt()

                        //Admin Rights status
                        if (registrationStatus == 5) {
                            userModel.status = 3
                        }

                        //Storing data into realm DB
                        realm!!.executeTransactionAsync { realm -> realm.copyToRealm(userModel) }
                        if (MainAct().isDebug()) {
                            Log.d(Logname, "inserted$userModel")
                        }


                            //Creating a model and inserting user details for run time check of account existence
                        val user = UserModel(
                            name = userModel.uname,
                            upass = userModel.upass,
                            uemail = userModel.email
                        )
                        val status = sqlLiteHelper.insertUser(user)
                        if (status > -1) {
                            if (MainAct().isDebug()) {
                                Log.d(Logname, "added into sql")
                            }
                        } else {
                            if (MainAct().isDebug()) {
                                Log.d(Logname, "not added into sql")
                            }
                        }
                        showSnackbar(getString(R.string.sign_up_success))
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.nav_sign_in)
                    }else if(unameCheck.size!=0){

                            showSnackbar("Username Already Exists")
                            if (MainAct().isDebug()) {
                                Log.d(Logname, "Username Already Exists")
                            }
                    }else if(emailCheck.size!=0){
                            showSnackbar("Email id Already Exists")
                            if (MainAct().isDebug()) {
                                Log.d(Logname, "Email id Already Exists")
                            }
                        }
                    }
                }

            }
            else{

                when {
                    TextUtils.isEmpty(fragmentNewUserBinding.userNameTextNewUser.text.toString()) -> {
                        fragmentNewUserBinding.userNameTextNewUser.requestFocus()
                    }
                    TextUtils.isEmpty(fragmentNewUserBinding.passwordTextNewUser.text.toString()) -> {
                        fragmentNewUserBinding.passwordTextNewUser.requestFocus()
                    }
                    TextUtils.isEmpty(fragmentNewUserBinding.phoneTextNewUser.text.toString()) -> {
                        fragmentNewUserBinding.passwordTextNewUser.requestFocus()
                    }
                    TextUtils.isEmpty(fragmentNewUserBinding.emailTextNewUser.text.toString()) -> {
                        fragmentNewUserBinding.passwordTextNewUser.requestFocus()
                    }
                }
                showSnackbar(getString(R.string.sign_up_enter_all_details))
            }
            //Sign up check


            val usname=fragmentNewUserBinding.userNameTextNewUser.text.toString()
            val pass=fragmentNewUserBinding.passwordTextNewUser.text.toString()
            val num=fragmentNewUserBinding.phoneTextNewUser.text.toString()
            if (MainAct().isDebug()) {
                Log.d(Logname, "$usname,$pass, $num")
            }
        }

        //back button
        fragmentNewUserBinding.fabClose.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.nav_sign_in)
        }


        //Admin rights initialization
        fragmentNewUserBinding.newUserLogo.setOnClickListener {
            registrationStatus++
            if(registrationStatus==5) {
                fragmentNewUserBinding.userLogo.visibility = View.GONE
                fragmentNewUserBinding.adminLogo.visibility = View.VISIBLE
            }else{
                if(registrationStatus==6) {
                    registrationStatus=0
                }
                fragmentNewUserBinding.userLogo.visibility = View.VISIBLE
                fragmentNewUserBinding.adminLogo.visibility = View.GONE
            }
            if (MainAct().isDebug()) {
                Log.d(Logname, registrationStatus.toString())
            }
        }

        return fragmentNewUserBinding.root
    }

    private fun showSnackbar(message: String) {

        //new
        try {
            requireActivity().window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try{

            Toast.makeText(requireContext(),message, Toast.LENGTH_LONG).show()
        }catch (e:java.lang.Exception){}


    }

    //email validator Function
    fun emailValidator(email: String): Boolean {
        val pattern: Pattern
        val emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(emailPattern)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

}