package com.udacity.project4.authentication

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.base.BaseViewModel

class AuthenticationViewModel : ViewModel() {

    private var _showReminderActivity = MutableLiveData<Boolean>()
    val showReminderActivity : LiveData<Boolean>
        get() = _showReminderActivity

    var auth = FirebaseAuth.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if(firebaseAuth.currentUser != null) {
            _showReminderActivity.value = true
        }
    }
    // When this object has an active observer, start observing the FirebaseAuth state to see if
    // there is currently a logged in user.
    fun onActive() {
        auth.addAuthStateListener(authStateListener)
    }

    // When this object no longer has an active observer, stop observing the FirebaseAuth state to
    // prevent memory leaks.
    fun onInactive() {
        auth.removeAuthStateListener(authStateListener)
    }
}