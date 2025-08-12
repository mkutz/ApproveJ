package service.application.shoppingcart

import org.approvej.ApprovalBuilder.approve
import org.approvej.ApprovalError
import org.approvej.print.ObjectPrinter.objectPrinter
import org.approvej.scrub.Scrubbers.uuids
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import service.application.article.ArticleBuilder.Companion.anArticle

class ItemTest {

  @Test
  fun `constructor article quantity`() {
    // tag::arrange[]
    val article = anArticle().build()
    val quantity = 1
    // end::arrange[]

    // tag::act[]
    val item = Item(article, quantity)
    // end::act[]

    // tag::assert[]
    assertThat(item.id).isNotNull
    assertThat(item.articleId).isEqualTo(article.id)
    assertThat(item.title).isEqualTo(article.title)
    assertThat(item.pricePerPiece).isEqualTo(article.price)
    assertThat(item.imageUrl).isEqualTo(article.imageUrl)
    assertThat(item.priceTotal).isEqualTo(article.price)
    assertThat(item.quantity).isEqualTo(quantity)
    // end::assert[]
  }

  @Test
  fun `constructor article quantity approve`() {
    val item = Item(anArticle().build(), 1)

    assertThatExceptionOfType(ApprovalError::class.java).isThrownBy {
      // tag::approve_byFile_trivial[]
      approve(item).byFile()
      // end::approve_byFile_trivial[]
    }
  }

  @Test
  fun `constructor article quantity approve printed`() {
    val article = anArticle().build()
    val quantity = 1

    val item = Item(article, quantity)

    assertThatExceptionOfType(ApprovalError::class.java).isThrownBy {
      // tag::approve_byFile_printed[]
      approve(item)
        .printWith(objectPrinter()) // <1>
        .byFile()
      // end::approve_byFile_printed[]
    }
  }

  @Test
  fun `constructor article quantity approve printed and scrubbed`() {
    val article = anArticle().build()
    val quantity = 1

    val item = Item(article, quantity)

    // tag::approve_byFile_printed[]
    approve(item)
      .printWith(objectPrinter()) // <1>
      .scrubbedOf(uuids()) // <2>
      .byFile()
    // end::approve_byFile_printed[]
  }
}
