package de.coldtea.smplr.alarm.alarms

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.coldtea.smplr.alarm.alarms.models.AlarmInfo
import de.coldtea.smplr.alarm.alarms.models.WeekInfo
import de.coldtea.smplr.alarm.extensions.getTimeFormattedString
import de.coldtea.smplr.alarm.extensions.nowPlus
import de.coldtea.smplr.alarm.lockscreenalarm.ActivityLockScreenAlarm
import de.coldtea.smplr.smplralarm.*
import de.coldtea.smplr.smplralarm.managers.AlarmListRequestManager
import de.coldtea.smplr.smplralarm.managers.SmplrAlarmManager
import timber.log.Timber
import java.util.*

class AlarmViewModel : ViewModel() {

    private val cal = Calendar.getInstance()
    private val now = cal.getTimeFormattedString("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    lateinit var alarmListRequestManager: AlarmListRequestManager

    private val _alarmListAsJson = MutableLiveData<String>()
    val alarmListAsJson: LiveData<String>
        get() = _alarmListAsJson


    fun initAlarmListListener(applicationContext: Context) =
        smplrAlarmChangeOrRequestListener(applicationContext) {
            _alarmListAsJson.postValue(it)
        }.also {
            alarmListRequestManager = it
        }

    fun setAlarm(hour: Int, minute: Int, weekInfo: WeekInfo, applicationContext: Context): Int {
        val intent = Intent(
            applicationContext,
            ActivityLockScreenAlarm::class.java
        )

        intent.putExtra("SmplrText", "You did it, you crazy bastard you did it!")

        return smplrAlarmSet(applicationContext) {
            hour { hour }
            min { minute }
            fullScreenIntent {
                intent
            }
            onAlarmRings { alarmId ->
                Timber.i("SmplrAlarmApp.MainFragment.onAlarmRings: $alarmId")
            }
            weekdays {
                if (weekInfo.monday) monday()
                if (weekInfo.tuesday) tuesday()
                if (weekInfo.wednesday) wednesday()
                if (weekInfo.thursday) thursday()
                if (weekInfo.friday) friday()
                if (weekInfo.saturday) saturday()
                if (weekInfo.sunday) sunday()
            }
        }
    }

    fun updateRepeatingAlarm(
        requestCode: Int,
        hour: Int,
        minute: Int,
        weekInfo: WeekInfo,
        isActive: Boolean,
        applicationContext: Context
    ) {
        smplrAlarmUpdateRepeatingAlarm(applicationContext) {
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

    fun updateSingleAlarm(
        requestCode: Int,
        hour: Int,
        minute: Int,
        isActive: Boolean,
        applicationContext: Context
    ) {
        smplrAlarmUpdateSingleAlarm(applicationContext) {
            requestCode { requestCode }
            hour { hour }
            min { minute }
            isActive { isActive }
        }
    }

    fun requestAlarmList() = alarmListRequestManager.requestAlarmList()

    private fun createBasicNotificationWithFullScreenIntent(
        timePair: Pair<Int, Int>,
        intent: Intent,
        applicationContext: Context
    ) = smplrAlarmSet(applicationContext) {
        hour { timePair.first }
        min { timePair.second }
        fullScreenIntent {
            intent
        }
        onAlarmRings { alarmId ->
            Timber.i("SmplrAlarmApp.MainFragment.onAlarmRings: $alarmId")
        }
    }

    fun cancelAlarm(requestCode: Int, applicationContext: Context) =
        smplrAlarmCancel(applicationContext) {
            requestCode { requestCode }
        }

}