package com.example.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val myPermissionReadExternalStorage = 667
    private lateinit var records: List<Record>
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var recyclerLayoutManager: LinearLayoutManager

    lateinit var mc: MusicController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        queryDevice()

        recyclerLayoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(records)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = recyclerLayoutManager
            adapter = songAdapter
        }

        mc = MusicController(this, records)
        createNotificationChannel()
    }

    override fun onStart() {
        super.onStart()

        Intent(this, MusicService::class.java).also { playIntent ->
            bindService(playIntent, mc.serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mc.serviceConnection)
        mc.disconnectService()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                myPermissionReadExternalStorage
            )
        } else {
        }
    }

    @SuppressLint("Recycle")
    private fun queryDevice() {
        val foundSongs: MutableList<Record> = mutableListOf()
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val columns = arrayOf(
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST,
            android.provider.MediaStore.Audio.Media.DATA

        )

        val selectionClause = "${android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC} = 1"
        val musicCursor = musicResolver.query(musicUri, columns, selectionClause, null, null)!!

        if (musicCursor.moveToFirst()) {
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val authorColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            val pathColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)

            do {
                val id = musicCursor.getLong(idColumn)
                val title = musicCursor.getString(titleColumn)
                val author = musicCursor.getString(authorColumn)
                val path = musicCursor.getString(pathColumn)
                foundSongs.add(Record(id, title, author, path))

            } while (musicCursor.moveToNext())
        }

        records = foundSongs.toList()
    }

    private fun createNotificationChannel() {
        val name = MP_KEY_NOTIFICATION_CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(MP_KEY_NOTIFICATION_CHANNEL_ID, name, importance)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            myPermissionReadExternalStorage -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    finish()
                }
                return
            }
            else -> {
            }
        }
    }

    companion object {
        const val MP_KEY_NOTIFICATION_CHANNEL_ID = "NOTIF_CHANNEL_ID"
        private const val MP_KEY_NOTIFICATION_CHANNEL_NAME = "PLAYER_CHANNEL"
    }

}
