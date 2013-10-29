package bot

import org.conbere.irc.{Client, Room, ClassicBot}
import akka.actor.{Props, ActorSystem}
import com.typesafe.scalalogging.log4j.Logging
import org.conbere.irc.Tokens.Message
import bot.logic.StateHandler

class WerewolfBot ( val serverName:String
                    , val nickName:String
                    , val userName:String
                    , val password:String
                    , val realName:String
                    , val rooms:List[Room])
  extends ClassicBot {

  def handler = new StateHandler(sender, Main.CHANNEL, Main.BOT_NAME, Main.MASTER_BOT)

  def receive = onConnect orElse defaultHandler orElse {
   case Message(option, command, params) => {
      params match {
        case List(_, _, Main.CHANNEL, message) => {
          handler.handleChannelMessage(message)
        }
        case List(from, _, Main.BOT_NAME, message) => {
          handler.handlePrivateMessage(message, from)
        }
        case List(from, "MODE", Main.CHANNEL, "-v", target) => {
          handler.handleDeath(target)
        }
        case _ => params.foreach({s => println("  " + s)})
     }
    }
    case _ => println( "Unknown case")
  }
}

object Main extends Logging {

  val CHANNEL = "#werewolf"
  val BOT_NAME = "carIes"
  val MASTER_BOT = "twgbot"

  def main(args:Array[String]) = {
    val system = ActorSystem("Irc")
    val server = "irc.server.com"
    val port = 6667
    val rooms = List(Room(CHANNEL, None))

    val bot = system.actorOf(Props(classOf[WerewolfBot], server, BOT_NAME, BOT_NAME, "", "CarIes", rooms))
    val client = system.actorOf(Props(classOf[Client], server, port, bot))
  }
}