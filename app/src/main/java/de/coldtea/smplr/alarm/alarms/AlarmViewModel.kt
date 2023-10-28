package de.coldtea.smplr.alarm.alarms

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.smplr.alarm.MainActivity
import de.coldtea.smplr.alarm.R
import de.coldtea.smplr.alarm.alarms.models.WeekInfo
import de.coldtea.smplr.alarm.lockscreenalarm.ActivityLockScreenAlarm
import de.coldtea.smplr.alarm.receiver.ActionReceiver
import de.coldtea.smplr.alarm.receiver.AlarmBroadcastReceiver
import de.coldtea.smplr.smplralarm.*
import de.coldtea.smplr.smplralarm.apis.SmplrAlarmListRequestAPI
import de.coldtea.smplr.smplralarm.receivers.AlarmAction
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    lateinit var smplrAlarmListRequestAPI: SmplrAlarmListRequestAPI

    private val _alarmListAsJson = MutableLiveData<String>()
    val alarmListAsJson: LiveData<String>
        get() = _alarmListAsJson

    fun initAlarmListListener(applicationContext: Context) =
        smplrAlarmChangeOrRequestListener(applicationContext) {
            _alarmListAsJson.postValue(it)
        }.also {
            smplrAlarmListRequestAPI = it
        }

    fun setFullScreenIntentAlarm(
        hour: Int,
        minute: Int,
        weekInfo: WeekInfo,
        applicationContext: Context
    ): Int {
        val onClickShortcutIntent = Intent(
            applicationContext,
            MainActivity::class.java
        )

        val fullScreenIntent = Intent(
            applicationContext,
            ActivityLockScreenAlarm::class.java
        )

        val alarmReceivedIntent = Intent(
            applicationContext,
            AlarmBroadcastReceiver::class.java
        )

        val snoozeIntent = Intent(applicationContext, ActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(HOUR, hour)
            putExtra(MINUTE, minute)
        }

        val dismissIntent = Intent(applicationContext, ActionReceiver::class.java).apply {
            action = ACTION_DISMISS
        }

        val notificationDismissIntent = Intent(applicationContext, ActionReceiver::class.java).apply {
            action = ACTION_NOTIFICATION_DISMISS
        }

        fullScreenIntent.putExtra("SmplrText", "You did it, you crazy bastard you did it!")

        return smplrAlarmSet(applicationContext) {
            hour { hour }
            min { minute }
            contentIntent { onClickShortcutIntent }
            receiverIntent { fullScreenIntent }
            alarmReceivedIntent { alarmReceivedIntent }
            weekdays {
                if (weekInfo.monday) monday()
                if (weekInfo.tuesday) tuesday()
                if (weekInfo.wednesday) wednesday()
                if (weekInfo.thursday) thursday()
                if (weekInfo.friday) friday()
                if (weekInfo.saturday) saturday()
                if (weekInfo.sunday) sunday()
            }
            notification {
                alarmNotification {
                    smallIcon { R.drawable.ic_baseline_alarm_on_24 }
                    title { "Simple alarm is ringing" }
                    message { "Simple alarm is ringing"}
                    bigText { "Simple alarm is ringing"}
                    autoCancel { true }
                    firstButtonText { "Snooze" }
                    secondButtonText { "Dismiss" }
                    firstButtonIntent { snoozeIntent }
                    secondButtonIntent { dismissIntent }
                    notificationDismissedIntent { notificationDismissIntent }
                }
            }
            infoPairs {
                listOf(
                    "a" to "b",
                    "b" to "c",
                    "c" to "d"
                )
            }
        }
    }

    fun setNotificationAlarm(
        hour: Int,
        minute: Int,
        weekInfo: WeekInfo,
        applicationContext: Context
    ): Int {
        val alarmReceivedIntent = Intent(
            applicationContext,
            AlarmBroadcastReceiver::class.java
        )
        val onClickShortcutIntent = Intent(
            applicationContext,
            MainActivity::class.java
        )

        onClickShortcutIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        return smplrAlarmSet(applicationContext) {
            hour { hour }
            min { minute }
            weekdays {
                if (weekInfo.monday) monday()
                if (weekInfo.tuesday) tuesday()
                if (weekInfo.wednesday) wednesday()
                if (weekInfo.thursday) thursday()
                if (weekInfo.friday) friday()
                if (weekInfo.saturday) saturday()
                if (weekInfo.sunday) sunday()
            }
            contentIntent{ onClickShortcutIntent }
            alarmReceivedIntent { alarmReceivedIntent }
            notification {
                alarmNotification {
                    smallIcon { R.drawable.ic_baseline_alarm_on_24 }
                    title { "Simple alarm is ringing" }
                    message { "Simple alarm is ringing"}
                    bigText { "Simple alarm is ringing"}
                    autoCancel { true }
                    firstButtonText { "Snooze" }
                    secondButtonText { "Dismiss" }
                }
            }
        }
    }

    fun setNoNotificationAlarm(
        hour: Int,
        minute: Int,
        weekInfo: WeekInfo,
        applicationContext: Context
    ): Int {

        val fullScreenIntent = Intent(
            applicationContext,
            ActivityLockScreenAlarm::class.java
        )

        val alarmReceivedIntent = Intent(
            applicationContext,
            AlarmBroadcastReceiver::class.java
        )

        fullScreenIntent.putExtra("SmplrText", "You did it, you crazy bastard you did it!")

        return smplrAlarmSet(applicationContext) {
            hour { hour }
            min { minute }
            receiverIntent { fullScreenIntent }
            alarmReceivedIntent { alarmReceivedIntent }
            weekdays {
                if (weekInfo.monday) monday()
                if (weekInfo.tuesday) tuesday()
                if (weekInfo.wednesday) wednesday()
                if (weekInfo.thursday) thursday()
                if (weekInfo.friday) friday()
                if (weekInfo.saturday) saturday()
                if (weekInfo.sunday) sunday()
            }
            infoPairs {
                listOf(
                    "a" to "b",
                    "b" to "c",
                    "c" to "d"
                )
            }
        }
    }

    fun updateAlarm(
        requestCode: Int,
        hour: Int,
        minute: Int,
        weekInfo: WeekInfo,
        isActive: Boolean,
        applicationContext: Context
    ) {
        smplrAlarmUpdate(applicationContext) {
            requestCode { requestCode }
            hour { hour }
            min { minute }
            weekdays {
                if (weekInfo.monday) monday()
                if (weekInfo.tuesday) tuesday()
                if (weekInfo.wednesday) wednesday()
                if (weekInfo.thursday) thursday()
                if (weekInfo.friday) friday()
                if (weekInfo.saturday) saturday()
                if (weekInfo.sunday) sunday()
            }
            isActive { isActive }
        }
    }

    private fun setAlarm(hour: Int, minute: Int, date: LocalDate, message: String): Int {

        val onClickShortcutIntent = Intent(
            context,
            MainActivity::class.java
        )

        val fullScreenIntent = Intent(
            context,
            ActivityLockScreenAlarm::class.java
        )

        val alarmReceivedIntent = Intent(
            context,
            AlarmBroadcastReceiver::class.java
        )

        /*val snoozeIntent = Intent(context, SnoozeActionReceiver::class.java).apply {
            action = AlarmAction.ACTION_SNOOZE
            putExtra(HOUR, hour)
            putExtra(MINUTE, minute)
        }


        val dismissIntent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_DISMISS
        }*/

        val notificationDismissIntent = Intent(context, ActionReceiver::class.java).apply {
            action = AlarmAction.ACTION_NOTIFICATION_DISMISS
        }

        fullScreenIntent.putExtra("SmplrText", "You did it, you crazy bastard you did it!")

        val alarmId = smplrAlarmSet(context) {
            hour { hour }
            min { minute }
            date { date }
            weekdays {}
            contentIntent { onClickShortcutIntent }
            //receiverIntent { fullScreenIntent }
            alarmReceivedIntent { alarmReceivedIntent }
            notification {
                alarmNotification {
                    smallIcon { R.drawable.ic_baseline_alarm_on_24 }
                    title { "PennyWise" }
                    message { message }
                    bigText { "" }
                    autoCancel { true }
                    /*firstButtonText { "Snooze" }
                    secondButtonText { "Dismiss" }
                    firstButtonIntent { snoozeIntent }
                    secondButtonIntent { dismissIntent }*/
                    notificationDismissedIntent { notificationDismissIntent }
                }
            }
            notificationChannel {
                channel {
                    importance { NotificationManager.IMPORTANCE_HIGH }
                    showBadge { false }
                    name { "de.coldtea.smplr.alarm.channel" }
                    description { "This notification channel is created by SmplrAlarm" }
                }
            }
        }
        return alarmId
    }

    fun updateAlarm(alarmId: Int, hour: Int, minute: Int, date: LocalDate, isActive: Boolean) {

        smplrAlarmUpdate(context) {
            requestCode { alarmId }
            hour { hour }
            min { minute }
            date { date }
            weekdays {}
            isActive { isActive }
        }

        smplrAlarmUpdate(context) {
        }
    }


    fun cancelAlarm(alarmId: Int) {
        smplrAlarmCancel(context) {
            requestCode { alarmId }
        }
    }


    fun requestAlarmList() = smplrAlarmListRequestAPI.requestAlarmList()


    companion object{
        internal const val ACTION_SNOOZE = "action_snooze"
        internal const val ACTION_DISMISS = "action_dismiss"
        internal const val ACTION_NOTIFICATION_DISMISS = "action_notification_dismiss"
        internal const val HOUR = "hour"
        internal const val MINUTE = "minute"
    }
}