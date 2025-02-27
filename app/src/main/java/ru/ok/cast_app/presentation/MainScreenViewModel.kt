package ru.ok.cast_app.presentation

import android.media.MediaRouter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient

data class Device(val id: String, val name: String?)

class MainScreenViewModel(
    private val castContext: CastContext,
    private val mediaRouter: MediaRouter
) : ViewModel() {
    private val httpUrlRegex =
        Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")

    var searchInProgress by mutableStateOf(false)
    var readyToCast by mutableStateOf(false)
    var casting by mutableStateOf(false)

    var currentPlayProgress by mutableFloatStateOf(0f)

    var deviceToCast by mutableStateOf<Device?>(null)

    private lateinit var url: String
    private lateinit var mediaClient: RemoteMediaClient

    private var connected: Boolean = false

    fun handleUrlChange(url: String) {
        var urlToCheck = url

        if (!urlToCheck.startsWith("http")) {
            urlToCheck = "https://".plus(url)
        }

        if (httpUrlRegex.matches(urlToCheck)) {
            this.url = url
            if (connected) {
                readyToCast = true
            }
        }
    }

    fun refreshDevices() {
        mediaRouter.addCallback(MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build(),
            mediaRouterCallBack(),
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN
        )
    }

    fun startCast() {
        val videoMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, url)
        }

        val mediaInfo = MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(videoMetadata)

        val mediaLoadRequestData = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo.build())
            .setAutoplay(true)

        mediaClient.registerCallback(mediaClientCallback())
        mediaClient?.load(mediaLoadRequestData.build())
    }

    fun startPlay() {

    }

    fun stopPlay() {

    }

    private fun castStateListener() = CastStateListener {
        when (it) {
            CastState.CONNECTED -> {
                val castSession = castContext.sessionManager.currentCastSession

                if (castSession == null) {
                    readyToCast = false
                } else {
                    mediaClient = castSession.remoteMediaClient!!
                    mediaClient.stop()
                    mediaClient.registerCallback(mediaClientCallback())
                    readyToCast = true
                    connected = true
                }
            }
        }
    }

    private fun mediaClientCallback() = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            mediaClient.addProgressListener(progressListener(), 200)
        }
    }

    private fun progressListener() =  RemoteMediaClient.ProgressListener { position: Long, duration: Long ->
        if (duration <= 0) return@ProgressListener

        if (position > 0) {
            currentPlayProgress = position.toFloat() / duration
        }
    }

    private fun mediaRouterCallBack() = object: MediaRouter.Callback() {
        override fun onRouteSelected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?
        ) {
            TODO("Not yet implemented")
        }

        override fun onRouteUnselected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?
        ) {
            TODO("Not yet implemented")
        }

        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            mediaRouter.removeCallback(mediaRouterCallBack())
        }

        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            device = Device(route.id, route.name)
        }

        override fun onRouteGrouped(
            router: MediaRouter?,
            info: MediaRouter.RouteInfo?,
            group: MediaRouter.RouteGroup?,
            index: Int
        ) {
            TODO("Not yet implemented")
        }

        override fun onRouteUngrouped(
            router: MediaRouter?,
            info: MediaRouter.RouteInfo?,
            group: MediaRouter.RouteGroup?
        ) {
            super.onRouteUngrouped(router, info, group)
        }

        override fun onRouteVolumeChanged(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            TODO("Not yet implemented")
        }

        override fun onRouteSelected(
            router: MediaRouter,
            selectedRoute: MediaRouter.RouteInfo,
            reason: Int,
            requestedRoute: MediaRouter.RouteInfo
        ) {
            mediaRouter = router

        }
    }

    /* fun startSearch(){
         search.start()

         val selector = MediaRouteSelector.Builder()
             // These are the framework-supported intents
             //.addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
             //.addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
             .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
             .build()

         mediaRouter.addCallback(selector, mediaRouterCallBack(), MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)


         deviceState = CastDeviceState.SEARCHING
     }*/
}