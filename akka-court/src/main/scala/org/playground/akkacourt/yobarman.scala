package org.playground.akkacourt

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

/**
 * A simple tutorial on actor/akka
 *
 * http://www.reactive.io/tips/2014/03/28/getting-started-with-actor-based-programming-using-scala-and-akka/
 */

// very simple Messages that our Actors will pass amongst each other.
case class Ticket(quantity: Int)

case class FullPint(number: Int)

case class EmptyPint(number: Int)

object YoBarmanApp extends App {

  // root object of our Akka Application, and is what all of our Actors will belong to
  val system: ActorSystem = ActorSystem("yo-barman")

  // attach new Actors to the system

  // meet zed the bartender
  // tender will receive a Ticket and send back a FullPint
  val zed: ActorRef = system.actorOf(Props(new BarTender), "zed")
  // meet drinkers
  val alice: ActorRef = system.actorOf(Props(new Person), "alice")
  val bob: ActorRef = system.actorOf(Props(new Person), "bob")
  val charlie: ActorRef = system.actorOf(Props(new Person), "charlie")

  // alice sends a ticket to zed the bar tender
  // tell or ! is the same expect the in the case of tell a "Sender Context" is passed as 2nd parameter
  zed.tell(Ticket(2), alice)
  zed.tell(Ticket(3), bob)
  zed.tell(Ticket(1), charlie)

  system.awaitTermination()
}

class BarTender extends Actor with ActorLogging {
  var total = 0

  def receive = {
    case Ticket(quantity) =>
      total = total + quantity

      log.info(s"I'll get $quantity pints for [${sender.path}]")

      for (number <- 1 to quantity) {
        log.info(s"Pint $number is coming right up for [${sender.path}]")

        Thread.sleep(1000)

        log.info(s"Pint $number is ready, here you go [${sender.path}]")

        // send a full pint message to ticket sender
        sender ! FullPint(number)
      }
    case EmptyPint(number) =>
      total match {
        case 1 =>
          log.info("Ya'll drank those pints quick, time to close up shop")

          context.system.shutdown()

        case n =>
          total = total - 1

          log.info(s"You drank pint $number quick, but there are still $total pints left")
      }
  }
}

class Person extends Actor with ActorLogging {

  // pattern matches the Messages received by the Actor
  def receive: PartialFunction[Any, Unit] = {

    case FullPint(number) =>
      log.info("I'll make short work of pint $number")

      Thread.sleep(1000)

      log.info(s"Done, here is the empty glass for pint $number")

      // the bar tender sender receive back an empty pint message
      sender ! EmptyPint(number)
  }
}


