package com.posite.my_alarm.util

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.posite.my_alarm.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSoundPlayer @Inject constructor(
    // 생성자에서 Context를 주입받습니다.
    @ApplicationContext private val context: Context
) {

    private var mediaPlayer: MediaPlayer? = null

    fun play() {
        if (mediaPlayer?.isPlaying == true) {
            return
        }
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            val uri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.packageName)
                .appendPath(context.resources.getResourceTypeName(R.raw.f1_radio_notification_made_with_voicemod))
                .appendPath(context.resources.getResourceEntryName(R.raw.f1_radio_notification_made_with_voicemod))
                .build()
            setDataSource(context, uri)
            isLooping = true
            setOnPreparedListener {
                it.start()
            }
            prepareAsync()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}