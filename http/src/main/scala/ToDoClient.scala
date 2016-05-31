package example

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import knobs.Config
import org.apache.commons.lang3.StringEscapeUtils
import org.http4s.dsl._

// @todo import org.http4s.BasicCredentials
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.client._
import org.http4s.EntityEncoder
import org.http4s.Header
import org.http4s.Headers

// @todo import org.http4s.headers.Authorization
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status.ResponseClass.Successful
import org.http4s.Uri
import org.http4s.UrlForm
import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task
import scalaz.{ReaderT, Kleisli}

trait ClientOp {
  def list(uri: Uri): Task[\/[String, List[Item]]]

  def selectItem(uri: Uri, id: Item.Id): Task[\/[String, Option[Item]]]

  def create(uri: Uri, content: String): Task[\/[String, Item.Id]]

  def shutdown(): Task[Unit]
}

class ToDoClient extends ClientOp {
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  lazy val client = PooledHttp1Client()

  def list(uri: Uri): Task[\/[String, List[Item]]] = {
    client.get(uri) {
      case Successful(resp) => resp.as[List[Item]].map(\/-(_))
      case resp => Task.now(-\/(resp.status.toString()))
    }
  }

  def selectItem(uri: Uri, id: Item.Id): Task[\/[String, Option[Item]]] = {
    val req = POST(uri, UrlForm("id" -> id.toString))
    client(req).flatMap { response =>
      response match {
        case Successful(resp) => resp.as[Option[Item]].map(\/-(_))
        case resp => Task.now(-\/(resp.status.toString()))
      }
    }
  }

  def create(uri: Uri, content: String): Task[\/[String, Item.Id]] = {
    EntityEncoder[String]
      .toEntity("{\"content\": \"" + StringEscapeUtils.escapeEcmaScript(content) + "\"}").flatMap {
      entity =>
        client(
          Request(
            method = Method.POST,
            uri = uri,
            headers = Headers(
              Header("Content-Type", "application/json")
            ),
            body = entity.body
          )
        ).flatMap { response =>
          response match {
            case Successful(resp) => resp.as[Item.Id].map(\/-(_))
            case resp => Task.now(-\/(resp.status.toString()))
          }
        }
    }
  }

  def shutdown(): Task[Unit] = client.shutdown
}

trait ClientOpR {
  def list(): ReaderT[Task, ToDoClientConfig, \/[String, List[Item]]]

  def selectItem(id: Item.Id): ReaderT[Task, ToDoClientConfig, \/[String, Option[Item]]]

  def create(content: String): ReaderT[Task, ToDoClientConfig, \/[String, Item.Id]]

  def shutdown(): ReaderT[Task, ToDoClientConfig, Unit]
}

trait ToDoClientConfig {
  def getListUri: Uri

  def getSelectItemUri: Uri

  def getCreateUri: Uri

  def getClientOp: ClientOpR

  def getTestData: Vector[String]
}

object ToDoClientConfig {

  private class TestConfig(cfg: Config) extends ToDoClientConfig {
    override def getListUri = Uri.fromString(cfg.require[String](listUri)).getOrElse(null)

    override def getSelectItemUri = Uri.fromString(cfg.require[String](selectItemUri)).getOrElse(null)

    override def getCreateUri = Uri.fromString(cfg.require[String](createUri)).getOrElse(null)

    override def getTestData = cfg.require[List[String]](testData).toVector

    override def getClientOp: ClientOpR = ToDoClient
  }

  def apply(cfg: Config): ToDoClientConfig = new TestConfig(cfg)

}

object ToDoClient extends ClientOpR {
  val todoClient = new ToDoClient()

  override def list(): ReaderT[Task, ToDoClientConfig, \/[String, List[Item]]] = Kleisli { cfg =>
    todoClient.list(cfg.getListUri)
  }

  override def selectItem(id: Item.Id): ReaderT[Task, ToDoClientConfig, \/[String, Option[Item]]] = Kleisli { cfg =>
    todoClient.selectItem(cfg.getSelectItemUri, id)
  }

  override def create(content: String): ReaderT[Task, ToDoClientConfig, \/[String, Item.Id]] = Kleisli { cfg =>
    todoClient.create(cfg.getCreateUri, content)
  }

  override def shutdown(): ReaderT[Task, ToDoClientConfig, Unit] = Kleisli { cfg =>
    todoClient.shutdown()
  }
}
