package com.example.freshrssreader.ui.screens.articles

import com.example.freshrssreader.data.model.Article
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedArticleHolder @Inject constructor() {
    var articles: List<Article> = emptyList()
}
