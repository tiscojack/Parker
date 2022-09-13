package com.example.parker.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.parker.R
import com.example.parker.places.PlaceLocal
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.storage.StorageReference
import java.io.InputStream


class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {

        // 1. Get tag
        val place = p0.tag as? PlaceLocal ?: return null


        val regex = Regex(".+\\.jpg")
        var isCustom = false
        if (regex.matches(place.name)) {
            isCustom = true
        }

        // Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_contents, null
        )


        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = if (isCustom) {
            "Custom Marker"
        } else {
            place.name
        }

        view.findViewById<TextView>(
            R.id.text_view_address
        ).text = if (isCustom) {
            "Reported by Anonymous"
        } else {
            place.address
        }

        if (isCustom) {
            val toggle = view.findViewById<TextView>(R.id.text_view_toggle)
            toggle.visibility = VISIBLE
            toggle.text = "Click to see photo and upvote/downvote"
        }

        view.findViewById<TextView>(
            R.id.text_view_rating
        ).text = if (isCustom) {
            "Upvotes: %.0f"
        } else {
            "Rating: %.1f"
        }.format(place.rating)

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}


@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}