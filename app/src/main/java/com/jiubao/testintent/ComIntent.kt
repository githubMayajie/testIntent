package com.jiubao.testintent

import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import java.util.*

/**
 * 功能描述:
 * 作者： Administrator 时间： 2018/4/11.
 * 版本:
 */
object ComIntent{
    //<uses-permission android:name="com.android.alarm.permission.SET_ALARM" />
    fun createAlarm(message:String,hour:Int,miunte:Int):Intent{
        return Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE,message)
            putExtra(AlarmClock.EXTRA_HOUR,hour)
            putExtra(AlarmClock.EXTRA_MINUTES,miunte)
        }
    }


    fun showAllArarm():Intent{
        return Intent(AlarmClock.ACTION_SHOW_ALARMS)
    }

    fun addCalendar(title:String,location:String,begin:Calendar,end:Calendar):Intent{
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE,title)
            putExtra(CalendarContract.Events.EVENT_LOCATION,location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,begin)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME,end)
        }
    }

    fun getCapture(mLocationForPhotos:Uri,fileName:String):Intent{
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT,Uri.withAppendedPath(mLocationForPhotos,fileName))
        }
    }
}