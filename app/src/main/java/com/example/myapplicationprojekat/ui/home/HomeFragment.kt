package com.example.myapplicationprojekat.ui.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import java.util.Calendar
import java.util.Collections.max
import java.util.Collections.min

class HomeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeBinding? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var sensorMenager: SensorManager? = null
    private var running = false
    private var totalSteps = 0
    private var previousTotalSteps = 0f

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

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0].toInt()

            writeToDB(totalSteps)
            readFromDB(false)
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
                        txtMin.text = week.min().toString()
                        txtMax.text = week.max().toString()
                        txtAvg.text = (week.sum() / 7.0).toString()
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

    private fun writeToDB(totalSteps: Int){
        var steps = 0
        val doc = db.collection("users").document(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    steps = document.get("todays_steps").toString().toInt()

                    steps += totalSteps
                    db.collection("users").document(uid)
                        .update("todays_steps", steps.toString())
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }
                }
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}