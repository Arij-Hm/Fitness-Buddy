package com.example.tracker

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_drawer_header.*

class Drawer_Header : AppCompatActivity() {
    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var uid : String
    //private lateinit var imageurl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_header)
        auth= FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        databaseReference= FirebaseDatabase.getInstance("https://fitnesstracker-328c0-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        uid = auth.currentUser?.uid.toString()

        Blurry.with(this)
            .radius(10)
            .sampling(8)
            .color(Color.argb(66, 255, 255, 0))
            .async()
            .animate(500)
            .onto(findViewById(R.id.nav_header));

        /*var test=false
        databaseReference.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val imageurl = it.child("url").value
                if(imageurl!=null && imageurl!=""){
                    val gsReference = storage.getReferenceFromUrl(imageurl!!.toString())
                    Glide.with(this)
                        .load(gsReference)
                        .into(headerpic)
                    test=true
                }
            }else{
                Toast.makeText(this@Drawer_Header, "Failed to get data.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@Drawer_Header, "User not found.", Toast.LENGTH_SHORT).show()
        }
        if(test) {
            recreate()
        }*/
    }
}