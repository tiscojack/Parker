package com.example.parker.listeners

import android.Manifest
import android.app.Activity
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat

class GrantPermissionListener(private val activity: Activity) : View.OnClickListener {

    override fun onClick(v: View?) {
        Log.d(
            TAG,
            "grantPerms button clicked"
        )

        ActivityCompat.requestPermissions(
            this.activity,
            PERMISSION_NEEDED,
            LOCATION_PERMISSION_REQUEST_ID
        )
    }

    companion object {
        public val PERMISSION_NEEDED = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
        )
        private val TAG: String? = GrantPermissionListener::class.simpleName
        private const val LOCATION_PERMISSION_REQUEST_ID = 123;
    }

}