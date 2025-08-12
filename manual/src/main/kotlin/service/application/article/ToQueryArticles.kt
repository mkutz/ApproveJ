package service.application.article

interface ToQueryArticles {

  fun queryArticles(query: String): List<Article>
}
