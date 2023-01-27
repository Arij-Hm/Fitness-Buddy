package com.example.tracker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_drawer_header.*
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var storage : FirebaseStorage
    private lateinit var databaseReference : DatabaseReference
    private lateinit var uid : String
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = System.identityHashCode(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(topAppBar)

        auth = FirebaseAuth.getInstance()
        auth.getAccessToken(false).addOnSuccessListener {
            if (it != null) {
                val loginMethod = it.signInProvider
                if (loginMethod == "google.com") {
                    //val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = System.identityHashCode(this)
                    val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

                    if (!GoogleSignIn.hasPermissions(account, fitnessOptions))
                        GoogleSignIn.requestPermissions(
                            this,
                            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account,
                            fitnessOptions
                        ) else {
                        accessGoogleFit()
                    }
                }
            }
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_View)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, HomeFragment())
        fragmentTransaction.commit()
        setTitle("Home")

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_account -> {
                    replaceFragment(ProfileFragment(), "My Profile")
                }
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), "Home")
                }
                R.id.nav_connect -> {
                    replaceFragment(Connect(), "Connect To Devices")
                }
            }
            true
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signoutBtn = findViewById<View>(R.id.SignOut)
        signoutBtn.setOnClickListener {
            logout()
        }

        storage = FirebaseStorage.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://fitnesstracker-328c0-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users")
        uid = auth.currentUser?.uid.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> accessGoogleFit()
                else -> {
                    Toast.makeText(this@MainActivity, "Data is not from Google.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this@MainActivity, "Permission is not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun accessGoogleFit() {
        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener {

                Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MainActivity,
                    " Failure",
                    Toast.LENGTH_SHORT
                ).show()
            }
        Fitness.getSensorsClient(this, account).findDataSources(DataSourcesRequest.Builder().setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
            .setDataSourceTypes(DataSource.TYPE_RAW)
            .build()).addOnSuccessListener { dataSources ->
            dataSources.forEach {
                Toast.makeText(this@MainActivity, "Data source found: ${it.streamIdentifier}", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@MainActivity, "Data Source type: ${it.dataType.name}", Toast.LENGTH_SHORT).show()

                if (it.dataType == DataType.TYPE_STEP_COUNT_DELTA) {
                    Toast.makeText(this@MainActivity, "Data source for STEP_COUNT_DELTA found!", Toast.LENGTH_SHORT).show()
                }
            }
        }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, "Failure!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun replaceFragment(fragment: Fragment, title:String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
        setTitle(title)
        drawerLayout.closeDrawers()
    }

   private fun logout(){
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this
        ) { Toast.makeText(this@MainActivity, "Signed Out", Toast.LENGTH_LONG).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        }
       Firebase.auth.signOut()
   }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
       if (toggle.onOptionsItemSelected(item))
           return true

       return super.onOptionsItemSelected(item)
   }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}