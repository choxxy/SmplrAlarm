package de.coldtea.smplr.alarm

import android.app.AlarmManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // https://kubiakdev.medium.com/notification-permission-request-on-android-13-part-2-the-implementation-f512239a9bc
    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // permission granted
        } else {
            // permission denied or forever denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // permission granted
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // show rationale and then launch launcher to request permission
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun canExactAlarmsBeScheduled(): Boolean {
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // below API it can always be scheduled
        }
    }

    override fun onResume() {
        super.onResume()
        // non relevant code here
        if (!canExactAlarmsBeScheduled()) {
            // start System Settings to enable alarms, coming back to Myapp will
            // provoke an onStart again
            AlertDialog.Builder(this)
                .setMessage(
                    getString(
                        R.string.need_permission_to_schedule_alarms,
                        getString(R.string.app_name)
                    )
                )
                .setPositiveButton(getString(R.string.dialog_ok)) { _: DialogInterface, _: Int ->
                    intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)/*.apply {
                        data = Uri.fromParts(PACKAGE_SCHEME, packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }*/
                    startActivity(intent)
                }.show()
        }
    }
}

