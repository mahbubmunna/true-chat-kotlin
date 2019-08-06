package me.moonss.truechat.registerlogin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import me.moonss.truechat.R

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            Log.d("LoginActivity", "Attempt login with email/pw: $email/***")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Login", "Successful Login. User: ${it.result!!.user.email}")
                    Toast.makeText(
                        this,
                        "Successful Login. User: ${it.result!!.user.email}", Toast.LENGTH_LONG)
                        .show()
                }
                .addOnFailureListener {
                    Log.d("Login", it.message)
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }

        }

        back_to_register_textview.setOnClickListener {
            finish()
        }
    }
}