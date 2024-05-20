package com.example.myapplicationprojekat.ui.home

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.db.williamchart.view.BarChartView
import com.example.myapplicationprojekat.R
import com.example.myapplicationprojekat.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Collections.max
import java.util.Collections.min
import java.util.Date
import kotlin.math.max
import kotlin.properties.Delegates

class HomeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeBinding? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var sensorMenager: SensorManager? = null

    private var running = false
    private var totalSteps = 0

    private var dataSteps: String = ""
    private var dataGoal: String = ""
    private var dataStreak: String = ""
    private var dataPB: String = ""

    private lateinit var txtDaily: TextView
    private lateinit var txtGoal: TextView
    private lateinit var pbDailyProgress: ProgressBar
    private lateinit var txtProgress: TextView
    private lateinit var txtStreak: TextView
    private lateinit var txtPB: TextView
    private lateinit var barChart: BarChartView
    private lateinit var txtMin: TextView
    private lateinit var txtMax: TextView
    private lateinit var txtAvg: TextView


    private val user = mAuth.currentUser
    private val uid = user!!.uid




    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val animationDuration = 1000L
    private var barSet = listOf(
        "MON" to 0F,
        "TUE" to 0F,
        "WED" to 0F,
        "THU" to 0F,
        "FRI" to 0F,
        "SAT" to 0F,
        "SUN" to 0F
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.apply {
            barChart.animation.duration = animationDuration
            barChart.run { animate(barSet) }
        }
        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //sensor for steps
        sensorMenager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

        //fetching layout elements
        txtDaily = view.findViewById(R.id.txtDaily)
        txtGoal = view.findViewById(R.id.txtGoal)
        pbDailyProgress = view.findViewById(R.id.pbDailySteps)
        txtProgress = view.findViewById(R.id.txtProgress)
        txtStreak = view.findViewById(R.id.txtStreak)
        txtPB = view.findViewById(R.id.txtPB)
        barChart = view.findViewById(R.id.barChart)
        txtMin = view.findViewById(R.id.min_num)
        txtMax = view.findViewById(R.id.max_num)
        txtAvg = view.findViewById(R.id.avg_num)

        readFromDB(true)

        //resetting data
        resetData()

    }

    private fun progress(dataSteps: String, dataGoal: String): Int {
        return (dataSteps.toInt() * 100 / dataGoal.toInt())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor: Sensor? = sensorMenager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor == null) {
            Toast.makeText(activity, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorMenager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
//            Toast.makeText(activity, "OVDe", Toast.LENGTH_SHORT).show()

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0].toInt()


            writeToDB(totalSteps)
            readFromDB(false)

            //resetting data at midnight
//            resetData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resetData() {

        // Check if the fragment is attached to an activity
        if (!isAdded) {
            return
        }

        //using SharedPreferences to store time locally on device
        val sharedPref = requireActivity().getSharedPreferences("mySharedPref", Activity.MODE_PRIVATE)
        val editorProgress = sharedPref.edit()

        //getting date, day of the week
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        val current = formatter.format(date)
        val day = LocalDate.now().dayOfWeek
        val dayNum = day.value  //  1-MONDAY, ..., 7-SUNDAY
        Log.d(TAG, current)
        Log.d(TAG, day.toString())
        Log.d(TAG, dayNum.toString())
        val today = sharedPref.getString("dateToday", null)
//        val weekDay = sharedPref.getString("dayOfWeek", null)
        Log.d(TAG, today.toString())
//        Log.d(TAG, weekDay.toString())



        //updating daily steps, pb, streak, barchart day, everyday at midnight
        if(today == null){
            editorProgress.apply{
                putString("dateToday", current)
//                Log.d(TAG, "ovde")
                apply()
            }
        } else if(current != today){
            Log.d(TAG,"NOVI DAN")
            editorProgress.apply {
                putString("dateToday", current)
                apply()
            }
            editorProgress.apply {

                readFromDB(false)

                var newStreak: Int = 0
                // Parse dataPB, dataStreak, dataSteps and dataGoal only if they are not empty
                var newPb: Int = if (dataPB.isNotEmpty()) dataPB.toInt() else 0
                val streak: Int = if (dataStreak.isNotEmpty()) dataStreak.toInt() else 0
                val steps: Int = if (dataSteps.isNotEmpty()) dataSteps.toInt() else 0
                val goal: Int = if (dataGoal.isNotEmpty()) dataGoal.toInt() else 0

                Log.d(TAG, "NEW PB: $newPb")
                Log.d(TAG, "STREAK: $streak")
                Log.d(TAG, "STEPS: $steps")
                Log.d(TAG, "GOAL: $goal")

                Log.d(TAG, steps.toString())
                if(steps > goal){
                    newStreak = streak + 1
                }
                if(steps > newPb){
                    newPb = steps
                }
                Log.d(TAG, "NEW PB: $newPb")
                Log.d(TAG, "NEW STREAK: $newStreak")


//                if(dataSteps.toInt() > dataGoal.toInt()){
//                    newStreak = dataStreak.toInt() + 1
//                }
//                if(dataSteps.toInt() > dataPB.toInt()){
//                    newPb = dataSteps.toInt()
//                }

//                //updating data for day of the week, except transition sunday->monday
//                if(dayNum != 1){
//                    var week: ArrayList<Float>
//                    db.collection("users").document(uid).get().addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val document = task.result
//                            if (document.exists()) {
//                                week = document.get("week_steps") as ArrayList<Float>
//                                if(week.isNotEmpty()){
//                                    Log.d(TAG, steps.toString())
//                                    week[dayNum - 2] = steps.toFloat()
//
//                                    db.collection("users").document(uid)
//                                        .update("week_steps", week)
//                                        .addOnSuccessListener {
//                                            Log.d(TAG, "DocumentSnapshot successfully updated!")
//                                        }
//                                        .addOnFailureListener { e ->
//                                            Log.w(TAG, "Error updating document", e)
//                                        }
//                                }
//                                Log.d(TAG, week.toString())
//                            } else {
//                                Log.d(TAG, "The document does not exits :(")
//                            }
//                        }
//                    }
//                }

                //readFromDB(false)

                //updating daily steps, pb, streak
                db.collection("users").document(uid)
                    .update(mapOf(
                        "todays_steps" to 0,
                        "pb" to newPb,
                        "streak" to newStreak
                    ))
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error updating document", e)
                    }

                readFromDB(false)
                apply()

            }

            //weekly resetting
            if(day.toString() == "MONDAY"){
                Log.d(TAG,"Nova nedelja")

                editorProgress.apply {
                    putString("dayOfWeek", day.toString())
                    apply()
                }
                editorProgress.apply {
                    val week_steps: ArrayList<Int> = ArrayList<Int>(7).apply {
                        for (i in 0 until 7) {
                            add(0)
                        }
                    }
                    db.collection("users").document(uid)
                        .update("week_steps", week_steps)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }
                    readFromDB(false)
                    apply()

                }
            }
        }

    }

    private fun readFromDB(animtionFlag: Boolean) {
        var week: ArrayList<Float>
        db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    dataSteps = document.get("todays_steps").toString()
                    dataGoal = document.get("goal_steps").toString()
                    dataStreak = document.get("streak").toString()
                    dataPB = document.get("pb").toString()
                    week = document.get("week_steps") as ArrayList<Float>
                    if (week.isNotEmpty()) {
                        barSet = listOf(
                            "MON" to week.get(0),
                            "TUE" to week.get(1),
                            "WED" to week.get(2),
                            "THU" to week.get(3),
                            "FRI" to week.get(4),
                            "SAT" to week.get(5),
                            "SUN" to week.get(6)
                        )
                        txtMin.text = String.format("%.2f", week.min())
                        txtMax.text = String.format("%.2f", week.max())
                        txtAvg.text = String.format("%.2f",(week.sum() / 7.0))
                    }
                    if (animtionFlag) {
                        barChart.run { animate(barSet) }
                    }

                    txtDaily.text = dataSteps
                    txtGoal.text = "/" + dataGoal
                    pbDailyProgress.progress = progress(dataSteps, dataGoal)
                    txtProgress.text = progress(dataSteps, dataGoal).toString() + "% finished"
                    txtStreak.text = dataStreak
                    txtPB.text = dataPB
                } else {
                    Log.d(TAG, "The document does not exits :(")
                }
            } else {
                task.exception?.message?.let {
                    Log.d(TAG, it)
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun writeToDB(totalSteps: Int){
        var steps = 0
        val day = LocalDate.now().dayOfWeek
        val dayNum = day.value  //  1-MONDAY, ..., 7-SUNDAY
        var week: ArrayList<Float>
        val doc = db.collection("users").document(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    steps = document.get("todays_steps").toString().toInt()

                    var pb = document.get("pb").toString().toInt()
                    steps += totalSteps
                    pb = max(pb, steps)
                    Log.d(TAG, "pb $pb, steps $steps")
//                    if(steps > pb) {
//                        pb = steps
//                    }
                    db.collection("users").document(uid)
                        .update( mapOf("todays_steps" to steps, "pb" to pb))
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }

                    week = document.get("week_steps") as ArrayList<Float>

                    week[dayNum - 1] = steps.toFloat()

                    db.collection("users").document(uid)
                        .update("week_steps", week)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }
                    Log.d(TAG, week.toString())


                }
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


}