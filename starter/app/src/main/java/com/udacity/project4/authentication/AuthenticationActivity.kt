package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 123

    private lateinit var mBinding : ActivityAuthenticationBinding
    private val authenticationViewModel by viewModels<AuthenticationViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        mBinding.loginBt.setOnClickListener {
                userLogin()
        }

        authenticationViewModel.showReminderActivity.observe(this, Observer { show ->
            run {
                if (show) {
                    showReminderActivity()
                }
            }
        })
    }

    private fun registerUser() {
        startActivityForResult(
            // Get an instance of AuthUI based on the default app
            AuthUI.getInstance().createSignInIntentBuilder().build(),
            RC_SIGN_IN
        )
    }

    private fun userLogin() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                showReminderActivity()

            } else {
                Log.i("onActivityResult", "Failed - error ${response?.error}")
                Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show()
                registerUser()
            }
        }

    }

    private fun showReminderActivity() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }


    override fun onResume() {
        super.onResume()
        authenticationViewModel.onActive()
    }

    override fun onDestroy() {
        super.onDestroy()
        authenticationViewModel.onInactive()
    }


}
