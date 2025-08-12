package service.adapters.demanding.restapi.shoppingcart

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpRequest.newBuilder
import java.net.http.HttpResponse.BodyHandlers
import org.approvej.ApprovalBuilder.approve
import org.approvej.ApprovalError
import org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter
import org.approvej.scrub.Scrubbers.uuids
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import service.adapters.demanding.restapi.toCents
import service.application.article.ArticleBuilder.Companion.anArticle
import service.application.article.ToStoreArticles
import service.application.shoppingcart.ItemBuilder.Companion.anItem
import service.application.shoppingcart.ShoppingCartBuilder.Companion.aShoppingCart
import service.application.shoppingcart.ToStoreShoppingCarts

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerApiTest(
  @param:Value("http://localhost:\${local.server.port}") val baseUrl: String,
  @param:Autowired val shoppingCartStore: ToStoreShoppingCarts,
  @param:Autowired val articleStore: ToStoreArticles,
  @param:Autowired val objectMapper: ObjectMapper,
) {

  val httpClient: HttpClient = HttpClient.newHttpClient()

  @Test
  fun `GET new shopping cart`() {
    // tag::get_new_shopping_cart_act[]
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart")).GET().build(),
        BodyHandlers.ofString(),
      )
    // end::get_new_shopping_cart_act[]

    // tag::get_new_shopping_cart_assert[]
    assertThat(response.statusCode()).isEqualTo(200)
    val receivedShoppingCart = objectMapper.readValue<ShoppingCartDto>(response.body())
    assertThat(receivedShoppingCart).isNotNull()
    assertThat(receivedShoppingCart.id).isNotNull()
    assertThat(receivedShoppingCart.value).isEqualTo(0)
    assertThat(receivedShoppingCart.items).isEmpty()
    // end::get_new_shopping_cart_assert[]
  }

  @Test
  fun `GET new shopping cart approve`() {
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart")).GET().build(),
        BodyHandlers.ofString(),
      )

    assertThatExceptionOfType(ApprovalError::class.java).isThrownBy {
      // tag::get_new_shopping_cart_approve_simple[]
      assertThat(response.statusCode()).isEqualTo(200)
      approve(response.body()).byFile()
      // end::get_new_shopping_cart_approve_simple[]
    }
  }

  @Test
  fun `GET new shopping cart approve scrubbed`() {
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart")).GET().build(),
        BodyHandlers.ofString(),
      )

    // tag::get_new_shopping_cart_approve_scrubbed[]
    assertThat(response.statusCode()).isEqualTo(200)
    approve(response.body()).scrubbedOf(uuids()).byFile()
    // end::get_new_shopping_cart_approve_scrubbed[]
  }

  @Test
  fun `GET existing shopping cart`() {
    // tag::get_existing_shopping_cart_arrange[]
    val existingShoppingCart =
      shoppingCartStore.storeShoppingCart(
        aShoppingCart().items(anItem().build(), anItem().build()).build()
      )
    // end::get_existing_shopping_cart_arrange[]

    // tag::get_existing_shopping_cart_act[]
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart/${existingShoppingCart.id}")).GET().build(),
        BodyHandlers.ofString(),
      )
    // end::get_existing_shopping_cart_act[]

    // tag::get_existing_shopping_cart_assert[]
    assertThat(response.statusCode()).isEqualTo(200)
    val receivedShoppingCart = objectMapper.readValue<ShoppingCartDto>(response.body())
    assertThat(receivedShoppingCart).isNotNull()
    assertThat(receivedShoppingCart.id).isEqualTo(existingShoppingCart.id.toString())
    assertThat(receivedShoppingCart.value).isPositive()
    // end::get_existing_shopping_cart_assert[]
  }

  @Test
  fun `GET existing shopping cart approve scrubbed`() {
    // tag::get_existing_shopping_cart_arrange[]
    val existingShoppingCart =
      shoppingCartStore.storeShoppingCart(
        aShoppingCart().items(anItem().build(), anItem().build()).build()
      )
    // end::get_existing_shopping_cart_arrange[]

    // tag::get_existing_shopping_cart_act[]
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart/${existingShoppingCart.id}")).GET().build(),
        BodyHandlers.ofString(),
      )
    // end::get_existing_shopping_cart_act[]

    // tag::get_existing_shopping_cart_assert[]
    assertThat(response.statusCode()).isEqualTo(200)
    approve(response.body()).scrubbedOf(uuids()).byFile()
    // end::get_existing_shopping_cart_assert[]
  }

  @Test
  fun `GET existing shopping cart approve scrubbed printed`() {
    // tag::get_existing_shopping_cart_arrange[]
    val existingShoppingCart =
      shoppingCartStore.storeShoppingCart(
        aShoppingCart().items(anItem().build(), anItem().build()).build()
      )
    // end::get_existing_shopping_cart_arrange[]

    // tag::get_existing_shopping_cart_act[]
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart/${existingShoppingCart.id}")).GET().build(),
        BodyHandlers.ofString(),
      )
    // end::get_existing_shopping_cart_act[]

    // tag::get_existing_shopping_cart_assert[]
    assertThat(response.statusCode()).isEqualTo(200)
    approve(response.body()).printWith(jsonStringPrettyPrinter()).scrubbedOf(uuids()).byFile()
    // end::get_existing_shopping_cart_assert[]
  }

  @Test
  fun `POST shopping cart items approve`() {
    val existingShoppingCart = shoppingCartStore.storeShoppingCart(aShoppingCart().build())
    val article = articleStore.storeArticle(anArticle().build())

    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart/${existingShoppingCart.id}/items"))
          .POST(BodyPublishers.ofString("""{"articleId":"${article.id}","quantity": 1}"""))
          .header("Content-Type", "application/json")
          .build(),
        BodyHandlers.ofString(),
      )

    // tag::post_shopping_cart_items_approve[]
    assertThat(response.statusCode()).isEqualTo(200)
    approve(response.body()).scrubbedOf(uuids()).printWith(jsonStringPrettyPrinter()).byFile()
    // end::post_shopping_cart_items_approve[]
  }

  @Test
  fun `POST shopping cart items`() {
    // tag::post_shopping_cart_items_arrange[]
    val existingShoppingCart = shoppingCartStore.storeShoppingCart(aShoppingCart().build())
    val article = articleStore.storeArticle(anArticle().build())
    // end::post_shopping_cart_items_arrange[]

    // tag::post_shopping_cart_items_act[]
    val response =
      httpClient.send(
        newBuilder(URI("$baseUrl/shoppingcart/${existingShoppingCart.id}/items"))
          .POST(BodyPublishers.ofString("""{"articleId":"${article.id}","quantity": 1}"""))
          .header("Content-Type", "application/json")
          .build(),
        BodyHandlers.ofString(),
      )
    // end::post_shopping_cart_items_act[]

    // tag::post_shopping_cart_items_assert[]
    assertThat(response.statusCode()).isEqualTo(200)
    val receivedShoppingCart = objectMapper.readValue<ShoppingCartDto>(response.body())
    assertThat(receivedShoppingCart).isNotNull()
    assertThat(receivedShoppingCart.id).isEqualTo(existingShoppingCart.id.toString())
    assertThat(receivedShoppingCart.value).isEqualTo(article.price.toCents())
    assertThat(receivedShoppingCart.items).hasSize(1)
    // end::post_shopping_cart_items_assert[]
  }

  companion object {
    @Container
    @ServiceConnection
    @JvmStatic
    val postgresql: PostgreSQLContainer<Nothing> =
      PostgreSQLContainer<Nothing>("postgres:latest").waitingFor(forListeningPort())

    @Container @ServiceConnection @JvmStatic val kafka = KafkaContainer("apache/kafka-native:3.8.0")
  }
}
