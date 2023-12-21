package com.dev.frequenc.ui_codes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dev.agorademo2.PermissionsManager
import com.dev.frequenc.R
import com.dev.frequenc.databinding.ActivityMainBinding
import com.dev.frequenc.ui_codes.connect.Profile.EditProfileActivity
import com.dev.frequenc.ui_codes.connect.chat.AllChatListViewModel
import com.dev.frequenc.ui_codes.connect.chat.AllChatUserFragment
import com.dev.frequenc.ui_codes.connect.home.ConnectHomeFragment
import com.dev.frequenc.ui_codes.data.AudienceDataResponse
import com.dev.frequenc.ui_codes.data.UpdateAgoraDetailResponse
import com.dev.frequenc.ui_codes.data.UpdateAgoraModel
import com.dev.frequenc.ui_codes.screens.Dashboard.MarketPlaceFragment
import com.dev.frequenc.ui_codes.screens.Dashboard.savedevent.SavedEventFragment
import com.dev.frequenc.ui_codes.screens.Dashboard.wallet.WalletFragment
import com.dev.frequenc.ui_codes.screens.Profile.AudienceProfileActivity
import com.dev.frequenc.ui_codes.screens.booking_process.booking_history.BookingHistoryFragment
import com.dev.frequenc.ui_codes.screens.utils.ApiClient
import com.dev.frequenc.ui_codes.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import io.agora.CallBack
import io.agora.ConnectionListener
import io.agora.Error
import io.agora.chat.ChatClient
import io.agora.chat.ChatOptions
import io.agora.chat.uikit.EaseUIKit
import io.agora.cloud.HttpClientManager
import io.agora.cloud.HttpClientManager.Method_POST
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var allChatListViewModel: AllChatListViewModel

    //    private var pwd: String? = "skkfjdsd"
    private var pwd: String? = null

    //                private var username: String? = "skkfjdsd"
//    private var username: String? = "7777444422"
//    private var username: String? = "52452423324234"
    private var username: String? = null
    val requestcode = 101
    var latitude = ""
    var longitude = ""
    var exitCount = 0
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var authorization: String
    lateinit var audience_id: String
    lateinit var binding: ActivityMainBinding
    private lateinit var connectionListener: ConnectionListener
//    private lateinit var walletViewModel: WalletViewModel

    companion object {
        private const val NEW_LOGIN = "NEW_LOGIN"
        private const val RENEW_TOKEN = "RENEW_TOKEN"
        private const val LOGIN_URL = "https://a61.chat.agora.io/app/chat/user/login"

        //        private const val REGISTER_URL = "https://a61.chat.agora.io/app/chat/user/register"
        private const val REGISTER_URL = "https://a61.chat.agora.io/611070665/1250094/users"

        //        private const val REGISTER_URL = "https://a61.chat.agora.io/app/chat/token"
        private const val REGISTER_USER_URL = "https://a61.chat.agora.io/611070665/1250094/users"

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE)!!

//        binding.drawerLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)

        allChatListViewModel =
            ViewModelProvider(this@MainActivity)[AllChatListViewModel::class.java]
