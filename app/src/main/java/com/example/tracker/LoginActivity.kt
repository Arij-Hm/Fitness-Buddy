package com.example.tracker

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
//import com.example.tracker.LoginActivity.Companion.getLaunchIntent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.signup.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


        //To access the sign up page
        val vbutton: Button = findViewById<Button>(R.id.button2)
        vbutton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        //For Email&Password authentication
        auth = FirebaseAuth.getInstance()
        val loginBtn = findViewById<View>(R.id.button)
        loginBtn.setOnClickListener(View.OnClickListener {
            login()
        })

        //For Google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        GmailButton.setOnClickListener {
            signIn()
        }

        FacebookButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, object :
                FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    if (loginResult != null) {
                        handleFacebookAccessToken(loginResult.accessToken)
                    }
                    // Get User's Info
                }

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "Login Cancelled", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: FacebookException) {
                    Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            })
        }

    }

    private fun login() {
        val emailTxt = findViewById<View>(R.id.LoginEmail) as EditText
        var email = emailTxt.text.toString()
        val passwordTxt = findViewById<View>(R.id.LoginPassword) as EditText
        var password = passwordTxt.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            this.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "Successfully Logged in.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error Logging in.", Toast.LENGTH_SHORT).show()
                }
            })

        } else {
            Toast.makeText(this, "Please fill up the Credentials.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun signIn() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_LONG).show()
            }
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    /*companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }*/

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if(account!=null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val intentF= Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intentF)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

}

