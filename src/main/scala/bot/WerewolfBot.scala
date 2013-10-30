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

  def handler = new StateHandler(sender)

  def receive = onConnect orElse defaultHandler orElse {
   case Message(option, command, params) => {
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
    val client = system.actorOf(Props(classOf[Client], Config.SERVER, Config.PORT, bot))
  }
}