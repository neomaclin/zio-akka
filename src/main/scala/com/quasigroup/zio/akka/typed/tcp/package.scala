package com.quasigroup.zio.akka.typed

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
  ): ZLayer[TypedAkka[T], Throwable, Binding] =
    ZLayer.fromServiceManaged { system =>
      implicit val sys: akka.actor.ActorSystem = system.classicSystem
      Task
        .fromFuture{_ =>
          Tcp().bindAndHandle(handler, bindingOn.host, bindingOn.port)
        }
        .toManaged(binding => Task.fromFuture(_ => binding.unbind()).either)
    }

}
