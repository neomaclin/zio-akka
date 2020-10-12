package com.quasigroup.zio.akka.typed

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.quasigroup.zio.akka.models.BindOn
import zio._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

package object http {

  type Binding = Has[ServerBinding]

  def live[T : Tag](bindingOn: BindOn, withRoutes: RequestContext => Future[RouteResult]): ZLayer[TypedAkka[T], Throwable, Binding] =
    ZLayer.fromServiceM { system =>
      implicit val sys: ActorSystem[T] = system
      Task.fromFuture { ec =>
        Http().newServerAt(bindingOn.host, bindingOn.port)
          .bind(withRoutes).map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))(ec)
      }
    }

}
