package com.example.tracker

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {
    //private lateinit var binding : ActivityProfileBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        /*binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)*/

            /*if (it.isSuccessful){

                uploadPic()
                Toast.makeText(this@Profile, "Updated profile successfully.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@Profile, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }*/
            //}

        }
}
