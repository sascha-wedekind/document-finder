package com.bytedompteur.documentfinder.searchhistory.dagger;

import com.bytedompteur.documentfinder.searchhistory.adapter.in.SearchHistoryService;
import com.bytedompteur.documentfinder.searchhistory.adapter.out.SearchHistoryFileRepository;
import com.bytedompteur.documentfinder.searchhistory.core.SearchHistoryRepository;
import com.bytedompteur.documentfinder.searchhistory.core.SearchHistoryServiceImpl;
import dagger.Binds;
import dagger.Module;
import jakarta.inject.Singleton;

@Module
public abstract class SearchHistoryModule {

    @Binds
    @Singleton
    // SearchHistoryServiceImpl is already annotated with @Singleton
    // SearchHistoryFileRepository is also annotated with @Singleton
    // This binding implies that the provided implementation (SearchHistoryServiceImpl)
    // will adhere to the @Singleton scope defined on itself.
    abstract SearchHistoryService bindSearchHistoryService(SearchHistoryServiceImpl impl);

    @Binds
    @Singleton
    // Similarly, this binding implies SearchHistoryFileRepository's @Singleton scope.
    abstract SearchHistoryRepository bindSearchHistoryRepository(SearchHistoryFileRepository impl);
}
