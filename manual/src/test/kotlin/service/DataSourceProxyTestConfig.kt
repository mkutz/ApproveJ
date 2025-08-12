package service

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.QueryExecutionListener
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import service.adapters.providing.database.article.ArticleStoreTest

@TestConfiguration
class DataSourceProxyTestConfig {

  @Component
  data class QueryCollector(private val queries: MutableList<String> = mutableListOf()) :
    QueryExecutionListener {

    override fun beforeQuery(execInfo: ExecutionInfo?, queryInfoList: List<QueryInfo>) {
      queries.addAll(queryInfoList.map { it.query })
    }

    override fun afterQuery(execInfo: ExecutionInfo?, queryInfoList: List<QueryInfo?>?) = Unit

    fun queries(): List<String> = queries

    fun reset() {
      queries.clear()
    }
  }

  @Component
  data class QueryExplanationCollector(
    private val originalDataSource: DataSource,
    private val queries: MutableList<String> = mutableListOf(),
  ) : QueryExecutionListener {

    override fun beforeQuery(execInfo: ExecutionInfo?, queryInfoList: List<QueryInfo>) {
      queries.addAll(queryInfoList.map { it.query })
    }

    override fun afterQuery(execInfo: ExecutionInfo?, queryInfoList: List<QueryInfo?>?) = Unit

    fun queries(): List<String> = queries

    fun reset() {
      queries.clear()
    }
  }

  @Bean
  @Primary
  fun dataSourceProxy(queryCollector: QueryCollector): DataSource {
    return ProxyDataSourceBuilder.create(originalDataSource())
      .name("datasourceProxy")
      .listener(queryCollector)
      .build()
  }

  @Bean
  fun originalDataSource(): DataSource {
    val container = ArticleStoreTest.postgresql
    return HikariDataSource().apply {
      jdbcUrl = container.jdbcUrl
      username = container.username
      password = container.password
      driverClassName = container.driverClassName
    }
  }
}
