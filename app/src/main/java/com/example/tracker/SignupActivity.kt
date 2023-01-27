package com.example.tracker

//import java.util.Observable
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.signup.*

//import android.util.Patterns

class SignupActivity : AppCompatActivity() {
    private lateinit var  auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        Login2.setOnClickListener{
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        val registerBtn = findViewById<View>(R.id.SignUp)
        registerBtn.setOnClickListener(View.OnClickListener {
        registerUser()
        })
    }
        private fun registerUser () {
            val emailTxt = findViewById<View>(R.id.SignupEmail) as EditText
            val passwordTxt = findViewById<View>(R.id.SignupPassword) as EditText
            val nameTxt = findViewById<View>(R.id.Name) as EditText

            var email = emailTxt.text.toString()
            var password = passwordTxt.text.toString()
            var name = nameTxt.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user!!.uid
                        //mDatabase.child(uid).child("Name").setValue(name)
                        startActivity(Intent(this, MainActivity::class.java))
                        Toast.makeText(this, "Successfully registered.", Toast.LENGTH_LONG).show()
                    }else {
                        Toast.makeText(this, "Error registering, try again later.", Toast.LENGTH_LONG).show()
                    }
                })
            }else {
                Toast.makeText(this,"Please fill up the Credentials.", Toast.LENGTH_LONG).show()
            }
    }
}