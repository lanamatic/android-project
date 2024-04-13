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

class SigupActavity : AppCompatActivity() {


    private lateinit var edtUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignup: Button

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnSignup = findViewById(R.id.button2)
        edtUsername = findViewById(R.id.username)
        editEmail = findViewById(R.id.email)
        edtPassword = findViewById(R.id.password)

        btnSignup.setOnClickListener {
            val email = editEmail.text.toString()
            val pass = edtPassword.text.toString()
            val username = edtUsername.text.toString()

            signup(username, email, pass)
        }
    }

    private fun signup(username:String, email:String, password:String){

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //jump tp main activity
                    val intent = Intent(this@SigupActavity, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    Toast.makeText(this@SigupActavity, "Error :(", Toast.LENGTH_SHORT).show()

                }
            }

    }
}