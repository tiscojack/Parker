package com.example.parker.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import com.example.parker.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.parker.databinding.ActivityMapsBinding
import com.example.parker.listeners.PlaceListener
import com.example.parker.places.PlaceLocal
import com.example.parker.places.PlaceResponse
import com.example.parker.utilities.GlideApp
import com.example.parker.utilities.MarkerInfoWindowAdapter
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.properties.Delegates

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var cameraPosition: CameraPosition? = null

    private val database =
        Firebase.database("https://parker-3cbe0-default-rtdb.europe-west1.firebasedatabase.app")
    private val myRef = database.getReference("places")

    private val storageRef =
        Firebase.storage("gs://parker-3cbe0.appspot.com").getReference("images")

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= 33) {
                lastKnownLocation =
                    savedInstanceState.getParcelable(
                        KEY_LOCATION,
                        Location::class.java
                    )
                cameraPosition =
                    savedInstanceState.getParcelable(
                        KEY_CAMERA_POSITION,
                        CameraPosition::class.java
                    )
            } else {
                lastKnownLocation =
                    savedInstanceState.getParcelable(KEY_LOCATION)
                cameraPosition =
                    savedInstanceState.getParcelable(KEY_CAMERA_POSITION)

            }
        }

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val logoutButton: Button = findViewById<View>(R.id.btn_logout) as Button
        val context: Context = this
        logoutButton.setOnClickListener {
            AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener {
                    // ...
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        }

        val backButton: ImageButton = findViewById(R.id.backB)
        backButton.setOnClickListener {
            val intent = Intent(context, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        this.mMap.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "Map ready")
        mMap = googleMap

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                ), DEFAULT_ZOOM.toFloat()
                            )
                        )
                    }
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)
                    mMap.moveCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                    )
                    mMap.uiSettings.isMyLocationButtonEnabled = false
                }
            }

        } catch (e: SecurityException) {
            Log.e(
                "Exception: %s",
                e.message, e
            )
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            myRef.addValueEventListener(PlaceListener(mMap, this))
            mMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
            mMap.setOnInfoWindowClickListener {
                showCustomDialog(it.tag)
            }
        } catch (e: SecurityException) {
            Log.e(
                "Exception: %s",
                e.message, e
            )
        }
    }


    private fun showCustomDialog(tag: Any?) {
        var karma: Long
        val dialog = Dialog(this)
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //The user will be able to cancel the dialog by clicking anywhere outside the dialog.
        dialog.setCancelable(true)
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.custom_marker_dialog)

        val place = tag as? PlaceLocal ?: return

        var userOwner: String?
        val regex = Regex(".+\\.jpg")
        var isCustom : Boolean
        if (regex.matches(place.name)) {
            isCustom = true
            userOwner = place.name.dropLast(9)
            Log.d(
                "Dialog",
                userOwner
            )
        } else {
            return
        }

        var vote by Delegates.observable(false) { _, oldValue, newValue ->
            if (newValue != oldValue) {
                Log.d(
                    "Dialog",
                    "New value: $newValue, old value: $oldValue"
                )
                myRef.parent?.child("users")?.child("$userOwner")?.get()
                    ?.addOnSuccessListener {
                        karma = it.value as Long
                        Log.d(
                            "Dialog",
                            "User current karma: $karma"
                        )
                        karma = if (newValue) {
                            karma.plus(1)
                        } else {
                            karma.minus(1)
                        }
                        Log.d(
                            "Dialog",
                            "User karma after upvote : $karma"
                        )
                        myRef.parent?.child("users")?.child("$userOwner")?.setValue(karma)
                    }

                val query = myRef.orderByChild("name").equalTo(tag.name)

                query.get().addOnSuccessListener {
                    for (childData in it.children) {
                        val place: PlaceResponse? = it.getValue(PlaceResponse::class.java)
                    }
                    Log.d("Dialog", it.value.toString())

                    val map = it.value as Map<String, *>
                    karma = place.rating.toLong()
                    Log.d(
                        "Dialog",
                        "Marker current rating: $karma"
                    )
                    karma = if (newValue) {
                        karma.plus(1)
                    } else {
                        karma.minus(1)
                    }
                    Log.d(
                        "Dialog",
                        "Marker rating after upvote : $karma"
                    )
                    myRef.child(map.keys.elementAt(0)).child("rating").setValue(karma)
                }
            }
        }

        //Initializing the views of the dialog.
        val upvoteButton = dialog.findViewById<Button>(R.id.upvote)
        val downvoteButton = dialog.findViewById<Button>(R.id.downvote)

        // If GlideApp is not defined, build the application (comment it out, build and kapt will create the instance)
        if (isCustom) {
            val image: ImageView = dialog.findViewById(R.id.photo_download)
            GlideApp.with(this)
                .load(storageRef.child(place.name))
                .into(image)
        }


        upvoteButton.setOnClickListener {
            vote = true
            dialog.dismiss()
        }


        downvoteButton.setOnClickListener {
            vote = false
            dialog.dismiss()
        }



        dialog.show()
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}