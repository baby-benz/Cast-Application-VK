package ru.ok.cast_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.startKoin
import ru.ok.cast_app.di.appModule
import ru.ok.cast_app.di.castModule
import ru.ok.cast_app.presentation.CastStatus
import ru.ok.cast_app.presentation.MainScreenViewModel
import ru.ok.cast_app.ui.theme.CastApplicationOKruTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainScreenViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule, castModule)
        }
        setContent {
            CastApplicationOKruTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                value = viewModel.url,
                onValueChange = {
                    viewModel.handleUrlChange(it)
                },
                supportingText = {
                    if (!viewModel.urlCorrect) {
                        Text(
                            text = "URL имеет неправильный формат",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (!viewModel.urlCorrect) {
                        Icon(
                            Icons.Default.Warning,
                            "Неверный URL",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                label = {
                    Text("Ссылка на видео")
                }
            )

            if (viewModel.deviceName == null) {
                LinearProgressIndicator()
            }

            if (viewModel.deviceName != null
                && (viewModel.castStatus == CastStatus.NOT_READY || viewModel.castStatus == CastStatus.CONNECTING)) {
                IconButton(
                    enabled = viewModel.deviceName != null,
                    onClick = {
                        viewModel.refreshDevices()
                    }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Обновление списка устройств")
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.clickable { viewModel.connect() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = null
                    )
                    Text(text = viewModel.deviceName.toString())
                    if (viewModel.castStatus == CastStatus.CONNECTING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp, 20.dp),
                        )
                    }
                }
            }
        }


        Column(
            modifier.padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (viewModel.deviceName != null
                && (viewModel.castStatus != CastStatus.NOT_READY || viewModel.castStatus != CastStatus.CONNECTING)) {
                Text("Casting to: " + viewModel.deviceName)
            }
            FilledTonalButton(
                onClick = {
                    viewModel.startCast()
                },
                enabled = viewModel.castStatus == CastStatus.READY,
                shape = RoundedCornerShape(20),
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Text("Cast")
            }

            IconButton(
                enabled = viewModel.castStatus == CastStatus.PLAYING || viewModel.castStatus == CastStatus.PAUSED,
                onClick = {
                    if (viewModel.castStatus == CastStatus.PLAYING) {
                        viewModel.pause()
                    } else if (viewModel.castStatus == CastStatus.PAUSED) {
                        viewModel.play()
                    }
                }) {
                Icon(
                    if (viewModel.castStatus == CastStatus.PLAYING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
                    contentDescription = "Пауза или запуск проигрывания"
                )
            }
            LinearProgressIndicator(
                progress = { viewModel.currentPlayProgress }
            )
        }
    }
}