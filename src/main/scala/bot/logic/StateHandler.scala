package bot.logic

import akka.actor.ActorRef
import org.conbere.irc.Messages.PrivMsg
import scala.util.Random

class StateHandler(sender: ActorRef, channel: String, playerName: String, masterBot: String){
  val rand = new Random(System.currentTimeMillis());

  def handleChannelMessage(message: String) = if (State.nextOperation != null) State.nextOperation(message) else printHelloMessage

  def handlePrivateMessage(message: String, from: String) = {
    matchPrivateMessage(message, "!vote", () => sender ! PrivMsg(masterBot, s"!vote ${State.players(rand.nextInt(State.players.length))}"))
    matchPrivateMessage(message, "!shoot", () => State.isVigilante = true)
    matchPrivateMessage(message, "!save", () => sender ! PrivMsg(masterBot, "!save"))
  }

  def handleDeath(user: String) = State.removePlayer(user)

  def printHelloMessage = {
    sender ! PrivMsg(channel, "Howdy partners!")
    State.nextOperation = listenForRegistrationStart
  }

  def listenForRegistrationStart(message: String): Unit = transition(message, "registration is now open, say !join to join the game", "!join", listenForGameStart, initGame)

  def listenForGameStart(message: String): Unit = {
    transition(message, "game starting! players are", "\\o/", listenForDay, updatePlayers)
    transition(message, "not enough players", ":(", listenForRegistrationStart)
  }

  def listenForDay(message: String): Unit = {
    transition(message, "it is now day", null, listenForDay, startDay)
    transition(message, "it is now night", null, listenForDay, endDay)
    transition(message, "game over!", "gg", listenForRegistrationStart, endDay)
  }

  def transition(message:String, condition: String, toSend: String, nextoperation: (String => Unit), updateState: (String => Unit) = doNothing) = {
    if (message.toLowerCase contains condition) {
      updateState(message)
      if (toSend != null) sender ! PrivMsg(channel, toSend)
      State.nextOperation = nextoperation
    }
  }

  def matchPrivateMessage(message: String, condition: String, command: (() => Unit)) = if (message contains condition) command

  def startDay(message: String) = {
    State.isDay = true
    val killThread = new KillFlow(sender, channel)
    killThread.start()
  }

  def endDay(message: String) = State.isDay = false

  def doNothing(message: String) = {}

  def initGame(message: String) = {State.isVigilante = false; State.isDay=false}

  def updatePlayers(message: String) = {
    def split = message.split(": ")
    State.players = split(1).split(", ")
    State.removePlayer(playerName)
  }
}

object State {
  var nextOperation: (String => Unit) = null
  var players: Array[String] = new Array[String](0)
  var isVigilante = false
  var isDay = false
  var wolves: Array[String] = new Array[String](0)
  var currentTarget: String = null

  def removePlayer(player: String) = players = players.filterNot((person) => person.equals(player))
}

class KillFlow(sender: ActorRef, channel: String) extends Thread {
  val rand = new Random(System.currentTimeMillis());

  def generateVictim = {
    val player = State.players(rand.nextInt(State.players.length))
    if (player.equals(State.currentTarget)) null else player
  }

  def generateKillMessage(victim: String): String = {
    if (State.isVigilante) {
      State.isVigilante = false
      String.format("!shoot %s", victim)
    } else {
      String.format("!vote %s", victim)
    }
  }

  def tryKill = {
    val victim = generateVictim
    if (victim != null) {
      sender ! PrivMsg(channel, generateKillMessage(victim))
      State.currentTarget = victim
    }
  }

  override def run() = {
    while(State.isDay) {
      Thread.sleep(1000)
      if (rand.nextInt(300) < 73) tryKill
    }
  }
}
