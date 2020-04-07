package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        auth = FirebaseAuth.getInstance()
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

        //          TODO: If the user was authenticated, send him to RemindersActivity Done
        if (auth.currentUser != null) {

            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        } else {

            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.GoogleBuilder().build(),
                            AuthUI.IdpConfig.EmailBuilder().build()
                        )
                    )
                    .build(),
                RC_SIGN_IN
            )
        }


//          TODO: a bonus is to customize the sign in flow to look nice using Done :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val idPResponse = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK){
                startActivity(Intent(this,RemindersActivity::class.java))
                return
            } else {

                if (idPResponse == null){
                    showSnackBar("Sign in cancelled")
                    return
                }

                if (idPResponse.error!!.errorCode == ErrorCodes.NO_NETWORK){
                    showSnackBar("No network")
                    return
                } else {
                    showSnackBar("Sign in error")
                }



            }
        }
    }


    private fun showSnackBar(message:String) {
        Snackbar.make(root,message,Snackbar.LENGTH_LONG).show()

    }
}
