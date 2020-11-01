package com.quasigroup.zio.akka.typed

import akka.actor
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Flow, Tcp}
import akka.util.ByteString
import com.quasigroup.zio.akka.models.BindOn
import zio._

package object tcp {

  type Binding = Has[ServerBinding]


  def live[T: Tag](
            bindingOn: BindOn,
            handler: Flow[ByteString, ByteString, _]
          ): ZLayer[TypedAkka[T], Throwable, Binding] = ZLayer.fromServiceManaged(start(bindingOn,handler).provide)

  def start[T: Tag](bindingOn: BindOn,  handler: Flow[ByteString, ByteString, _]): ZManaged[ActorSystem[T], Throwable, ServerBinding] =
    (for {
      system <- ZIO.environment[ActorSystem[T]]
      server <- Task.fromFuture { _ =>
        implicit val sys: actor.ActorSystem = system.classicSystem
        Tcp().bindAndHandle(handler, bindingOn.host, bindingOn.port)
      }
    }yield server).toManaged(binding => Task.fromFuture(_ => binding.unbind()).either)
}
