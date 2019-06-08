package com.example.musicplayer

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(private val records: List<Record>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.record_row, parent, false)

        return SongViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(records[position])
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                (itemView.context as MainActivity).mc.songPicked(adapterPosition)
            }
        }

        private val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        private val songAuthor: TextView = itemView.findViewById(R.id.songAuthor)
        private val songCover: ImageView = itemView.findViewById(R.id.songCover)

        fun bind(song: Record) {
            songTitle.text = song.title
            songAuthor.text = song.author
            loadCover(song.path, songCover)
        }

        private fun loadCover(path: String, image: ImageView) {
            val dataRetriever = MediaMetadataRetriever()
            dataRetriever.setDataSource(path)
            val cover = dataRetriever.embeddedPicture
            if (cover != null) {
                val coverImage = BitmapFactory.decodeByteArray(cover, 0, cover.size)
                image.setImageBitmap(coverImage)
            } else {
                image.setImageResource(R.drawable.image)
            }
        }
    }


}