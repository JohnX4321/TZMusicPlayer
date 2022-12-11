package com.thingsenz.musicplayer.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.thingsenz.musicplayer.MainActivity
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.utils.Util

class PermissionDeniedFragment: Fragment() {

    companion object {
        @Volatile
        private var sINSTANCE: PermissionDeniedFragment? = null

        fun getInstance(): PermissionDeniedFragment = synchronized(this) {
            if (sINSTANCE==null)
                sINSTANCE = PermissionDeniedFragment()
            return@synchronized sINSTANCE!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perm_denied,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView = view.findViewById<ImageView>(R.id.permImage)
        val textView = view.findViewById<TextView>(R.id.permText)
        val grantBtn = view.findViewById<AppCompatButton>(R.id.permButton)
        //imageView.setImageDrawable(ContextCompat.getDrawable(view.context,0))
        textView.text = if (Util.shouldShowRationale(requireActivity())) getString(R.string.perm_failed_request_again) else getString(R.string.perm_failed_redirect_settings)
        grantBtn.setOnClickListener {
            if (Util.shouldShowRationale(requireActivity()))
                (requireActivity() as MainActivity).permissionLauncher.launch(Util.getPermissionList())
            else {
                val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }


}