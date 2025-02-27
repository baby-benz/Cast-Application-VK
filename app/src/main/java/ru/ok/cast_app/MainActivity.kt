package ru.ok.cast_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        var videoUrl by remember {
            mutableStateOf("https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5")
        }
        var isUrlCorrect by remember {
            mutableStateOf(true)
        }

        Box(
            modifier,
            contentAlignment = Alignment.TopCenter
        ) {
            TextField(
                value = videoUrl,
                onValueChange = {
                    isUrlCorrect = viewModel.handleUrlChange(it)
                    videoUrl = it
                },
                supportingText = {
                    if (!isUrlCorrect) {
                        Text(
                            text = "URL имеет неправильный формат",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (!isUrlCorrect) {
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

            IconButton(
                enabled = viewModel.searchInProgress,
                onClick = {
                    viewModel.refreshDevices()
                }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Обновление списка устройств")
            }

            DeviceItem(device = currentDevice, icon = {
                when(currentDevice) {
                    is ChromeCastDevice -> Icon(imageVector = Icons.Default.Cast, contentDescription = null)
                    is SamsungDevice -> Icon(imageVector = Icons.Default.Tv, contentDescription = null)
                }
            }, loading = true)

            Box(
                modifier = modifier.padding(bottom = 10.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FilledTonalButton(
                    onClick = {
                        viewModel.castVideo()
                    },
                    enabled = isUrlCorrect && viewModel.readyToCast,
                    shape = RoundedCornerShape(20),
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text("Cast")
                }
            }

            Column(
                modifier = modifier
            ) {
                IconButton(
                    enabled = viewModel.readyToCast,
                    onClick = {
                        if (viewModel.casting) viewModel.pause() else viewModel.play()
                    }) {
                    Icon(
                        if (viewModel.casting) Icons.Default.else Icons . Filled . PlayArrow,
                        contentDescription = "Localized description"
                    )
                }
                LinearProgressIndicator(
                    progress = { viewModel.currentPlayTime.toFloat() }
                )
            }
        }
    }
}