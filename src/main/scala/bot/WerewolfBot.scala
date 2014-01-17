package bot

import org.conbere.irc.{Client, ClassicBot}
import akka.actor.{ActorRef, Props, ActorSystem}
import com.typesafe.scalalogging.log4j.Logging
import bot.logic._
import scala.concurrent.duration._
import org.conbere.irc.Messages.PrivMsg
import scala.Predef._
import scala.util.Random
import bot.logic.Proverb
import org.conbere.irc.Tokens.Message
import bot.logic.NoEvent
import bot.logic.TrashTalk
import bot.logic.TryKill
import bot.logic.NiceTalk
import org.conbere.irc.Room

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

class WerewolfBot ( val serverName:String
                    , val nickName:String
                    , val userName:String
                    , val password:String
                    , val realName:String
                    , val rooms:List[Room])
  extends ClassicBot {

  var msgDispatcher: ActorRef = sender
  val rand = new Random(System.currentTimeMillis());

  context.system.scheduler.schedule(0 seconds, 2 seconds)(tryTalkIfDay)

  def handler = new StateHandler(sender)

  def generateEvent: SpeechEvent = {
    val random: Int = rand.nextInt(300)
    random match {
      case n if Config.killRandRange contains n => TryKill(rand)
      case n if Config.pvbRandRange contains n => Proverb(rand)
      case n if Config.niceTalkRange contains n => NiceTalk(rand)
      case n if Config.trashRandRange contains n => TrashTalk(rand)
      case _ => NoEvent()
    }
  }

  def tryTalkIfDay = if (State.isDay && !State.blockTalk) {
   val messages = generateEvent.generateMessage()
   messages foreach {msg => msgDispatcher ! PrivMsg(Config.CHANNEL, msg)}
  }

  def receive = onConnect orElse defaultHandler orElse {
   case Message(option, command, params) => {
     msgDispatcher = sender
      params match {
        case List(_, _, Config.CHANNEL, message) => {
          handler.handleChannelMessage(message)
        }
        case List(from, _, Config.BOT_NAME, message) => {
          handler.handlePrivateMessage(message, from)
        }
        case List(from, "MODE", Config.CHANNEL, "-v", target) => {
          handler.handleDeath(target)
        }
        case _ => params.foreach({s => println("  " + s)})
     }
   }
   case _ => println( "Unknown case")
  }
}

object Main extends Logging {

  def main(args:Array[String]) = {
    val system = ActorSystem("Irc")
    val rooms = List(Room(Config.CHANNEL, None))

    val bot = system.actorOf(Props(classOf[WerewolfBot], Config.SERVER, Config.BOT_NAME, Config.BOT_NAME, "", "CarIes", rooms))
    system.actorOf(Props(classOf[Client], Config.SERVER, Config.PORT, bot))
  }
}
