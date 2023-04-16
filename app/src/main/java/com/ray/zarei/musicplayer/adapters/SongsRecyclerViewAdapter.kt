package com.ray.zarei.musicplayer.adapters


import android.content.ContentUris
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ray.zarei.musicplayer.R
import com.ray.zarei.musicplayer.Song

class SongsRecyclerViewAdapter(private val songs: ArrayList<Song>, private val onItemClickListener: ((Int)->Unit)): RecyclerView.Adapter<SongsRecyclerViewAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false))

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        songs[position].let {
            holder.tvTitle.text = it.title
            holder.tvArtist.text = it.artistName
        }

        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")

        val coverUri = ContentUris.withAppendedId(sArtworkUri, songs[position].albumId)

        Glide.with(holder.itemView.context)
            .load(coverUri)
            .placeholder(R.drawable.default_album_art)
            .error(R.drawable.default_album_art)
            .centerCrop()
            .into(holder.ivSong)


        holder.itemView.setOnClickListener {
            this.onItemClickListener(position)
        }

    }

    class SongViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val tvTitle = v.findViewById<TextView>(R.id.tv_title)
        val tvArtist = v.findViewById<TextView>(R.id.tv_artist)
        val ivSong = v.findViewById<ImageView>(R.id.iv_song)
    }
}