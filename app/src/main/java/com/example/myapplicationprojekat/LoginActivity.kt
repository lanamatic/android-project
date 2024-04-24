package com.example.myapplicationprojekat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        //check if user is already logged in
        if(auth.currentUser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            finish()
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtUsername = findViewById(R.id.username)
        edtPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.login)
        btnSignup = findViewById(R.id.signup)

        btnSignup.setOnClickListener {
            val intent = Intent(this, SigupActavity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString()
            val password = edtPassword.text.toString()
            //validation
            if(username.isNotEmpty() && password.isNotEmpty()){
                login(username, password)
            }else{
                Toast.makeText(this@LoginActivity, "Empty fields ar not allowed!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun login(username: String, password: String){

        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //login
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LoginActivity, "User does not exists", Toast.LENGTH_SHORT).show()

                }
            }
    }


}