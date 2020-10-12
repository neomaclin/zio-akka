package com.quasigroup.zio.akka

import akka.actor.ActorSystem
import zio._

package object classic {

  type ClassicAkka = Has[ActorSystem]

  def live(name: String): ZLayer[Any, Throwable, ClassicAkka] =
    Task
      .effect(ActorSystem(name))
      .toManaged(sys => Task.fromFuture(_ => sys.terminate()).either)
      .toLayer

}
