package com.example.parker.listeners

import android.Manifest
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat

class GrantPermissionListener(private val activity: Activity, mode: Int) : View.OnClickListener {

    private val PERMISSION_NEEDED = getPermsList(mode)
    private val PERMISSION_REQUEST_ID = when (mode) {
        0 -> LOCATION_PERMISSION_REQUEST_ID
        1 -> CAMERA_ACTIVITY_PERMISSION_REQUEST_ID
        else -> 104
    }

    override fun onClick(v: View?) {
        Log.d(
            TAG,
            "grantPerms button clicked"
        )

        ActivityCompat.requestPermissions(
            this.activity,
            PERMISSION_NEEDED,
            PERMISSION_REQUEST_ID
        )
    }

    companion object {

        private val TAG: String? = GrantPermissionListener::class.simpleName
        private const val LOCATION_PERMISSION_REQUEST_ID = 123
        private const val CAMERA_ACTIVITY_PERMISSION_REQUEST_ID = 101

        fun getPermsList(mode: Int): Array<String> {
            val PERMISSION_NEEDED = when (mode) {
                0 -> {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                    )
                }
                1 -> {
                    mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET
                    ).apply {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }.toTypedArray()
                }
                else -> {
                    arrayOf(Manifest.permission.INTERNET)
                }
            }

            return PERMISSION_NEEDED
        }
    }

}