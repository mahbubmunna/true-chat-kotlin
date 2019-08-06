package me.moonss.truechat.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import me.moonss.truechat.R
import me.moonss.truechat.messages.LatestMessagesActivity
import me.moonss.truechat.models.User
import java.util.*

class RegisterActivity: AppCompatActivity() {
    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_text_view.setOnClickListener {
            Log.d("RegisterActivity", "try to open login activity somehow")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        photopicker_button_register.setOnClickListener {
            Log.d("Register", "Try to pick photo ")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            photopicker_button_register.alpha = 0f
            profileImage_imgaeview_register.setImageBitmap(bitmap)
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //photopicker_button_register.setBackgroundDrawable(bitmapDrawable)
        }

    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please Enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "email: $email")
        Log.d("RegisterActivity", "Password: $password")

        //FireBase Auth to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful
                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result!!.user.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", it.message)
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        Log.d("RegisterActivity", "Filename: $filename")

        if (selectedPhotoUri != null) {

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfuly uploaded to path: ${it.metadata!!.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "Link: $it")

                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "$it")

                }
        } else {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_LONG).show()
        }



    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user =
            User(uid, username_edittext_register.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully added user to Firebase database $it")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed with message: $it")
                Toast.makeText(this, "Failed with message: $it", Toast.LENGTH_LONG).show()
            }
    }
}

