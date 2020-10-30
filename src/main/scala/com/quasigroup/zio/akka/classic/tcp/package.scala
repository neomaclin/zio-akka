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
  ): ZLayer[ClassicAkka, Throwable, Binding] =
    ZLayer.fromServiceManaged { system =>
      implicit val sys: ActorSystem = system
      Task
        .fromFuture(_ =>
          Tcp().bindAndHandle(handler, bindingOn.host, bindingOn.port)
        )
        .toManaged(binding => Task.fromFuture(_ => binding.unbind()).either)
    }


}
