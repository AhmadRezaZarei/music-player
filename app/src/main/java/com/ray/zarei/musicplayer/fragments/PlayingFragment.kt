package com.ray.zarei.musicplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ray.zarei.musicplayer.MusicPlayerRemote
import com.ray.zarei.musicplayer.R


class PlayingFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_playing, container, false)


        view.findViewById<View>(R.id.btn_next).setOnClickListener {
            MusicPlayerRemote.playNextSong()
        }

        view.findViewById<View>(R.id.btn_prev).setOnClickListener {
            MusicPlayerRemote.playPreviousSong()
        }

        view.findViewById<View>(R.id.btn_play_pause).setOnClickListener {
            MusicPlayerRemote.togglePlayPauseSong()
        }

        return view
    }

    companion object {
        fun newInstance() =
            PlayingFragment()
    }
}