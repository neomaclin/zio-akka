
## Example usage:

```scala
 import sttp.tapir.server.akkahttp._
 import com.quasigroup.inc.basic.Entrance
 import com.quasigroup.zio.akka.classic
 import com.quasigroup.zio.akka.classic.http._
 import com.quasigroup.zio.akka.models.BindOn
 import zio._
 import zio.console._
 
 import scala.concurrent.{ExecutionContext, Future}
 
 object Main extends App {
 
 
   def run(args: List[String]) = (for {
       (name, binding) <- IO.succeed(("test", BindOn("127.0.0.1",8080))) // try using config to populate these
       routing <- IO.succeed[ToRoute](ec => Entrance.indexHtml.toRoute(_ => Future(Right("hello world"))(ec)))
       _ <- (for {
         akka <- classic.start(name)
         _ <- classic.http.start(binding,routing).provide(akka)
       } yield {}).use(_ => getStrLn)
   } yield {}).provideLayer(Console.live).run.exitCode
 
 }

```

Note: ```Entrance.indexHtml``` is a ```tapir``` endpoint from my other project: https://github.com/neomaclin/endpoints, or just for a quick demo to get some taste of what zio and akka do. 

```Scala

import com.quasigroup.zio.akka.classic
import com.quasigroup.zio.akka.classic.http._
import zio._
import zio.console._

object Main extends App {

 def run(args: List[String]) =
     (ZIO.environment[Binding] *> getStrLn).provideLayer(Console.live ++ classic.http.demo).exitCode

}
```
