package ru.ok.cast_app.di

import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext
import org.koin.dsl.module

val castModule = module {
    single<CastContext> {
        CastContext.getSharedInstance(get())
    }

    single<MediaRouter> {
        MediaRouter.getInstance(get())
    }
}