package ru.ok.cast_app.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.ok.cast_app.presentation.MainScreenViewModel

val appModule = module {
    viewModel<MainScreenViewModel> {
        MainScreenViewModel(castContext = get())
    }
}