package com.example.myapplicationprojekat.ui.home

import android.content.ContentValues.TAG
import android.content.Context
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplicationprojekat.R
import com.example.myapplicationprojekat.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var db: FirebaseFirestore
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var sensorMenager : SensorManager? = null
    private var running = false
    private var totalSteps = 0
    private var previousTotalSteps = 0f

    private var dataSteps: String = ""
    private var dataGoal: String  = ""
    private var dataStreak: String = ""
    private var dataPB: String  = ""

    private lateinit var txtDaily:TextView
    private lateinit var txtGoal:TextView 
    private lateinit var pbDailyProgress:ProgressBar
    private lateinit var txtProgress:TextView 
    private lateinit var txtStreak:TextView
    private lateinit var txtPB:TextView


    val user = mAuth.currentUser
    val uid = user!!.uid


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.apply {
            barChart.animation.duration = HomeFragment.animationDuration
            barChart.animate(HomeFragment.barSet)
        }
        val root: View = binding.root
//
//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //sensor for steps
        sensorMenager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?


//        //getting data from db



        db = FirebaseFirestore.getInstance()
//        val uid = user!!.uid
        db.collection("users").document(uid).get().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                val document = task.result
                if(document.exists()){
                    dataSteps = document.get("todays_steps").toString()
                    dataGoal = document.get("goal_steps").toString()
                    dataStreak = document.get("streak").toString()
                    dataPB = document.get("pb").toString()

                    //fetching layout elements
                    txtDaily = view.findViewById(R.id.txtDaily)
                    txtGoal = view.findViewById(R.id.txtGoal)
                    pbDailyProgress= view.findViewById(R.id.pbDailySteps)
                    txtProgress = view.findViewById(R.id.txtProgress)
                    txtStreak = view.findViewById(R.id.txtStreak)
                    txtPB = view.findViewById(R.id.txtPB)

                    txtDaily.text = dataSteps
                    txtGoal.text = "/" + dataGoal
                    pbDailyProgress.progress = progress(dataSteps, dataGoal)
                    txtProgress.text = progress(dataSteps,dataGoal).toString() + "% finished"
                    txtStreak.text = dataStreak
                    txtPB.text = dataPB
//                    Log.d(TAG, dataGoal.toString() + " " + dataSteps.toString() + " " + dataStreak)
                }
                else{
                    Log.d(TAG, "The document does not exits :(")
                }
            }
            else{
                task.exception?.message?.let {
                    Log.d(TAG, it)
                }
            }
        }


    }

    private fun progress(dataSteps: String, dataGoal: String): Int{
        return(dataSteps.toInt()*100/dataGoal.toInt())
    }

    companion object{
        //todo zameniti vrednosti
        private  val barSet= listOf(
            "MON" to 11325F,
            "TUE" to 5680F,
            "WED" to 15006F,
            "THU" to 9891F,
            "FRI" to 17121F,
            "SAT" to 10591F,
            "SUN" to 12310F
        )

        private const val animationDuration=1000L
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor : Sensor? = sensorMenager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if(stepSensor == null){
            Toast.makeText(activity, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else{
            sensorMenager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
//            Toast.makeText(activity, "OVDe", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running){
            totalSteps = event!!.values[0].toInt()

            writeToDB(totalSteps)
        }
    }

    private fun writeToDB(totalSteps: Int) {
        var steps = 0
        val doc = db.collection("users").document(uid).get().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                val document = task.result
                if(document.exists()){
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