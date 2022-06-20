package com.hushbunny.app.uitls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionUtility {
    fun requestCameraPermission(request: ActivityResultLauncher<String>) {
        request.launch(Manifest.permission.CAMERA)
    }
    fun userGrantedCameraPermission(hostActivity: FragmentActivity): Boolean {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            hostActivity,
            Manifest.permission.CAMERA
        )

        return hasCameraPermission == PackageManager.PERMISSION_GRANTED
    }
    fun showPermissionRationaleToast(fragmentContext: Context, message: String) {
        Toast.makeText(
            fragmentContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}