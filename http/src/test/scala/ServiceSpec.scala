package example

import java.io.File
import knobs.{ClassPathResource, FileResource, Required}
import org.http4s.client.blaze.{defaultClient => client}
import org.scalatest.{FlatSpec, Matchers}
import scala.util.Try
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.std.vector._
import scalaz.syntax.traverse._

class ServiceSpec extends FlatSpec with Matchers {
  info("Start todo service before test")
  val t = Task.delay(Main.main(Array[String]()))
  Task.fork(t).runAsync(println)
  // Service can not start fast enough before hitting the test code, requires sleep:
  Thread.sleep(15000)

  "A todo service" must "be able to create todo items and serve the same items" in {

    val tcfg = knobs.loadImmutable(Required(
      FileResource(new File(absConfigFile)) or
        ClassPathResource(configFile)) :: Nil)

    val testConfig = ToDoClientConfig(tcfg.run)

    val tryResult = Try {
      info("Testing create items")
      val v: Vector[\/[String, Item.Id]] =
        testConfig.getTestData.traverse(testConfig.getClientOp.create(_).run(testConfig)).run
      v.sequenceU.isRight should equal(true)

      // @todo info("Testing get all items using process")
      // val allItemsD: \/[String, List[Item]] = clientOp.list().run(ProdConfig).run

      info("Testing retrieved items have correct content")
      val b: Vector[\/[String, Option[Item]]] = v.map(_.flatMap(id =>
        testConfig.getClientOp.selectItem(id).run(testConfig).run))
      val c: \/[String, Vector[Option[Item]]] = b.sequenceU
      val d: \/[String, Vector[Boolean]] =
        c.map(
          o =>
            o.map(oi =>
              if (oi.isEmpty)
                false
              else if (testConfig.getTestData.contains(oi.get.content))
                true
              else
                false
            ))
      d.isRight should equal(true)
      d.getOrElse(Vector(false)).contains(false) should equal(false)
    }

    // @todo not sure this is the right place to shutdown client
    client.shutdownNow()
    // @todo shutdown service

    tryResult.isSuccess should equal(true)
  }
}
