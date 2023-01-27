package com.example.tracker

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_drawer_header.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.math.sqrt


class HomeFragment : Fragment() , SensorEventListener, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var v: View
    private lateinit var calculate : Button
    private lateinit var  bmiResult : TextView
    private lateinit var height : EditText
    private lateinit var weight : EditText
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var uid : String
    private lateinit var add : FloatingActionButton
    private lateinit var sub : FloatingActionButton
    private lateinit var glasses : TextView
    private lateinit var setCal : Button
    private lateinit var setW : Button
    private lateinit var CaloriesVal : EditText
    private lateinit var setWater: Button
    private lateinit var weightVal : EditText
    private var sensorManager : SensorManager?= null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var stepcount : Int = 0
    private var magni : Double = 0.0
    private lateinit var goalVal : EditText
    private lateinit var setGoal : Button
    val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    private var indice : Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        auth = FirebaseAuth.getInstance()
        val date = SimpleDateFormat("dd - MM - yyyy").format(System.currentTimeMillis())
        //test sur l'existence de "built in sensor"
        val pm = context?.packageManager
        println(pm?.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER))
        println(pm?.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR))

        val sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        height=v.findViewById(R.id.BMIHeight)
        weight=v.findViewById(R.id.BMIWeight)
        bmiResult=v.findViewById(R.id.BMIResult)
        calculate=v.findViewById(R.id.BMI)
        auth= FirebaseAuth.getInstance()
        uid=auth.currentUser?.uid.toString()
        databaseRef=FirebaseDatabase.getInstance("https://fitnesstracker-328c0-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        storage= FirebaseStorage.getInstance()

        goalVal = v.findViewById(R.id.GoalVal)
        setGoal = v.findViewById(R.id.setGoal)


        setGoal.setOnClickListener {
            databaseRef.child(uid).child("stepgoal").setValue(goalVal.text.toString())
            circularProgressBar.progressMax=goalVal.text.toString().toFloat()
        }

        databaseRef.child(uid).get().addOnSuccessListener {
            if (it.exists())
                if (it.child("stepgoal").value!=null)
                    circularProgressBar.progressMax=it.child("stepgoal").value.toString().toFloat()
        }

        auth.getAccessToken(false).addOnSuccessListener {
            if (it != null ){
                val loginMethod = it.signInProvider
                if (loginMethod=="google.com"){
                    //trying Google Fit step-counter
                    indice = true
                    Fitness.getRecordingClient(
                        context,
                        GoogleSignIn.getAccountForExtension(context, fitnessOptions)
                    )
                        // This example shows subscribing to a DataType, across all possible data
                        // sources. Alternatively, a specific DataSource can be used.
                        .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                        .addOnSuccessListener {
                            Toast.makeText(context, "You're now using Fit Api.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                        }

                    val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())

                    val datasource = DataSource.Builder()
                        .setAppPackageName("com.google.android.gms")
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setType(DataSource.TYPE_DERIVED)
                        .setStreamName("estimated_steps")
                        .build()

                    val request = DataReadRequest.Builder()
                        .aggregate(datasource)
                        .bucketByTime(1, java.util.concurrent.TimeUnit.DAYS)
                        .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(),
                            java.util.concurrent.TimeUnit.SECONDS
                        )
                        .build()

                    Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                        .readData(request)
                        .addOnSuccessListener { response ->
                            val totalSteps2 = response.buckets
                                .flatMap { it.dataSets }
                                .flatMap { it.dataPoints }
                                .sumBy { it.getValue(Field.FIELD_STEPS).asInt() }
                            Log.i(TAG, "Total steps: $totalSteps2")
                        }


                    Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                        .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                        .addOnSuccessListener { result ->
                            val totalSteps =
                                result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                            circularProgressBar.apply {
                                setProgressWithAnimation((totalSteps/2).toInt().toFloat())}
                            steps.text="${(totalSteps/2).toInt()} steps"
                            databaseRef.child(uid).child(date).child("steps").setValue((totalSteps/2).toInt().toString())
                            println("$totalSteps steps")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to get steps", Toast.LENGTH_SHORT).show()
                        }
                    //end of Google Fit trial
                }
            }
        }

        calculate.setOnClickListener {
            val hVal=height.text.toString()
            val wVal=weight.text.toString()
            var h= hVal.toDouble()
            if (h>2)
            {
                h /= 100
                println(h)
            }
            var aux= wVal.toDouble()/(h*h)
            val decFormat = DecimalFormat("#.00")
            val resultCal = decFormat.format(aux)
            bmiResult.text = "Your BMI is $resultCal"
            if (15<aux && aux<19)
                Indication.text="Underweight."
            else if (19<aux && aux<25)
                Indication.text="Ideal."
            else if (25<aux && aux<30)
                Indication.text="Overweight."
            else if (50>aux && aux>30)
                Indication.text="Obese."
            else
                Indication.text="Make sure the numbers are correct."
        }

        CaloriesVal = v.findViewById(R.id.Calories)
        weightVal = v.findViewById(R.id.WeightVal)
        setCal = v.findViewById(R.id.SetCalories)
        setW = v.findViewById(R.id.SetWeight)

        setW.setOnClickListener {
            val editweight = weightVal.text.toString()
            databaseRef.child(uid).child("weight").setValue(editweight)
            databaseRef.child(uid).child(date).child("weight").setValue(editweight)
        }
        setCal.setOnClickListener {
            val editCal = CaloriesVal.text.toString()
            databaseRef.child(uid).child(date).child("calories").setValue(editCal)
        }

        databaseRef.child(uid).child(date).get().addOnSuccessListener {
            if (it.exists()) {
                val weightdb = it.child("weight").value
                val caldb = it.child("calories").value
                if (weightdb!=null)
                    weightVal.setText(weightdb.toString())
                if (caldb!=null)
                    CaloriesVal.setText(caldb.toString())
            }
        }

        add = v.findViewById(R.id.plus)
        sub = v.findViewById(R.id.minus)
        setWater = v.findViewById(R.id.SetWater)
        glasses = v.findViewById(R.id.WaterGlasses)
        var number=0

        databaseRef.child(uid).child(date).get().addOnSuccessListener {
            if (it.exists()) {
                var num = it.child("water").value
                if (num!=null){
                    glasses.text = "${num.toString().toInt()} glass(es)"
                    number = num.toString().toInt()
                }/*else {
                    num = 0
                }*/
            }
        }

        add.setOnClickListener {
            number += 1
            glasses.text = "$number glass(es)"
        }
        sub.setOnClickListener {
            if (number > 0)
                number -= 1
            glasses.text = "$number glass(es)"
        }

        setWater.setOnClickListener {
            databaseRef.child(uid).child(date).child("water").setValue(number.toString())
        }

        databaseRef.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                if (it.child("stepgoal").value!=null)
                    GoalVal.setText(it.child("stepgoal").value.toString())
                steps.text=it.child(date).child("steps").value.toString()
            }
        }


        databaseRef.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val bdate = it.child("birthdate").value.toString()
                if(bdate!="null"){
                    val day = bdate.subSequence(0,2).toString().toInt()
                    println("day $day")
                    val month = bdate.subSequence(5,7).toString().toInt()
                    println("Month $month")
                    val year = bdate.subSequence(10,14).toString().toInt()
                    println("year $year")
                    println("The age is ${getAge(year,month,day)}")
                    databaseRef.child(uid).child("age").setValue(getAge(year,month,day).toString())
                }
            }
        }

        //val radioGroupSex : RadioGroup = v.findViewById(R.id.Sex)
        val radioGroupact : RadioGroup = v.findViewById(R.id.Activity)
        val calBMR : Button = v.findViewById(R.id.CalBMR)
        //val age : EditText = v.findViewById(R.id.Age)
        var BMRVal = 0.0
        var amr = 0.0
        val setBMR_AMR :TextView = v.findViewById(R.id.BMR)

        calBMR.setOnClickListener {
            //val selectedSex = radioGroupSex!!.checkedRadioButtonId
            val selectedActivity = radioGroupact!!.checkedRadioButtonId
            //val sexButton: RadioButton = v.findViewById(selectedSex)
            val activityButton: RadioButton = v.findViewById(selectedActivity)
            //databaseRef.child(uid).child("gender").setValue(sexButton.text.toString())
            databaseRef.child(uid).child("activityLevel").setValue(activityButton.text.toString())
            //databaseRef.child(uid).child("age").setValue(age.text.toString())
            databaseRef.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val dbweight = it.child("weight").value.toString()
                    val dbheight = it.child("height").value.toString()
                    val dbage = it.child("age").value.toString()
                    val dbsex = it.child("sex").value.toString()

                    if (dbsex == "Female")
                        BMRVal =
                            655.1 + (9.563 * dbweight.toDouble()) + (1.85 * dbheight.toDouble()) - (4.676 * dbage.toDouble())
                    else if (dbsex == "Male")
                        BMRVal =
                            66.47 + (13.75 * dbweight.toDouble()) + (5.003 * dbheight.toDouble()) - (6.755 * dbage.toDouble())
                    else
                        setBMR_AMR.text = "Please fill in your profile info first."
                    if (activityButton.text.toString() == "Sedentary")
                        amr = BMRVal * 1.2
                    else if (activityButton.text.toString() == "Active")
                        amr = BMRVal * 1.55
                    else
                        amr = BMRVal * 1.9

                    setBMR_AMR.text =
                        "Your BMR = ${BMRVal.roundToInt()} and you need around ${amr.roundToInt()} calories a day."
                }
            }
        }
        databaseRef.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                if (it.child(date)
                        .child("steps").value != null && it.child("stepgoal").value != null
                ) {
                    if (it.child(date).child("steps").value.toString()
                            .toInt() >= it.child("stepgoal").value.toString().toInt()
                    ) {
                        val intent = Intent(context, Congrats::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        val swipeLayout : (SwipeRefreshLayout) = v.findViewById(R.id.swipeRefresh)
        swipeLayout.setOnRefreshListener(this)
        swipeLayout.setColorScheme(android.R.color.holo_blue_dark,
            android.R.color.holo_purple,
            android.R.color.holo_orange_light,
            android.R.color.holo_green_dark)
        swipeLayout.setProgressViewOffset(false, 0, 130)

        return v
    }

    override fun onResume() {
        super.onResume()
        val date = SimpleDateFormat("dd - MM - yyyy").format(System.currentTimeMillis())
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        running=true
        val pm = context?.packageManager
        if (!indice) {
            if (pm?.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) == true) {
                val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                if (stepSensor != null) {
                    sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
                    stepcount = sharedPreferences.getInt(uid, 0)
                }
            } else {
                val sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                /*var aux=0
            databaseRef.child(uid).child(date).get().addOnSuccessListener {
                if (it.exists())
                    aux=it.child("steps").value.toString().toInt()
            }
            if (sharedPreferences.getInt(uid+date,0)<aux)
                stepcount=aux
            else*/
                stepcount = sharedPreferences.getInt(uid + date, 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //sensorManager?.unregisterListener(this)
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        val date = SimpleDateFormat("dd - MM - yyyy").format(System.currentTimeMillis())
        editor.clear()
        editor.putInt(uid+date,stepcount)
        println("${sharedPreferences.getInt(uid,0)} stepcount")
        editor.apply()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val pm = context?.packageManager
        val date = SimpleDateFormat("dd - MM - yyyy").format(System.currentTimeMillis())
        if (!indice) {
            if (pm?.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) == true) {
                if (running) {
                    totalSteps = event!!.values[0]
                    val currentSteps: Int = totalSteps.toInt() - previousTotalSteps.toInt()
                    //steps.text = "$currentSteps Steps"
                    println("my steps $currentSteps")
                    circularProgressBar.apply {
                        setProgressWithAnimation(currentSteps.toFloat())
                    }
                    if (totalSteps != null && steps != null) {
                        databaseRef.child(uid).child(date).child("steps")
                            .setValue(stepcount.toString())
                            steps.text = "$totalSteps Steps"
                    }
                }
            } else {
                if (event != null) {
                    val x_accelerometer = event.values[0]
                    val y_accelerometer = event.values[1]
                    val z_accelerometer = event.values[2]

                    databaseRef.child(uid).child(date).get().addOnSuccessListener {
                        if (it.exists())
                            stepcount = it.child("steps").value.toString().toInt()
                    }

                    val magnitute: Double =
                        sqrt(x_accelerometer * x_accelerometer + y_accelerometer * y_accelerometer + z_accelerometer * z_accelerometer).toDouble()
                    val magniDelta = magnitute - magni
                    magni = magnitute
                    if (magniDelta > 6) {
                        stepcount++
                    }
                    if (stepcount != null && steps != null) {
                        databaseRef.child(uid).child(date).child("steps")
                            .setValue(stepcount.toString())
                        circularProgressBar.apply {
                            setProgressWithAnimation(stepcount.toFloat())
                            steps.text = "$stepcount Steps"
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("No implementation needed.")
    }

    fun getAge(year: Int, month: Int, dayOfMonth: Int): Int {
        return Period.between(
            LocalDate.of(year, month, dayOfMonth),
            LocalDate.now()
        ).years
    }

    override fun onRefresh() {
        val swipeLayout : (SwipeRefreshLayout) = v.findViewById(R.id.swipeRefresh)
        Handler().postDelayed(Runnable { swipeLayout.isRefreshing = false }, 5000)
    }
}
