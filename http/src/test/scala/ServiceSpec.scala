package example

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import knobs.{ClassPathResource, FileResource, Required}
import org.scalatest.{FlatSpec, Matchers}
import scala.util.Try
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.std.vector._
import scalaz.syntax.traverse._

class ServiceSpec extends FlatSpec with Matchers {
  info("Start todo service before test")
  // The following may be a more expensive way to run a service since the system now monitors cancelService
  // I don't see a way to call the server's shutdown method after it gets wrap in Task
  val cancelService = new AtomicBoolean(false)
  val t: Task[Unit] = Task.delay(Main.main(Array[String]()))
  val running: Unit = Task.fork(t).runAsyncInterruptibly(println,cancelService)
  // Service can not start fast enough before hitting the test code, requires sleep:
  Thread.sleep(15000)

  "A todo service" must "be able to create todo items and serve the same items" in {
    val tcfg = knobs.loadImmutable(Required(
      FileResource(new File(absConfigFile)) or
        ClassPathResource(configFile)) :: Nil)

    val testClient = ToDoClientConfig(tcfg.run)

    val tryResult = Try {
      info("Testing create items")
      val v: Vector[\/[String, Item.Id]] =
        testClient.getTestData.traverse(testClient.getClientOp.create(_).run(testClient)).run
      v.sequenceU.isRight should equal(true)

      // @todo info("Testing get all items using process")
      // val allItemsD: \/[String, List[Item]] = clientOp.list().run(ProdConfig).run

      info("Testing retrieved items have correct content")
      val b: Vector[\/[String, Option[Item]]] = v.map(_.flatMap(id =>
        testClient.getClientOp.selectItem(id).run(testClient).run))
      val c: \/[String, Vector[Option[Item]]] = b.sequenceU
      val d: \/[String, Vector[Boolean]] =
        c.map(
          o =>
            o.map(oi =>
              if (oi.isEmpty)
                false
              else if (testClient.getTestData.contains(oi.get.content))
                true
              else
                false
            ))
      d.isRight should equal(true)
      d.getOrElse(Vector(false)).contains(false) should equal(false)
    }

    testClient.getClientOp.shutdown().run
    cancelService.set(true)  // Probably useless because the next line is the last line

    tryResult.isSuccess should equal(true)
  }
}
