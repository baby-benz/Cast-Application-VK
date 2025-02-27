package ru.ok.cast_app.presentation

import androidx.mediarouter.media.MediaRouter
import androidx.annotation.UiThread
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
import com.google.android.gms.common.api.Status

enum class CastStatus {
    NOT_READY,
    CONNECTING,
    READY,
    PLAYING,
    PAUSED
}

class MainScreenViewModel(
    private val castContext: CastContext,
    private val mediaRouter: MediaRouter
) : ViewModel() {
    init {
        castContext.addCastStateListener(castStateListener())
        mediaRouter.addCallback(
            MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build(),
            mediaRouterCallBack(),
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN
        )
    }

    var urlCorrect by mutableStateOf(true)
    var url by mutableStateOf("https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5")
    var castStatus by mutableStateOf(CastStatus.NOT_READY)
    var deviceName by mutableStateOf<String?>(null)
    var currentPlayProgress by mutableFloatStateOf(0f)

    private val httpUrlRegex =
        Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")

    private lateinit var mediaClient: RemoteMediaClient
    private lateinit var selectedRoute: MediaRouter.RouteInfo

    private var connected: Boolean = false

    fun handleUrlChange(url: String) {
        var urlToCheck = url

        if (!urlToCheck.startsWith("http")) {
            urlToCheck = "https://".plus(url)
        }

        this.url = url

        if (httpUrlRegex.matches(urlToCheck)) {
            urlCorrect = true
            if (connected) {
                castStatus = CastStatus.READY
            }
        } else {
            urlCorrect = false
            castStatus = CastStatus.NOT_READY
        }
    }

    @UiThread
    fun connect() {
        mediaRouter.selectRoute(selectedRoute)
        castStatus = CastStatus.CONNECTING
    }

    @UiThread
    fun refreshDevices() {
        deviceName = null
        mediaRouter.addCallback(
            MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build(),
            mediaRouterCallBack(),
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN
        )
    }

    @UiThread
    fun startCast() {
        val videoMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, url)
        }

        val mediaInfo = MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(videoMetadata)

        val mediaLoadRequestData = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo.build())

        mediaClient.addProgressListener(progressListener(), 200)
        mediaClient.load(mediaLoadRequestData.build()).addStatusListener {
            if (it == Status.RESULT_SUCCESS) {
                castStatus = CastStatus.PLAYING
            }
        }
    }

    @UiThread
    fun play() {
        mediaClient.play().addStatusListener {
            if (it == Status.RESULT_SUCCESS) {
                castStatus = CastStatus.PLAYING
            }
        }
    }

    @UiThread
    fun pause() {
        mediaClient.pause().addStatusListener {
            if (it == Status.RESULT_SUCCESS) {
                castStatus = CastStatus.PAUSED
            }
        }
    }

    @UiThread
    private fun castStateListener() = CastStateListener {
        if (it == CastState.CONNECTED) {
            val castSession = castContext.sessionManager.currentCastSession

            if (castSession == null) {
                castStatus = CastStatus.NOT_READY
            } else {
                mediaClient = castSession.remoteMediaClient!!
                mediaClient.stop()
                connected = true
                if (urlCorrect) {
                    castStatus = CastStatus.READY
                }
            }
        }
    }

    private fun progressListener() =
        RemoteMediaClient.ProgressListener { position: Long, duration: Long ->
            if (duration <= 0) return@ProgressListener

            if (position > 0) {
                currentPlayProgress = position.toFloat() / duration
            }
        }

    private fun mediaRouterCallBack() = object : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            mediaRouter.removeCallback(this)
            deviceName = route.name
            selectedRoute = route
        }

        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
            if (route.name == deviceName) {
                deviceName = null
            }
        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            deviceName = route.name
            selectedRoute = route
        }
    }
}