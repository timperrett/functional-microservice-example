package example

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.apache.commons.lang3.StringEscapeUtils
import org.http4s.dsl._
// @todo import org.http4s.BasicCredentials
//import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.client._
import org.http4s.client.blaze.{defaultClient => client}
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
}

class ToDoClient extends ClientOp {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  def list(uri: Uri): Task[\/[String, List[Item]]] = {
    //lazy val client = PooledHttp1Client()
    client.get(uri) {
      case Successful(resp) => resp.as[List[Item]].map(\/-(_))
      case resp => Task.now(-\/(resp.status.toString()))
    }
  }

  def selectItem(uri: Uri, id: Item.Id): Task[\/[String, Option[Item]]] = {
    //lazy val client = PooledHttp1Client()
    val req = POST(uri, UrlForm("id" -> id.toString))
    client(req).flatMap { response =>
      response match {
        case Successful(resp) => resp.as[Option[Item]].map(\/-(_))
        case resp => Task.now(-\/(resp.status.toString()))
      }
    }
  }

  def create(uri: Uri, content: String): Task[\/[String, Item.Id]] = {
    //lazy val client = PooledHttp1Client()
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
}

trait ClientOpR {
  def list(): ReaderT[Task, Config, \/[String, List[Item]]]

  def selectItem(id: Item.Id): ReaderT[Task, Config, \/[String, Option[Item]]]

  def create(content: String): ReaderT[Task, Config, \/[String, Item.Id]]
}

trait Config {
  def listUri: Uri

  def selectItemUri: Uri

  def createUri: Uri

  def clientOp: ClientOpR
}

object ToDoClient extends ClientOpR {
  override def list(): ReaderT[Task, Config, \/[String, List[Item]]] = Kleisli { cfg =>
    new ToDoClient().list(cfg.listUri)
  }

  override def selectItem(id: Item.Id): ReaderT[Task, Config, \/[String, Option[Item]]] = Kleisli { cfg =>
    new ToDoClient().selectItem(cfg.selectItemUri, id)
  }

  override def create(content: String): ReaderT[Task, Config, \/[String, Item.Id]] = Kleisli { cfg =>
    new ToDoClient().create(cfg.createUri, content)
  }
}
