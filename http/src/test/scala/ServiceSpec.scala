package example

import org.http4s.client.blaze.{defaultClient => client}
import org.http4s.Uri
import org.scalatest.{FlatSpec, Matchers}
import scala.util.Try
import scalaz.\/
import scalaz.std.vector._
import scalaz.syntax.traverse._

class ServiceSpec extends FlatSpec with Matchers {

  // @todo use knobs to get configurations and testData
  object ProdConfig extends Config {
    lazy val listUri: Uri = Uri.fromString("http://localhost:8084/list").getOrElse(null)
    lazy val selectItemUri: Uri = Uri.fromString("http://localhost:8084/selectitem").getOrElse(null)
    lazy val createUri: Uri = Uri.fromString("http://localhost:8084/create").getOrElse(null)
    lazy val clientOp: ClientOpR = ToDoClient
  }

  val testData = Vector(
    "find water",
    "make shelter",
    "make fire",
    "find food"
  )

  "A todo service" must "be able to create todo items and serve the same items" in {
    import ProdConfig._

    val tryResult = Try {
      info("Testing create items")
      val v: Vector[\/[String, Item.Id]] = testData.traverse(clientOp.create(_).run(ProdConfig)).run
      v.sequenceU.isRight should equal(true)

      // @todo info("Testing get all items using process")
      //val allItemsD: \/[String, List[Item]] = clientOp.list().run(ProdConfig).run

      info("Testing retrieved items have correct content")
      val b: Vector[\/[String, Option[Item]]] = v.map(_.flatMap(id => clientOp.selectItem(id).run(ProdConfig).run))
      val c: \/[String, Vector[Option[Item]]] = b.sequenceU
      val d: \/[String, Vector[Boolean]] = c.map(o => o.map(oi =>
        if (oi.isEmpty) false else if (testData.contains(oi.get.content)) true else false
      ))
      d.isRight should equal(true)
      d.getOrElse(Vector(false)).contains(false) should equal(false)

      // @todo not sure this is the right place to shutdown client
      client.shutdownNow()
    }

    tryResult.isSuccess should equal(true)
  }
}
