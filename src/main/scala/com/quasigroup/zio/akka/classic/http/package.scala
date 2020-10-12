package com.quasigroup.zio.akka.classic

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.quasigroup.zio.akka.models._
import zio._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

package object http {

  type Binding = Has[ServerBinding]

  def live(
      bindingOn: BindOn,
      withRoutes: RequestContext => Future[RouteResult]
  ): ZLayer[ClassicAkka, Throwable, Binding] =
    ZLayer.fromServiceM { system =>
      implicit val sys: ActorSystem = system
      Task.fromFuture {
        Http()
          .newServerAt(bindingOn.host, bindingOn.port)
          .bind(withRoutes)
          .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))(_)
      }
    }

}
