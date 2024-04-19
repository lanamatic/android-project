package com.example.myapplicationprojekat

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SigupActavity : AppCompatActivity() {


    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtGoal: EditText
    private lateinit var btnSignup: Button

    private lateinit var auth: FirebaseAuth
//    val db = Firebase.firestore

    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //initialize firebase auth
        auth = FirebaseAuth.getInstance()
        //initialize firebase firestore
        db = FirebaseFirestore.getInstance()

        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnSignup = findViewById(R.id.button2)
        edtUsername = findViewById(R.id.username)
        edtEmail = findViewById(R.id.email)
        edtGoal = findViewById(R.id.goal)
        edtPassword = findViewById(R.id.password)

        btnSignup.setOnClickListener {
            val email = edtEmail.text.toString()
            val pass = edtPassword.text.toString()
            val username = edtUsername.text.toString()
            val goal: Int = edtGoal.text.toString().toInt()

            signup(username, email, pass, goal)
        }
    }

    private fun signup(username:String, email:String, password:String, goal: Int){

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
//                    val goal: Int = 9999
                    auth.currentUser?.let { addUserToDB(username, email, it.uid, goal) }
//
//                    //jump to main activity
//                    val intent = Intent(this@SigupActavity, MainActivity::class.java)
//                    startActivity(intent)

                } else {
                    Toast.makeText(this@SigupActavity, "Error :(", Toast.LENGTH_SHORT).show()

                }
            }

    }

    private fun addUserToDB(username: String, email: String, uid: String, goal: Int) {
        val user = User(username, email, uid, goal)
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${uid}")
                //jump to main activity

                val intent = Intent(this@SigupActavity, MainActivity::class.java)
                finish()
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@SigupActavity, "Error -" + e.message, Toast.LENGTH_SHORT).show()
            }
    }
}