package com.bytedompteur.documentfinder.searchhistory.core;

import java.util.List;

public interface SearchHistoryRepository {
    void save(List<String> queries);
    List<String> load();
}