//        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


        val connectFragment = ConnectHomeFragment()
        val marketPlace = MarketPlaceFragment()
        val walletFragment = WalletFragment()
        val savedEventFragment = SavedEventFragment()
        val bookingHistoryFragment = BookingHistoryFragment()

        setCurrentFragment(marketPlace, "MarketPlaceFragment")


        try {
            username = sharedPreferences.getString(Constants.User_Id, null)
            val mob_no = sharedPreferences.getString(Constants.PhoneNo, null)
            pwd = username!!.substring(
                (username!!.lastIndex - 5),
                (username!!.lastIndex)
            ) + "@${mob_no!!.substring(mob_no.lastIndex - 5, mob_no.lastIndex)}"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_marketplace ->
                    setCurrentFragment(marketPlace, "MarketPlaceFragment")

                R.id.bottom_chat -> {
                    Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show()

                    //                    Toast.makeText(this,"Releasing soon",Toast.LENGTH_SHORT).show()
//                    setCurrentFragment(allChatUserFragment, "AllChatUserFragment")
                }

                R.id.bottom_connect ->
//                    Toast.makeText(this,"Releasing soon",Toast.LENGTH_SHORT).show()

//                    setCurrentFragment(connectFragment, "ConnectFragment")
                    Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show()


//                    Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show()

                R.id.bottom_wallet -> setCurrentFragment(walletFragment, "WalletFragment")

            }

            true
        }


        val userRegistered = sharedPreferences.getBoolean(Constants.isUserTypeRegistered, false)

        authorization = sharedPreferences.getString(Constants.Authorization, "-1").toString()
        audience_id = sharedPreferences.getString(Constants.AudienceId, "-1").toString()

        requestPermissions()

        if (userRegistered && !(authorization == "-1") && !(audience_id == "-1")) {

            Log.d("Audience Id", audience_id)
            Log.d("Bearer", authorization)
            Log.d("token", "onCreate: ${sharedPreferences.getString(Constants.Authorization, " ")}")
//            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
            binding.navbar.rlLogout.visibility = View.VISIBLE
            binding.navbar.viewLogout.visibility = View.VISIBLE
            binding.navbar.rlLogout.setOnClickListener {
                closeDrawer()
                showLogout()
//           binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }


            binding.navbar.rlSavedEvents.setOnClickListener {
                setCurrentFragment(savedEventFragment, "SavedEventFragment")
                closeDrawer()
            }
            binding.navbar.rlBookingHistory.setOnClickListener {
                setCurrentFragment(bookingHistoryFragment, "BookingHistoryFragment")
                closeDrawer()
            }


            if (!sharedPreferences.getBoolean(Constants.Is_AgoraRegistered, false)) {
            signUp()

            callRegisterUserApis()
            } else {
//            getTokenFromAppServer(NEW_LOGIN)
            }

//            loginChatSdk()
            this.runOnUiThread {
                allChatListViewModel.toastMessage.observe(this@MainActivity)
                { observr ->
                    run {
                        if (!observr.isNullOrEmpty()) {
                            Toast.makeText(this, observr, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


            GlobalScope.launch {
                sharedPreferences?.getString(Constants.Authorization, null)?.let { token ->
//                    allChatListViewModel.callConnectionApi(token)
                }
            }

            this?.runOnUiThread {
                allChatListViewModel.isApiCalled.observe(this@MainActivity) {
                    if (it == true) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }

        } else {
            runOnUiThread {
                Toast.makeText(this, "User Not Logged in", Toast.LENGTH_SHORT).show()
                Log.e("Audience Id", audience_id)
                Log.e("Bearer", authorization)
                binding.navbar.rlLogout.visibility = View.INVISIBLE
                binding.navbar.viewLogout.visibility = View.INVISIBLE
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }

        }


        onBackPressedDispatcher.addCallback(
            this@MainActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    try {
                        if (supportFragmentManager.fragments.size > 1 && (supportFragmentManager?.findFragmentByTag(
                                "MarketPlaceFragment"
                            )?.isVisible == false)
                        ) {

                            val fragmentManager = supportFragmentManager
                            val backStackCount = fragmentManager.backStackEntryCount
                            if (fragmentManager.backStackEntryCount > 0)
                                fragmentManager.popBackStackImmediate()
                            else
                                onBackPressedDispatcher.onBackPressed()
//                        for (i in 0 until backStackCount) {
//                            val backStackEntry: FragmentManager.BackStackEntry =
//                                fragmentManager.getBackStackEntryAt(backStackCount)
//                            if (!backStackEntry.getName().equals("MarketPlaceFragment")) {
//                                fragmentManager.popBackStackImmediate(
//                                    backStackEntry.getId(),
//                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
//                                )
//                            }
//                        }
//                var fragmentToKeep =
//                    this.supportFragmentManager.findFragmentByTag("MarketPlaceFragment")!! as MarketPlaceFragment
//                val transaction: FragmentTransaction =
//                    this.supportFragmentManager.beginTransaction()
//
//                if (fragmentToKeep != null) {
//                    transaction.detach(fragmentToKeep)
//                } else {
//                    fragmentToKeep = MarketPlaceFragment()
//                }
//
//                for (fragment in supportFragmentManager.fragments) {
//                    if (fragment != null && fragment !== fragmentToKeep) {
//                        transaction.remove(fragment)
//                    }
//                }
//
//                transaction.addToBackStack(null)
//                transaction.commit()
                        } else {
                            val builder1 = AlertDialog.Builder(this@MainActivity)
                                .setMessage("Do you want to exit ?")
                                .setTitle("Alert !")
                                .setPositiveButton("Yes") { dialog, id ->
//                                super.onBackPressed()
                                    System.exit(0)
                                }
                                .setNegativeButton("No") { dialog, id ->
                                    dialog.cancel()
                                }


                            builder1.create().show()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        supportFragmentManager.beginTransaction()
                            .add(R.id.flFragment, MarketPlaceFragment(), "MarketPlaceFragment")
                            .commit()
                    }
                }

            })

//        this.supportFragmentManager.addOnBackStackChangedListener {
//            if (supportFragmentManager.findFragmentByTag("MarketPlaceFragment")?.isVisible == true) {
//
//            } else if (supportFragmentManager.findFragmentByTag("CreateFragment")?.isVisible == true) {
//
//            } else if (supportFragmentManager.findFragmentByTag("ConnectFragment")?.isVisible == true) {
//
//            } else if (supportFragmentMan ager.findFragmentByTag("WalletFragment")?.isVisible == true) {
//
//            } else {
//
//            }
//        }


    }

    private fun callRegisterUserApis() {

        val headers: MutableMap<String, String> =
            HashMap()
        headers["Content-Type"] = "application/json"
        val request = JSONObject()
        request.putOpt("userAccount", username)
        request.putOpt("userPassword", pwd)

        val response = HttpClientManager.httpExecute(
            REGISTER_USER_URL,
            headers,
            request.toString(),
            Method_POST
        )

        Log.d("Codes", "callRegisterUserApis: " + response)
    }


    private fun requestPermissions() {
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, 110)
    }



    private fun signUp() {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
//            LogUtils.showErrorToast(this, binding.tvLog, getString(R.string.username_or_pwd_miss))
            Log.d(Constants.TAG_CHAT, "signUp: getString(R.string.username_or_pwd_miss)")
            return
        }
        this.execute {
            try {
                val headers: MutableMap<String, String> =
                    HashMap()
                headers["Content-Type"] = "application/json"
                val request = JSONObject()
                request.putOpt("userAccount", username)
                request.putOpt("userPassword", pwd)
//                request.put("")
                Log.d(Constants.TAG_CHAT, "begin to signUp...")
//                LogUtils.showErrorLog(binding.tvLog, "begin to signUp...")
                val response = HttpClientManager.httpExecute(
                    REGISTER_URL,
                    headers,
                    request.toString(),
                    Method_POST
                )
                val code = response.code
                val responseInfo = response.content
                if (code == 200) {
                    sharedPreferences.edit().putBoolean(Constants.Is_AgoraRegistered, true).apply()
                    if (responseInfo != null && responseInfo.length > 0) {
                        val `object` = JSONObject(responseInfo)
                        val resultCode = `object`.getString("code")
                        if (resultCode == "RES_OK") {
                            Log.d(Constants.TAG_CHAT, getString(R.string.sign_up_success))
//                            LogUtils.showToast(
//                                this@MainActivity,
//                                binding.tvLog,
//                                getString(R.string.sign_up_success)
//                            )
                            try {
                                runOnUiThread {
                                    Toast.makeText(
                                        applicationContext,
                                        getString(R.string.sign_up_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
//                            callUpdateAgoraApi(username, pwd)
                        } else {
                            val errorInfo = `object`.getString("errorInfo")
                            Log.d(Constants.TAG_CHAT, errorInfo)
                            runOnUiThread {
                                Toast.makeText(applicationContext, errorInfo, Toast.LENGTH_SHORT)
                                    .show()
                            }
//                            LogUtils.showErrorLog(binding.tvLog, errorInfo)
                        }
                    } else {
                        Log.d(Constants.TAG_CHAT, responseInfo)
                        runOnUiThread {
                            Toast.makeText(applicationContext, responseInfo, Toast.LENGTH_SHORT)
                                .show()
                        }
//                        LogUtils.showErrorLog(binding.tvLog, responseInfo)
                    }
//                    getTokenFromAppServer(NEW_LOGIN)
                } else {
                    Log.d(Constants.TAG_CHAT, responseInfo)
//                    LogUtils.showErrorLog(binding.tvLog, responseInfo)
                    sharedPreferences.edit().putBoolean(Constants.Is_AgoraRegistered, false).apply()
                    if (code in 400..499) {
                        sharedPreferences.edit().putBoolean(Constants.Is_AgoraRegistered, true)
                            .apply()
//                        getTokenFromAppServer(NEW_LOGIN)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(Constants.TAG_CHAT, e.message.toString())
//                LogUtils.showErrorLog(binding.tvLog, e.message)
            }

        }
    }



    fun onUserException(exception: String) {
//        LogUtils.showLog(binding.tvLog, "onUserException: $exception")
        Log.d(Constants.TAG_CHAT, "onUserException: $exception")
        ChatClient.getInstance().logout(false, null)
    }

    fun execute(runnable: Runnable?) {
        Thread(runnable).start()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun checkPermissions(permission: String, requestCode: Int): Boolean {
        PermissionsManager.instance?.let {
            if (!it.hasPermission(this, permission)) {
                it.requestPermissions(this, arrayOf<String>(permission), requestCode)
                return false
            }
            return true
        }
        return false
    }

    private fun setCurrentFragment(fragment: Fragment, fragmetsTag: String) =
        supportFragmentManager.beginTransaction().apply {
            replace(binding.flFragment.id, fragment, fragmetsTag)
            addToBackStack(fragmetsTag)
            commit()
        }

    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawers()
        }
    }


    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestcode
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE)
//        {
//            if( grantResults.isNotEmpty() )
//                if(  grantResults[0].equals(PackageManager.PERMISSION_GRANTED) ||
//                    grantResults[1].equals(PackageManager.PERMISSION_GRANTED)   )
//                {
//                    GetLocation()
//                }
//        }
    }


    private fun getProfileApi() {
//        binding.progressDialog.visibility = View.VISIBLE
        try {
            ApiClient.getInstance()!!.getProfile(authorization, audience_id)
                .enqueue(object : retrofit2.Callback<AudienceDataResponse> {
                    override fun onResponse(
                        call: Call<AudienceDataResponse>,
                        response: Response<AudienceDataResponse>
                    ) {
//                binding.progressDialog.visibility = View.GONE
                        if (response.isSuccessful && response.body() != null) {
                            Log.d(
                                "Profile Api",
                                "onResponse Retrofit Profile Data: " + response.body()
                            )
                            val res = response.body()

                            var item: AudienceDataResponse = res!!

                            binding.navbar.tvCustName.text = res!!.fullName

                            binding.navbar.llNavDetails.setOnClickListener {
                                closeDrawer()
                                val intent =
                                    Intent(this@MainActivity, EditProfileActivity::class.java)
                                intent.putExtra("item", item as Serializable)
                                startActivity(intent)
                            }

                            if (!res.profile_pic.isNullOrEmpty())
                                Glide.with(this@MainActivity).load(res.profile_pic)
                                    .into(binding.navbar.ivProfileImage)
                        }
                    }

                    override fun onFailure(call: Call<AudienceDataResponse>, t: Throwable) {
//                binding.progressDialog.visibility = View.GONE
                        Log.d("Profile Api", "onFailure Retrofit: " + t.localizedMessage)


                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showLogout() {
        val builder1 = AlertDialog.Builder(this)
            .setMessage("Do you want to Logout from your account ?")
            .setTitle("Alert !")
            .setPositiveButton("Yes") { dialog, id ->
                sharedPreferences.edit().putBoolean(Constants.isUserTypeRegistered, false).apply()
                sharedPreferences.edit().putString(Constants.Authorization, "").apply()
                sharedPreferences.edit().putString(Constants.AudienceId, "").apply()
                binding.navbar.rlLogout.visibility = View.GONE
                dialog.cancel()
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
            }

        builder1.create().show()

    }


    override fun onResume() {
        super.onResume()
        exitCount = 0
        getProfileApi()
    }


    //    override fun onBackPressed() {
////        this.supportFragmentManager.findFragmentByTag("MarketPlaceFragment")?.id?.let {
////        supportFragmentManager.inTransaction {
////
////            popBackStack(
////                it, FragmentManager.POP_BACK_STACK_INCLUSIVE)
////
////            replace(R.id.container, newFragment, "NewFragment")
////        }
//        val builder1 = AlertDialog.Builder(this)
//            .setMessage("Do you want to exit ?")
//            .setTitle("Alert !")
//            .setPositiveButton("Yes") { dialog, id ->
//                super.onBackPressed()
//                System.exit(0)
//            }
//            .setNegativeButton("No") { dialog, id ->
//                dialog.cancel()
//            }
//
//        builder1.create().show()
//    }
    override fun onBackPressed() {
//        this.supportFragmentManager.findFragmentByTag("MarketPlaceFragment")?.id?.let {
//        supportFragmentManager.inTransaction {
//
//            popBackStack(
//                it, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//
//            replace(R.id.container, newFragment, "NewFragment")
//        }

        try {
            if (this.supportFragmentManager.fragments.size > 1 && (this.supportFragmentManager?.findFragmentByTag(
                    "MarketPlaceFragment"
                )?.isVisible == false)
            ) {

                val fragmentManager = this.supportFragmentManager
                val backStackCount = fragmentManager.backStackEntryCount
                for (i in 0 until backStackCount) {
                    val backStackEntry: FragmentManager.BackStackEntry =
                        fragmentManager.getBackStackEntryAt(i)
                    if (!backStackEntry.getName().equals("MarketPlaceFragment")) {
                        fragmentManager.popBackStackImmediate(
                            backStackEntry.getId(),
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }
                }
//                var fragmentToKeep =
//                    this.supportFragmentManager.findFragmentByTag("MarketPlaceFragment")!! as MarketPlaceFragment
//                val transaction: FragmentTransaction =
//                    this.supportFragmentManager.beginTransaction()
//
//                if (fragmentToKeep != null) {
//                    transaction.detach(fragmentToKeep)
//                } else {
//                    fragmentToKeep = MarketPlaceFragment()
//                }
//
//                for (fragment in supportFragmentManager.fragments) {
//                    if (fragment != null && fragment !== fragmentToKeep) {
//                        transaction.remove(fragment)
//                    }
//                }
//
//                transaction.addToBackStack(null)
//                transaction.commit()
            } else {
                val builder1 = AlertDialog.Builder(this)
                    .setMessage("Do you want to exit ?")
                    .setTitle("Alert !")
                    .setPositiveButton("Yes") { dialog, id ->
                        super.onBackPressed()
                        System.exit(0)
                    }
                    .setNegativeButton("No") { dialog, id ->
                        dialog.cancel()
                    }


                builder1.create().show()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.supportFragmentManager.beginTransaction()
                .add(R.id.flFragment, MarketPlaceFragment(), "MarketPlaceFragment").commit()
        }
    }


}


