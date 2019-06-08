package com.example.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.MediaController

class MusicController(context: Context) :
    MediaController.MediaPlayerControl, MediaController(context) {

    init {
        setPrevNextListeners({ ms.setNext() },
            { ms.setPrevious() })
        setAnchorView((context as MainActivity).findViewById(R.id.main_activity_layout))
        setMediaPlayer(this)
        isEnabled = true
    }

    constructor(context: Context, songs: List<Record>) : this(context) {
        this.songs = songs
    }


    lateinit var ms: MusicService
    private var isServiceBound: Boolean = false
    private lateinit var songs: List<Record>

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            connectService((service as MusicService.MusicBinder).getService(), songs)
            ms.mc = this@MusicController
        }

        override fun onServiceDisconnected(name: ComponentName) {
            disconnectService()
        }
    }

    fun songPicked(index: Int) {
        ms.setSong(index)
        ms.prepareSong()
        show()
    }

    override fun hide() {
    }

    override fun isPlaying(): Boolean {

        return if (isServiceBound) {
            ms.isPlaying()
        } else {
            false
        }
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        return if (isServiceBound && ms.isPlaying()) {
            ms.getDuration()
        } else {
            0
        }
    }

    override fun pause() {
        ms.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return if (isServiceBound && ms.isPlaying()) {
            ms.getDuration()
        } else {
            0
        }
    }

    override fun seekTo(pos: Int) {
        ms.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        return if (isServiceBound && ms.isPlaying()) {
            ms.getPosition()
        } else {
            0
        }
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        ms.start()
    }

    override fun getAudioSessionId(): Int {
        return MP_KEY_AUDIO_SESSION_ID
    }

    override fun canPause(): Boolean {
        return true
    }

    fun connectService(musicService: MusicService, songs: List<Record>) {
        this.ms = musicService
        ms.songs = songs
        isServiceBound = true
    }

    fun disconnectService() {
        isServiceBound = false
    }


    companion object {
        private const val MP_KEY_AUDIO_SESSION_ID = 665
    }
}