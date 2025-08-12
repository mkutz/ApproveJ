package service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ShoppingCartApplication

fun main(args: Array<String>) {
  @Suppress("SpreadOperator") runApplication<ShoppingCartApplication>(*args)
}
