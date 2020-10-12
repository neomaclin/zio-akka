package com.quasigroup.zio.akka.classic

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Tcp}
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.util.ByteString
import com.quasigroup.zio.akka.models.BindOn
import zio._

package object tcp {

  type Binding = Has[ServerBinding]

  def live(bindingOn: BindOn, handler: Flow[ByteString, ByteString, _]): ZLayer[ClassicAkka, Throwable, Binding] =
    ZLayer.fromServiceManaged { system: ActorSystem =>
      implicit val sys = system
      implicit val context = system.dispatcher
      Task.fromFuture( _ => Tcp().bindAndHandle(handler, bindingOn.host, bindingOn.port))
        .toManaged(binding => Task.fromFuture(_ => binding.unbind()).either)
      }

}
