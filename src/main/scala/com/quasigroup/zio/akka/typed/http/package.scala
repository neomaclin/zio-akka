package com.quasigroup.zio.akka.typed

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import com.quasigroup.zio.akka.models.BindOn
import akka.http.scaladsl.server.Route
import zio._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

package object http {

  type Binding = Has[ServerBinding]
  type ToRoute = ExecutionContext => Route

  def start[T : Tag](bindingOn: BindOn, withRoutes: ToRoute): ZManaged[ActorSystem[T], Throwable, ServerBinding] =
    (for {
      system <- ZIO.environment[ActorSystem[T]]
      ec <- ZIO.access[ActorSystem[T]](_.executionContext)
      server <- Task.fromFuture {
        implicit val sys: ActorSystem[T] = system
        Http()
          .newServerAt(bindingOn.host, bindingOn.port)
          .bind(withRoutes(ec))
          .map(
            _.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds)
          )(_)
      }
    }yield server).toManaged_


  def live[T : Tag](bindingOn: BindOn, withRoutes: ToRoute): ZLayer[TypedAkka[T], Throwable, Binding] =
    ZLayer.fromServiceManaged(start(bindingOn,withRoutes).provide)

}
