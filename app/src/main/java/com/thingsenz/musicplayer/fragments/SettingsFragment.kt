package com.thingsenz.musicplayer.fragments

import android.content.Intent
import android.media.Image.Plane
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.ui.activities.AboutActivity
import com.thingsenz.musicplayer.utils.Prefs

class SettingsFragment: PreferenceFragmentCompat() {

    companion object {
        @Volatile
        private var sINSTANCE: SettingsFragment? = null

        fun getInstance(): SettingsFragment = synchronized(this) {
            if (sINSTANCE==null) {
                sINSTANCE = SettingsFragment()
            }
            return@synchronized sINSTANCE!!
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_settings)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val darkModePref = findPreference<SwitchPreference>(getString(R.string.dark_mode_key))!!
        darkModePref.isChecked = Prefs.getInstance().darkMode
        darkModePref.setOnPreferenceChangeListener { _, newValue ->
            Prefs.getInstance().darkMode = newValue as Boolean
            true
        }

        val osPref = findPreference<Preference>(getString(R.string.licenses_key))!!
        osPref.setOnPreferenceClickListener {
            LibsBuilder().start(requireContext())
            true
        }

        val aboutPref = findPreference<Preference>(getString(R.string.about_key))!!
        aboutPref.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(),AboutActivity::class.java))
            true
        }

        val playSongSeqPref = findPreference<SwitchPreference>(getString(R.string.play_song_seq_key))!!
        playSongSeqPref.isChecked = Prefs.getInstance().playSongsSeq
        playSongSeqPref.setOnPreferenceChangeListener { _, newValue ->
            Prefs.getInstance().playSongsSeq = newValue as Boolean
            true
        }

    }


}