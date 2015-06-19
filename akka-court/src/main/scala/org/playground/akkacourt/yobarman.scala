package org.playground.akkacourt

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

// very simple Messages that our Actors will pass amongst each other.
case object Ticket
case object FullPint
case object EmptyPint

class BarTender extends Actor with ActorLogging {
  def receive = {
    case Ticket =>
      log.info("1 pint coming right up")

      Thread.sleep(1000)

      log.info("Your pint is ready, here you go")

      // send a full pint message to ticket sender
      sender ! FullPint

    case EmptyPint =>
      log.info("I think you're done for the day")

      context.system.shutdown()
  }
}

class Person extends Actor with ActorLogging {

  // pattern matches the Messages received by the Actor
  def receive:PartialFunction[Any, Unit] = {
    //case Pint => log.info("Thanks for the pint")
    case FullPint =>
      log.info("I'll make short work of this")

      Thread.sleep(1000)

      log.info("I'm ready for the next")

      // the bar tender sender receive back an empty pint message
      sender ! EmptyPint
  }
}

object YoBarmanApp extends App {

  // root object of our Akka Application, and is what all of our Actors will belong to
  val system:ActorSystem = ActorSystem("yo-barman")

  // attach a new Actor to the system
  val alice:ActorRef = system.actorOf(Props(new Person), "alice")

  // tender will receive a Ticket and send back a FullPint
  val zed:ActorRef = system.actorOf(Props(new BarTender), "zed")

  // alice sends a ticket to zed the bar tender
  // tell or ! is the same expect the in the case of tell a "Sender Context" is passed as 2nd parameter
  zed.tell(Ticket, alice)

  system.awaitTermination()

  // send a Pint Message to alice Actor
  //alice ! Pint

  // close program
  //system.shutdown()
}
