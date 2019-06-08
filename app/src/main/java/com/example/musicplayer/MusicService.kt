package com.example.musicplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.content.ContentUris

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    companion object {
        const val PLAYER_NOTIFICATION_ID = 666
    }

    lateinit var songs: List<Record>
    private lateinit var mp: MediaPlayer
    lateinit var mc: MusicController
    private var positon: Int = 0
    private val mb = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService

    }

    override fun onCreate() {
        super.onCreate()

        mp = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            @Suppress("DEPRECATION")
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnErrorListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp!!.start()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, MainActivity.MP_KEY_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(songs[positon].title)
            .setSmallIcon(R.drawable.image)
            .setContentText(songs[positon].author)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(PLAYER_NOTIFICATION_ID, notification)
        mc.show(0)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        if (mp.currentPosition > 0) {
            mediaPlayer!!.reset()
            setNext()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mb
    }

    override fun onUnbind(intent: Intent): Boolean {
        mp.stop()
        mp.release()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        mp.release()
        stopForeground(true)
    }

    fun prepareSong() {
        mp.reset()

        val songToPlayId = songs[positon].id
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songToPlayId
        )

        try {
            mp.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
        }

        mp.prepare()
    }

    fun setSong(songIndex: Int) {
        positon = songIndex
    }

    fun getPosition(): Int {
        return mp.currentPosition
    }

    fun getDuration(): Int {
        return mp.duration
    }

    fun isPlaying(): Boolean {
        return mp.isPlaying
    }

    fun pausePlayer() {
        mp.pause()
    }

    fun seek(position: Int) {
        mp.seekTo(position)
    }

    fun start() {
        mp.start()
    }

    fun setPrevious() {
        positon--
        if (positon < 0) {
            positon = songs.size - 1
        }
        prepareSong()
    }

    fun setNext() {
        positon++
        if (positon >= songs.size) {
            positon = 0
        }
        prepareSong()
    }

}