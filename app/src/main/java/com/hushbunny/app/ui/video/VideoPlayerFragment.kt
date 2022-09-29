package com.hushbunny.app.ui.video

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.databinding.FragmentVideoPlayerBinding
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.uitls.APIConstants

class VideoPlayerFragment : Fragment() {

    private var videoPlayerBinding: FragmentVideoPlayerBinding? = null
    private val args: VideoPlayerFragmentArgs by navArgs()
    private var currentVideoPosition = 0

    companion object {
        private const val PLAYBACK_TIME = "playbackTime"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videoPlayerBinding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return videoPlayerBinding?.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PLAYBACK_TIME, currentVideoPosition)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            currentVideoPosition = savedInstanceState.getInt(PLAYBACK_TIME)
        }
        initializePlayer(if (args.isLocal) "file://${args.url}" else "${APIConstants.VIDEO_BASE_URL}${args.url}")
        videoPlayerBinding?.actionCloseImage?.setOnClickListener { onBackPressed() }
        val controller = MediaController(context)
        controller.setMediaPlayer(videoPlayerBinding?.productDescriptionVideoView)
        videoPlayerBinding?.productDescriptionVideoView?.setMediaController(controller)
    }

    private fun onBackPressed() {
        activity?.onBackPressed()
    }

    private fun initializePlayer(videoUrl: String) {
        videoPlayerBinding?.loadingProgressBar?.visibility = VideoView.VISIBLE
        val videoUri = getMedia(videoUrl)
        videoPlayerBinding?.productDescriptionVideoView?.setVideoURI(videoUri)
        videoPlayerBinding?.productDescriptionVideoView?.setOnPreparedListener {
            videoPlayerBinding?.loadingProgressBar?.visibility = VideoView.INVISIBLE
            if (currentVideoPosition > 0) {
                videoPlayerBinding?.productDescriptionVideoView?.seekTo(currentVideoPosition)
            } else {
                videoPlayerBinding?.productDescriptionVideoView?.seekTo(0)
            }
            videoPlayerBinding?.productDescriptionVideoView?.start()
        }

        videoPlayerBinding?.productDescriptionVideoView?.setOnCompletionListener {
            videoPlayerBinding?.productDescriptionVideoView?.seekTo(0)
        }
    }

    // Release all media-related resources.
    private fun releasePlayer() {
        currentVideoPosition = videoPlayerBinding?.productDescriptionVideoView?.currentPosition ?: 0
        videoPlayerBinding?.productDescriptionVideoView?.stopPlayback()
    }

    // Get a Uri for the media
    private fun getMedia(mediaName: String): Uri {
        return Uri.parse(mediaName)
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    override fun onDestroyView() {
        videoPlayerBinding?.productDescriptionVideoView?.apply {
            stopPlayback()
            suspend()
            setOnCompletionListener(null)
            setOnPreparedListener(null)
        }
        super.onDestroyView()
        videoPlayerBinding = null
    }
}
