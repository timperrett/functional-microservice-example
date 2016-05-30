package example

import doobie.imports._
import doobie.contrib.h2.h2types._
import java.io.File
import knobs.{FileResource, _}
import scalaz.Monad
import scalaz.syntax.monad._
import scalaz.concurrent.Task
//import scalaz.stream.Process

abstract class Repository[M[_] : Monad] {
  def list: M[List[Item]]
  def selectItem(id: Item.Id): M[Option[Item]]
  def create(item: Item): M[Item.Id]
  def initTable(): M[Int]
}

class InMemoryRepository extends Repository[Task] with DAO {
  // An in-memory database
  val xat = for {
    cfg <- knobs.loadImmutable(Required(FileResource(new File(absConfigFile)) or
      ClassPathResource(configFile)) :: Nil)
    usr = cfg.require[String]("h2username")
    pwd = cfg.require[String]("h2password")
  } yield DriverManagerTransactor[Task]("org.h2.Driver", "jdbc:h2:mem:todo;DB_CLOSE_DELAY=-1", usr, pwd)

  def list: Task[List[Item]] =
    xat.flatMap(allItems.list.transact(_))

  def selectItem(id: Item.Id): Task[Option[Item]] =
    xat.flatMap(oneItem(id).option.transact(_))

//  def stream: Process[ConnectionIO, Item] =
//    allItems.process.transact(xa)

  def create(item: Item): Task[Item.Id] =
    xat.flatMap(insertItem(item).run.transact(_)).as(item.id)

  def initTable(): Task[Int] =
    xat.flatMap(init.run.transact(_))
}

trait DAO {
  // On table not found, init the db and try again
  def withInit[A](a: ConnectionIO[A]): ConnectionIO[A] =
    a.exceptSomeSqlState {
      case SqlState("42S02") => init.run *> a
    }

  def allItems: Query0[Item] =
    sql"""
      SELECT id, content, created_at 
      FROM items
      ORDER BY created_at ASC
    """.query[Item]

  def oneItem(id: Item.Id): Query0[Item] =
    sql"""
      SELECT *
      FROM items
      WHERE id = ${id}
    """.query[Item]

  def insertItem(item: Item): Update0 =
    sql"""
      INSERT INTO items (id, content, created_at)
      VALUES (${item.id}, ${item.content}, ${item.createdAt})
    """.update

  def init: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS items (
        id         UUID    PRIMARY KEY,
        content    VARCHAR NOT NULL,
        created_at LONG    NOT NULL       
      )
    """.update
}


