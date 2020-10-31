package com.quasigroup.zio.akka.classic

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Flow, Tcp}
import akka.util.ByteString
import com.quasigroup.zio.akka.models._
import zio._

package object tcp {

  type Binding = Has[ServerBinding]

  def live(
      bindingOn: BindOn,
      handler: Flow[ByteString, ByteString, _]
  ): ZLayer[ClassicAkka, Throwable, Binding] =  ZLayer.fromServiceManaged(start(bindingOn,handler).provide)

  def start(bindingOn: BindOn,  handler: Flow[ByteString, ByteString, _]): ZManaged[ActorSystem, Throwable, ServerBinding] =
    (for {
      system <- ZIO.environment[ActorSystem]
      server <- Task.fromFuture { _ =>
          implicit val sys: ActorSystem = system
          Tcp().bindAndHandle(handler, bindingOn.host, bindingOn.port)
        }
    }yield server).toManaged(binding => Task.fromFuture(_ => binding.unbind()).either)

}
