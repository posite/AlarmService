package com.posite.my_alarm.util.permission

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

abstract class SinglePermission(activity: ComponentActivity) : Permission {

    abstract val permission: String

    private val activityRef = WeakReference(activity)

    private var onSuccess: (() -> Unit)? = null

    private var onDeny: ((permission: String) -> Unit)? = null

    protected val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSuccess?.invoke()
        } else {
            onDeny?.invoke(permission)
        }
    }

    override fun isGranted(): Boolean {
        val activity = activityRef.get() ?: run {
            return false
        }
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun request() {
        permissionLauncher.launch(permission)
    }

    fun onSuccess(listener: () -> Unit): SinglePermission {
        this.onSuccess = listener
        return this
    }

    fun onDeny(listener: (permission: String) -> Unit): SinglePermission {
        this.onDeny = listener
        return this
    }

}