package com.example.myapplicationprojekat.ui.dashboard

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationprojekat.R
import com.example.myapplicationprojekat.User
import com.example.myapplicationprojekat.UserAdapter
import com.example.myapplicationprojekat.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var userRecyclingView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        userList = ArrayList(10)
//        adapter = UserAdapter(requireContext(), userList)
//        userRecyclingView = view.findViewById(R.id.recycler)
//        userRecyclingView.layoutManager = LinearLayoutManager(this)
//        userRecyclingView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        db.collection("users").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Access document data
                    // Process data as needed
                    val dataUsername = document.data.get("username").toString()
                    val dataEmail = document.data.get("email").toString()
                    val dataUid = document.data.get("uid").toString()
                    val dataGoal = document.data.get("goal_steps").toString().toInt()
                    val dataDaily = document.data.get("todays_steps").toString().toInt()
                    val dataStreak = document.data.get("streak").toString().toInt()
                    val dataPB = document.data.get("pb").toString().toInt()

                    val dataUser = User(dataUsername, dataEmail, dataUid, dataGoal, dataDaily, dataStreak, dataPB)
                    userList.add(dataUser)


                }
                val competitors = view.findViewById<ListView>(R.id.competitors)
                val arrayAdapter: ArrayAdapter<*>
                //TODO sortiranje i lepsi ispis
                arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, userList)
                competitors.adapter = arrayAdapter
                Log.d(TAG, userList.toString())

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}