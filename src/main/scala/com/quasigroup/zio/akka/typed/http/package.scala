package com.quasigroup.zio.akka.typed

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import com.quasigroup.zio.akka.models.BindOn
import zio._

import scala.concurrent.duration.DurationInt

package object http {

  type Binding = Has[ServerBinding]
  type ToRoute[T] = ActorSystem[T] => Route

  def start[T : Tag](bindingOn: BindOn, withRoutes: ToRoute[T]): ZManaged[ActorSystem[T], Throwable, ServerBinding] =
    (for {
      system <- ZIO.environment[ActorSystem[T]]
      server <- Task.fromFuture {
        implicit val sys: ActorSystem[T] = system
        Http()
          .newServerAt(bindingOn.host, bindingOn.port)
          .bind(withRoutes(sys))
          .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))(_)
      }
    }yield server).toManaged_


  def live[T : Tag](bindingOn: BindOn, withRoutes: ToRoute[T]): ZLayer[TypedAkka[T], Throwable, Binding] =
    ZLayer.fromServiceManaged(start(bindingOn,withRoutes).provide)

}
