package com.example.parker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.parker.listeners.GrantPermissionListener
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class DashboardActivity : AppCompatActivity() {


    private var allPermissionGranted: Boolean? = null
    private val TAG = DashboardActivity::class.simpleName


    private val database =
        Firebase.database("https://parker-3cbe0-default-rtdb.europe-west1.firebasedatabase.app")
    private val myRef = database.getReference("users")

    private fun hasPerms(): Boolean {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (allPermissionGranted == null) {
            for (permission in GrantPermissionListener.PERMISSION_NEEDED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                    Log.d(TAG, "Failed to grant perms")
                }
            }
            return true
        }

        return allPermissionGranted!!

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // register all the ImageButtons with their appropriate IDs
        val logOutB: ImageButton = findViewById(R.id.logOutB)
        val profileB: ImageButton = findViewById(R.id.profileB)

        // register all the Buttons with their appropriate IDs
        val rewardB: Button = findViewById(R.id.rewardB)
        val editProfileB: Button = findViewById(R.id.editProfileB)

        // register all the card views with their appropriate IDs
        val contributeCard: CardView = findViewById(R.id.contributeCard)
        val mapCard: CardView = findViewById(R.id.mapCard)
        val learnCard: CardView = findViewById(R.id.learnCard)
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
            myRef.child("${user.uid}").get().addOnSuccessListener {
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

        //TODO: handle each of the buttons with the OnClickListener
        rewardB.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: Karma rewards, mainly customization unlocks",
                Toast.LENGTH_SHORT
            ).show()
        }
        editProfileB.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: Editing Profile",
                Toast.LENGTH_SHORT
            ).show()
        }


        //TODO: handle each of the cards with the OnClickListener
        contributeCard.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: Add custom Marker",
                Toast.LENGTH_SHORT
            ).show()
        }


        if (!hasPerms()) {
            val listener: View.OnClickListener = GrantPermissionListener(this)
            mapCard.setOnClickListener(listener)
        } else {
            mapCard.setOnClickListener {
                val intent = Intent(this, MapsActivity::class.java)
                this.startActivity(intent)
            }
        }


        //TODO: Find a usage for this card
        learnCard.setOnClickListener {
            Toast.makeText(
                this,
                "TODO: I don't know",
                Toast.LENGTH_SHORT
            ).show()
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

        // Check if the permission are granted or

        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.w(
                    TAG,
                    "A needed permission is not granted!"
                )
                allPermissionGranted = false
                return
            }
        }

        Log.d(
            TAG,
            "permissions obtained"
        )
        this.allPermissionGranted = true
        val intent = Intent(this, MapsActivity::class.java)
        this.startActivity(intent)
        finish()
    }

}
