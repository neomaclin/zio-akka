package com.quasigroup.zio.akka

import akka.actor.ActorSystem
import zio._

package object classic {
  type ClassicAkka = Has[ActorSystem]

  def live(name: String): ZManaged[Any, Throwable, ActorSystem] =
    Task.effect(ActorSystem(name)).toManaged(sys=>Task.fromFuture(_ => sys.terminate()).either)

}
