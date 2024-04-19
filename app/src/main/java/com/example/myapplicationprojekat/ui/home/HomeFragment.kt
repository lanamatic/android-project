package com.example.myapplicationprojekat.ui.home

import android.content.ContentValues.TAG
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
import com.example.myapplicationprojekat.R
import com.example.myapplicationprojekat.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth


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

        //getting data from db
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        var dataSteps: String = ""
        var dataGoal: String  = ""
        var dataStreak: String = ""
        var dataPB: String  = ""

        db = FirebaseFirestore.getInstance()
        val uid = user!!.uid
        db.collection("users").document(uid).get().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                val document = task.result
                if(document.exists()){
                    dataSteps = document.get("todays_steps").toString()
                    dataGoal = document.get("goal_steps").toString()
                    dataStreak = document.get("streak").toString()
                    dataPB = document.get("pb").toString()

                    //fetching layout elements
                    val txtDaily:TextView = view.findViewById(R.id.txtDaily)
                    val txtGoal:TextView = view.findViewById(R.id.txtGoal)
                    val pbDailyProgress:ProgressBar= view.findViewById(R.id.pbDailySteps)
                    val txtProgress:TextView = view.findViewById(R.id.txtProgress)
                    val txtStreak:TextView = view.findViewById(R.id.txtStreak)
                    val txtPB:TextView = view.findViewById(R.id.txtPB)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}