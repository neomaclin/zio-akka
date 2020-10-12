package com.quasigroup.zio.akka.classic


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.quasigroup.zio.akka.models.BindOn
import zio._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

package object http {

  type Binding = Has[ServerBinding]

  def live(bindingOn: BindOn, withRoutes: HttpRequest => Future[HttpResponse]): ZLayer[ClassicAkka, Throwable, Binding] =
    ZLayer.fromServiceM { system: ActorSystem =>
      implicit val sys = system
      implicit val context = system.dispatcher
      Task.fromFuture { _ =>
        Http().newServerAt(bindingOn.host, bindingOn.port)
          .bind(withRoutes).map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))
      }
    }
}
