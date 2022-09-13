package com.example.parker.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.parker.R
import com.example.parker.databinding.ActivityDashboardBinding
import com.example.parker.listeners.GrantPermissionListener
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class DashboardActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityDashboardBinding

    private var locationPermissionGranted: Boolean? = null
    private var cameraActivityPermissionGranted: Boolean? = null
    private val TAG = DashboardActivity::class.simpleName

    private lateinit var uid : String

    private val database =
        Firebase.database("https://parker-3cbe0-default-rtdb.europe-west1.firebasedatabase.app")
    private val myRef = database.getReference("users")

    private fun hasLocationPerms(): Boolean {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (locationPermissionGranted == null) {
            for (permission in GrantPermissionListener.getPermsList(0)) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "Failed to grant location perms")
                    return false
                }
            }
            return true
        }
        return locationPermissionGranted!!
    }

    private fun hasCameraActivityPerms(): Boolean {
        if (cameraActivityPermissionGranted == null) {
            for (permission in GrantPermissionListener.getPermsList(1)) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "Failed to grant camera perms")
                    return false
                }
            }
            return true
        }
        return cameraActivityPermissionGranted!!
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // register all the ImageButtons with their appropriate IDs
        val logOutB: ImageButton = findViewById(R.id.logOutB)

        // register all the Buttons with their appropriate IDs
        val rewardB: Button = findViewById(R.id.rewardB)
        val deleteProfileB: Button = findViewById(R.id.deleteProfileB)

        // register all the card views with their appropriate IDs
        val contributeCard: CardView = findViewById(R.id.contributeCard)
        val mapCard: CardView = findViewById(R.id.mapCard)
        val sourcecodeCard: CardView = findViewById(R.id.learnCard)
        val adminCard: CardView = findViewById(R.id.adminCard)
        val helpCard: CardView = findViewById(R.id.helpCard)
        val settingsCard: CardView = findViewById(R.id.settingsCard)


        val user = Firebase.auth.currentUser
        user?.let {
            val username: TextView = findViewById(R.id.name)
            username.text = "${user.displayName}"

            Log.d(TAG, "${user.photoUrl}")
            if (user.photoUrl != null) {
                val profilepic: ImageButton = findViewById(R.id.profileB)
                Picasso.get().load("${user.photoUrl}").into(profilepic)
            }

            val karmatext: TextView = findViewById(R.id.karma)
            uid = user.uid
            myRef.child(uid).get().addOnSuccessListener {
                Log.d(
                    "firebase",
                    "Got value ${it.value}"
                )
                val amount = it.value as Long
                val i = amount.toInt()
                karmatext.text = "$i Karma"
            }.addOnFailureListener {
                Log.e(
                    "firebase",
                    "Error getting data",
                    it
                )
            }
        }


        val context: Context = this
        logOutB.setOnClickListener {
            AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener {
                    // ...
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        }

        rewardB.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: Karma rewards, mainly customization unlocks",
                Toast.LENGTH_SHORT
            ).show()
        }
        deleteProfileB.setOnClickListener {
            user!!.delete().addOnCompleteListener{task ->
                if(task.isSuccessful){
                    Log.d(TAG, "User account deleted")
                    myRef.child(uid).removeValue()
                    val intent = Intent(this, LoginActivity::class.java)
                    this.startActivity(intent)
                }
            }
        }



        if (!hasLocationPerms()) {
            val listener: View.OnClickListener = GrantPermissionListener(this, 0)
            mapCard.setOnClickListener(listener)
        } else {
            mapCard.setOnClickListener {
                val intent = Intent(this, MapsActivity::class.java)
                this.startActivity(intent)
            }
        }

        if (!hasCameraActivityPerms()) {
            val listener: View.OnClickListener = GrantPermissionListener(this, 1)
            contributeCard.setOnClickListener(listener)
        } else {
            contributeCard.setOnClickListener {
                val intent = Intent(this, CameraActivity::class.java)
                this.startActivity(intent)
            }
        }



        sourcecodeCard.setOnClickListener {
            val url = "https://github.com/tiscojack/Parker"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        adminCard.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: Admin Area for Parking Facilities managers",
                Toast.LENGTH_SHORT
            ).show()
        }
        helpCard.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: Simple activity to report bugs or contact support",
                Toast.LENGTH_SHORT
            ).show()
        }


        settingsCard.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "onRequestPermissionsResult")

        // Check if the permission are granted

        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.w(
                    TAG,
                    "A needed permission is not granted!"
                )
                when (requestCode) {
                    123 -> locationPermissionGranted = false
                    101 -> cameraActivityPermissionGranted = false
                }
                return
            }
        }

        Log.d(
            TAG,
            "permissions obtained"
        )
        lateinit var intent: Intent
        when (requestCode) {
            123 -> {
                this.locationPermissionGranted = true
                intent = Intent(this, MapsActivity::class.java)
            }
            101 -> {
                this.cameraActivityPermissionGranted = true
                intent = Intent(this, CameraActivity::class.java)
            }
            else -> {
                intent = Intent(this, DashboardActivity::class.java)
            }
        }
        this.startActivity(intent)
        finish()
    }

}
