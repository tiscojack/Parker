package com.example.parker.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.parker.R

//TODO: proper settings

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}