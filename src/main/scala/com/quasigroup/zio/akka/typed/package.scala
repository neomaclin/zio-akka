package com.quasigroup.zio.akka

import akka.actor.typed._
import zio._

package object typed {

  type TypedAkka[T] = Has[ActorSystem[T]]

  def live[T: Tag](guardianBehavior: Behavior[T], name: String): ZLayer[Any, Throwable, TypedAkka[T]] =
    Task
      .effect(ActorSystem(guardianBehavior, name))
      .toManaged(sys => UIO(sys.terminate()))
      .toLayer
}
