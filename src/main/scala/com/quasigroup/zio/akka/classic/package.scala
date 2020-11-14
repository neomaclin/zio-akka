package com.quasigroup.zio.akka

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Cancellable, Props}
import akka.stream.scaladsl.RunnableGraph
import akka.util.Timeout
import zio._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{DurationDouble, FiniteDuration}
import scala.reflect.ClassTag


package object classic {

  type ClassicAkka = Has[ActorSystem]
  type ClassicAkkaService = Has[Service]

  trait Service {
    def register(props: Props, name: String): Task[ActorRef]
    def register(props: Props): Task[ActorRef]
    def select(path: String): Task[ActorSelection]
    def stop(actor: ActorRef): Task[Unit]
    def scheduleOnce(delay: FiniteDuration, f: => Unit): Task[Cancellable]
    def scheduleOnce[T](
        delay: FiniteDuration,
        receiver: ActorRef,
        message: T
    ): Task[Unit]
    def ask[C, R: Tag : ClassTag](receiver: ActorRef, message: C): Task[R]
    def execute[M](graph: RunnableGraph[M]): Task[M]
  }

  val demo: ZLayer[Any, Throwable, ClassicAkka] = live("demo")

  def live(name: String): ZLayer[Any, Throwable, ClassicAkka] = {
    start(name).toLayer
  }

  def start(name: String): ZManaged[Any, Throwable, ActorSystem] = {
    Task
      .effect(ActorSystem(name))
      .toManaged(sys => Task.fromFuture(_ => sys.terminate()).either)
  }

  private def createService(system: ActorSystem): Task[Service] = Task.effect {
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    new classic.Service {
      def register(props: Props, name: String): Task[ActorRef] =
        Task { system.actorOf(props, name) }
      def register(props: Props): Task[ActorRef] =
        Task { system.actorOf(props) }
      def select(path: String): Task[ActorSelection] =
        Task { system.actorSelection(path) }
      def stop(actor: ActorRef): Task[Unit] =
        Task { system.stop(actor) }
      def scheduleOnce(delay: FiniteDuration, f: => Unit): Task[Cancellable] =
        Task { system.scheduler.scheduleOnce(delay)(f) }
      def scheduleOnce[T](
          delay: FiniteDuration,
          receiver: ActorRef,
          message: T
      ): Task[Unit] =
        Task { system.scheduler.scheduleOnce(delay, receiver, message) }
      def execute[M](graph: RunnableGraph[M]): Task[M] = {
        Task { graph.run() }
      }
      import akka.pattern
      def ask[C, R: Tag : ClassTag](receiver: ActorRef, message: C): Task[R] = {
        implicit val timeout: Timeout = 0.5.second
        Task.fromFuture[R]{ _ => pattern.ask(receiver, message).mapTo[R] }
      }
    }
  }

  val serviceM: ZManaged[ActorSystem, Throwable, Service] =
    ZManaged.fromEffect(ZIO.environment[ActorSystem] >>= createService)

  val service: ZLayer[ClassicAkka, Throwable, ClassicAkkaService] =
    ZLayer.fromServiceM(createService)

  def register(props: Props): RIO[ClassicAkkaService, ActorRef] =
    ZIO.accessM(_.get.register(props))

  def register(props: Props, name: String): RIO[ClassicAkkaService, ActorRef] =
    ZIO.accessM(_.get.register(props, name))

  def select(path: String): RIO[ClassicAkkaService, ActorSelection] =
    ZIO.accessM(_.get.select(path))

  def stop(actor: ActorRef): RIO[ClassicAkkaService, Unit] =
    ZIO.accessM(_.get.stop(actor))

  def scheduleOnce(
      delay: FiniteDuration,
      f: => Unit
  ): RIO[ClassicAkkaService, Cancellable] =
    ZIO.accessM(_.get.scheduleOnce(delay, f))

  def scheduleOnce(
      delay: FiniteDuration,
      receiver: ActorRef,
      message: Any
  ): RIO[ClassicAkkaService, Unit] =
    ZIO.accessM(_.get.scheduleOnce(delay, receiver, message))

  def execute[M](graph: RunnableGraph[M]): RIO[ClassicAkkaService, M] =
    ZIO.accessM(_.get.execute(graph))

  def ask[C, R: Tag : ClassTag](receiver: ActorRef, message: C): RIO[ClassicAkkaService,R] =
    ZIO.accessM(_.get.ask(receiver, message))
}
