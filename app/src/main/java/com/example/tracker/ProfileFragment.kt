package com.example.tracker

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_drawer_header.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.toString as toString1

class ProfileFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var  editName: EditText
    private lateinit var  editHeight: EditText
    private lateinit var  editWeight: EditText
    private lateinit var  editDate: EditText
    private lateinit var  uid : String
    private lateinit var v: View
    private lateinit var  name : String
    private lateinit var  height : String
    private lateinit var  weight : String
    private lateinit var  birthdate : String
    private lateinit var url : String
    private lateinit var image : ImageView
    private lateinit var save : Button
    private lateinit var radioGroupSex : RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v=inflater.inflate(R.layout.fragment_profile, container, false)
        auth= FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString1()
        editName=v.findViewById(R.id.NameText)
        editHeight=v.findViewById(R.id.HeightText)
        editWeight=v.findViewById(R.id.WeightText)
        editDate=v.findViewById(R.id.BirthText)
        val context: Context? = container?.context
        var test = false
        //For the DatePicker
        val textView: TextView = v.findViewById(R.id.BirthText)
        textView.text = SimpleDateFormat("dd - MM - yyyy").format(System.currentTimeMillis())
        
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd - MM - yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            textView.text = sdf.format(cal.time)

        }
        image = v.findViewById(R.id.profile_pic)
        editDate.setOnClickListener {
            if (context != null) {
                DatePickerDialog(
                    context, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        image.setOnClickListener {
            selectPic()
            test=true
        }

        save=v.findViewById(R.id.Save)
        radioGroupSex= v.findViewById(R.id.Sex)
        //storing info in the database
        //databaseReference= FirebaseDatabase.getInstance().getReference("Users")
        databaseReference= FirebaseDatabase.getInstance("https://fitnesstracker-328c0-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        save.setOnClickListener {
            name = editName.text.toString()
            height = editHeight.text.toString()
            weight = editWeight.text.toString()
            birthdate = editDate.text.toString()
            val selectedSex = radioGroupSex!!.checkedRadioButtonId
            val sexButton: RadioButton = v.findViewById(selectedSex)
            //val user= User(name, height, weight, birthdate, "")
            if (uid != null) {
                //databaseReference.child(uid)setValue(user)
                    //.addOnSuccessListener {
                databaseReference.child(uid).child("name").setValue(name)
                databaseReference.child(uid).child("height").setValue(height)
                databaseReference.child(uid).child("weight").setValue(weight)
                databaseReference.child(uid).child("birthdate").setValue(birthdate)
                databaseReference.child(uid).child("sex").setValue(sexButton.text.toString())
                if (test)
                    uploadPic()
                Toast.makeText(context, "Updated profile successfully.", Toast.LENGTH_SHORT).show()
                    //}.addOnFailureListener {
                        //Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                    //}
            }
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
        setData(uid)

        return v
    }

    private fun setData(userid:String){
        databaseReference.child(userid).get().addOnSuccessListener {
            if (it.exists()){
                println("Snapshot exists")
                val Name = it.child("name").value
                val Height = it.child("height").value
                val Weight = it.child("weight").value
                val Date = it.child("birthdate").value
                val imageurl= it.child("url").value
                val sex = it.child("sex").value
                val storage = FirebaseStorage.getInstance()

                editName.setText(Name?.toString())
                editHeight.setText(Height?.toString())
                editWeight.setText(Weight?.toString())
                editDate.setText(Date?.toString())

                if (sex == "Female")
                    radioGroupSex.check(R.id.Female)
                else
                    radioGroupSex.check(R.id.Male)

                if(imageurl!="" && imageurl!=null ){
                val gsReference = storage.getReferenceFromUrl(imageurl!!.toString())
                    println("The gsReference $gsReference")
                Glide.with(this)
                    .load(gsReference)
                    .into(profile_pic)
                }
                Toast.makeText(context, "Succeeded to download data.", Toast.LENGTH_SHORT).show()
            }else{
                println("snapshot doesn't exist")
                Toast.makeText(context, "No data.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPic(){

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now= Date()
        val fileName= formatter.format(now)
        storageReference= FirebaseStorage.getInstance().getReference("images/$fileName")

        if (imageUri!=null) {
            println("URI $imageUri")
            storageReference.putFile(imageUri).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener {
                    url=it.toString()
                    databaseReference.child(uid).child("url").setValue(url)
                }
                image.setImageURI(null)
                Toast.makeText(context, "Successfully uploaded.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Upload failed .", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun selectPic(){
        val intent= Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent, 100)
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data!=null)
        {
            imageUri= data.data!!
            image.setImageURI(imageUri)
        }
    }
}