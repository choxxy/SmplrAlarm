package de.coldtea.smplr.alarm.alarms

import android.Manifest
import android.app.AlarmManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.coldtea.smplr.alarm.R
import de.coldtea.smplr.alarm.alarms.models.WeekInfo
import de.coldtea.smplr.alarm.databinding.FragmentAlarmsBinding
import de.coldtea.smplr.alarm.extensions.nowPlus
import de.coldtea.smplr.smplralarm.apis.SmplrAlarmAPI
import de.coldtea.smplr.smplralarm.models.NotificationItem
import de.coldtea.smplr.smplralarm.smplrAlarmRenewMissingAlarms
import de.coldtea.smplr.smplralarm.smplrAlarmUpdate
import java.util.Calendar


class AlarmFragment : Fragment() {

    private lateinit var binding: FragmentAlarmsBinding

    private val viewModel by viewModels<AlarmViewModel>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleButton(true)
        } else {
            toggleButton(false)
            Toast.makeText(
                requireContext(),
                "permission denied or forever denied,cannot set alarm",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleButton(enabled: Boolean) {
        binding.setIntentAlarm.isEnabled = enabled
        binding.setNoNotificationAlarm.isEnabled = enabled
        binding.setNotificationAlarm.isEnabled = enabled
        binding.updateList.isEnabled = enabled
        binding.updateAlarms.isEnabled = enabled
        binding.checkAlarm.isEnabled = enabled
        binding.updateNotification.isEnabled = enabled
    }

    private fun canExactAlarmsBeScheduled(): Boolean {
        val alarmManager =
            requireContext().getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // below API it can always be scheduled
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)

        viewModel.initAlarmListListener(requireContext().applicationContext)
        viewModel.alarmListAsJson.observe(viewLifecycleOwner) {
            binding.alarmListJson.text = it
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (!canExactAlarmsBeScheduled()) {
            AlertDialog.Builder(requireContext())
                .setMessage(
                    getString(
                        R.string.need_permission_to_schedule_alarms,
                        getString(R.string.app_name)
                    )
                ).setPositiveButton(getString(R.string.dialog_ok)) { _: DialogInterface, _: Int ->
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }.show()
        }
    }

    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setMessage("Notification blocked")
        alertDialog.setPositiveButton(
            "Grant Permission"
        ) { dialog, _ -> // Responds to click on the action
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
        }
        alertDialog.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.create().show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // permission granted.Awesome
            toggleButton(true)

        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showDialog()
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        val defaultTime = Calendar.getInstance().nowPlus(2)

        binding.hour.setText(defaultTime.first.toString())
        binding.minute.setText(defaultTime.second.toString())
        binding.sunday.isChecked = true

        binding.setIntentAlarm.setOnClickListener {

            val weekInfo = binding.getWeekInfo()

            val alarmId = viewModel.setFullScreenIntentAlarm(
                binding.hour.text.toString().toInt(),
                binding.minute.text.toString().toInt(),
                weekInfo,
                requireContext().applicationContext
            )

            binding.toastAlarm()
            binding.alarmId.setText(alarmId.toString())
        }

        binding.setNotificationAlarm.setOnClickListener {

            val weekInfo = binding.getWeekInfo()

            val alarmId = viewModel.setNotificationAlarm(
                binding.hour.text.toString().toInt(),
                binding.minute.text.toString().toInt(),
                weekInfo,
                requireContext().applicationContext
            )

            binding.toastAlarm()
            binding.alarmId.setText(alarmId.toString())
        }

        binding.setNoNotificationAlarm.setOnClickListener {

            val weekInfo = binding.getWeekInfo()

            val alarmId = viewModel.setNoNotificationAlarm(
                binding.hour.text.toString().toInt(),
                binding.minute.text.toString().toInt(),
                weekInfo,
                requireContext().applicationContext
            )

            binding.toastAlarm()
            binding.alarmId.setText(alarmId.toString())
        }


        binding.updateList.setOnClickListener {
            viewModel.requestAlarmList()
        }

        binding.updateAlarms.setOnClickListener {
            val weekInfo = binding.getWeekInfo()

            viewModel.updateAlarm(
                binding.alarmId.text.toString().toInt(),
                binding.hour.text.toString().toInt(),
                binding.minute.text.toString().toInt(),
                weekInfo,
                binding.isActive.isChecked,
                requireContext().applicationContext
            )

            viewModel.updateAlarm(
                binding.alarmId.text.toString().toInt(),
                binding.hour.text.toString().toInt(),
                binding.minute.text.toString().toInt(),
                weekInfo,
                binding.isActive.isChecked,
                requireContext().applicationContext
            )
        }

        binding.updateNotification.setOnClickListener {
            smplrAlarmUpdate(requireContext().applicationContext) {
                requestCode { binding.alarmId.text.toString().toInt() }
                notification {
                    NotificationItem(
                        R.drawable.ic_baseline_change_circle_24,
                        "I am changed",
                        "I am changed",
                        "I am changed"
                    )
                }
            }
        }

        binding.cancelAlarm.setOnClickListener {
            viewModel.cancelAlarm(
                binding.alarmId.text.toString().toInt(),
                requireContext().applicationContext
            )
        }

        binding.checkAlarm.setOnClickListener {
            val intent = SmplrAlarmAPI.getAlarmIntent(
                binding.alarmId.text.toString().toInt(),
                requireContext().applicationContext
            )

            Toast.makeText(requireContext(), intent?.toString().orEmpty(), Toast.LENGTH_LONG).show()
        }

        smplrAlarmRenewMissingAlarms(requireContext())
    }

    private fun isPermissionGranted(name: String) = ContextCompat.checkSelfPermission(
        requireContext(), name
    ) == PackageManager.PERMISSION_GRANTED

    private fun FragmentAlarmsBinding.getWeekInfo(): WeekInfo =
        WeekInfo(
            monday.isChecked,
            tuesday.isChecked,
            wednesday.isChecked,
            thursday.isChecked,
            friday.isChecked,
            saturday.isChecked,
            sunday.isChecked
        )

    private fun FragmentAlarmsBinding.toastAlarm() {
        var toastText = "${hour.text}:${minute.text}"
        if (monday.isChecked) toastText = toastText.plus(" Monday")
        if (tuesday.isChecked) toastText = toastText.plus(" Tuesday")
        if (wednesday.isChecked) toastText = toastText.plus(" Wednesday")
        if (thursday.isChecked) toastText = toastText.plus(" Thursday")
        if (friday.isChecked) toastText = toastText.plus(" Friday")
        if (saturday.isChecked) toastText = toastText.plus(" Saturday")
        if (sunday.isChecked) toastText = toastText.plus(" Sunday")

        Toast.makeText(requireContext(), toastText, LENGTH_SHORT).show()

    }
}