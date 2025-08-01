package com.posite.my_alarm.di

import android.app.AlarmManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.posite.my_alarm.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {
    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideMediaPlayer(@ApplicationContext context: Context): MediaPlayer {
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            setDataSource(
                context,
                Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(context.resources.getResourcePackageName(R.raw.f1_radio_notification_made_with_voicemod))
                    .appendPath(context.resources.getResourceTypeName(R.raw.f1_radio_notification_made_with_voicemod))
                    .appendPath(context.resources.getResourceEntryName(R.raw.f1_radio_notification_made_with_voicemod))
                    .build()
            )
            isLooping = false
            setOnCompletionListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    this.seekTo(0)
                    this.start()
                }, 1000)
            }
        }
    }
}