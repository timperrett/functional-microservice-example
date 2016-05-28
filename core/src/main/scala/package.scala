import scalaz.Kleisli
import scalaz.concurrent.Task

package object example {

  import annotation.tailrec
  import java.util.concurrent.atomic.AtomicReference

  implicit class Atomic[A](val atomic: AtomicReference[A]){
    @tailrec final def update(f: A => A): A = {
      val oldValue = atomic.get()
      val newValue = f(oldValue)
      if (atomic.compareAndSet(oldValue, newValue)) newValue else update(f)
    }

    def get: A = atomic.get
  }

}
