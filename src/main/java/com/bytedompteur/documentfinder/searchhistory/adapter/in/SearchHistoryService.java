package com.bytedompteur.documentfinder.searchhistory.adapter.in;

import java.util.List;

public interface SearchHistoryService {
    void addSearchQuery(String query);
    List<String> getSearchHistory();
}
