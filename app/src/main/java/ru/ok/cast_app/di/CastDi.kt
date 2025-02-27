package ru.ok.cast_app.di

import com.google.android.gms.cast.framework.CastContext
import org.koin.dsl.module

val castModule = module {
    single<CastContext> {
        CastContext.getSharedInstance(get())
    }
}