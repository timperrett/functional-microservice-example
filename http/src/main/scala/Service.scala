package example

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._

object ToDoService {
  import scala.concurrent.ExecutionContext

  def service(implicit executionContext: ExecutionContext = ExecutionContext.global) =
    HttpService {
      case req @ GET -> Root =>
        Ok("testing")

      case GET -> Root / "list" =>
        Ok("testing")

      case GET -> Root / "create" =>
        Ok("testing")

    }
}
