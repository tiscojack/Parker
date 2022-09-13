package com.example.parker.listeners

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.parker.R
import com.example.parker.places.PlaceLocal
import com.example.parker.places.PlaceResponse
import com.example.parker.places.toPlace
import com.example.parker.utilities.BitmapHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class PlaceListener(private val map: GoogleMap, private val context: Context) : ValueEventListener {

    private val places = mutableListOf<PlaceLocal>()

    private val parkingIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(context, R.color.green_500)
        BitmapHelper.vectorToBitmap(context, R.drawable.ic_baseline_local_parking_24, color)
    }

    private fun addMarkers(googleMap: GoogleMap) {
        places.forEach { place ->
            Log.d("marker", "added in ${place.latLng} and name: ${place.name}")
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .position(place.latLng)
                    .icon(parkingIcon)
            )

            // Set place as the tag on the marker object so it can be referenced within
            // MarkerInfoWindowAdapter
            marker?.tag = place
        }
    }


    override fun onDataChange(snapshot: DataSnapshot) {

        for (childData in snapshot.children) {
            val place: PlaceResponse? = childData.getValue(PlaceResponse::class.java)
            if (place != null) {
                place.toPlace()?.let { places.add(it) }
            }
        }
        addMarkers(map)
    }

    override fun onCancelled(error: DatabaseError) {
        Log.e(
            "JSON",
            "onCancelled: Something went wrong! Error:" + error.message
        )
    }
}
