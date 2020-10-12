package com.quasigroup.zio.akka


import akka.actor.typed.{ActorSystem, Behavior}
import zio.{Has, Task, UIO, ZManaged}

package object typed {

  type TypedAkka[T] = Has[ActorSystem[T]]

  def live[T](guardianBehavior: Behavior[T], name: String): ZManaged[Any, Throwable, ActorSystem[T]] =
    Task.effect(ActorSystem(guardianBehavior, name)).toManaged(sys => UIO(sys.terminate()))

}
