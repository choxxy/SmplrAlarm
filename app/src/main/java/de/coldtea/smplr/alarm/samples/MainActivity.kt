package de.coldtea.smplr.alarm.samples

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import de.coldtea.smplr.alarm.R

/**
 * @author Nav Singh
 */
class MainActivity : AppCompatActivity() {
  
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            sendNotification(this)
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val message = intent?.getStringExtra(NOTIFICATION_MESSAGE_TAG)
        findViewById<TextView>(R.id.tv_message).text = message
        findViewById<Button>(R.id.click).setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    Log.e(TAG, "onCreate: PERMISSION GRANTED")
                    sendNotification(this)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Snackbar.make(
                        findViewById(R.id.parent_layout),
                        "Notification blocked",
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        // Responds to click on the action
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }.show()
                }
                else -> {
                    // The registered ActivityResultCallback gets the result of this request
                        requestPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val NOTIFICATION_MESSAGE_TAG = "message from notification"
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
            putExtra(
                NOTIFICATION_MESSAGE_TAG, "Hi ☕\uD83C\uDF77\uD83C\uDF70"
            )
        }
    }
}