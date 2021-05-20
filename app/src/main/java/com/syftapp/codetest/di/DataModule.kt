package com.syftapp.codetest.di

import com.syftapp.codetest.data.database.AppDatabase
import com.syftapp.codetest.data.repository.BlogRepository
import org.koin.dsl.module

val dataModule = module {

    single { BlogRepository(get(), get(),) }

    single {AppDatabase(get())}

}