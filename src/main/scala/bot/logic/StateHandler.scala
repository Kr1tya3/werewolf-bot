package bot.logic

import akka.actor.ActorRef
import org.conbere.irc.Messages.PrivMsg
import scala.util.Random
import bot.Config

class StateHandler(sender: ActorRef){
  val rand = new Random(System.currentTimeMillis());

  def handleChannelMessage(message: String) =
    if (State.nextOperation != null) State.nextOperation(message) else printHelloMessage

  def handlePrivateMessage(message: String, from: String) = {
    matchPrivateMessage(message, "!vote",
      () => if (rand.nextInt(20) > 1) sender ! PrivMsg(Config.MASTER_BOT, s"!vote ${pickTargetAsWolf}"))
    matchPrivateMessage(message, "!shoot", () => State.isVigilante = true)
    matchPrivateMessage(message, "!save", () => sender ! PrivMsg(Config.MASTER_BOT, "!save"))
    matchPrivateMessage(message, "!see", () => sender ! PrivMsg(Config.MASTER_BOT, s"!see ${pickTargetAsSeer}"))
    matchPrivateMessage(message, "You've found a WOLF", () => {
      val wolf = getPlayerFromSeerMessage(message)
      sender ! PrivMsg(Config.CHANNEL, s"I'm the seer, it's ${wolf}!")
      sender ! PrivMsg(Config.CHANNEL, s"!vote ${wolf}")
      State.blockTalk = true
    })
    matchPrivateMessage(message, "is a fellow villager.", () => State.addInnocent(getPlayerFromSeerMessage(message)))
  }

  def handleDeath(user: String) = State.removePlayer(user)

  def printHelloMessage = {
    sender ! PrivMsg(Config.CHANNEL, "Howdy partners!")
    State.nextOperation = listenForRegistrationStart
  }

  def listenForRegistrationStart(message: String): Unit =
    transition(message, "registration is now open, say !join to join the game", "!join", listenForGameStart, initGame)

  def listenForGameStart(message: String): Unit = {
    transition(message, "game starting! players are", "\\o/", listenForDay, updatePlayers)
    transition(message, "not enough players", ":(", listenForRegistrationStart)
  }

  def listenForDay(message: String): Unit = {
    transition(message, "it is now day", null, listenForDay, startDay)
    transition(message, "pulls out a gun and", null, listenForDay, vigilanteReveal)
    transition(message, "it is now night", null, listenForDay, endDay)
    transition(message, "game over!", "gg", listenForRegistrationStart, endDay)
  }

  def transition(message:String, condition: String, toSend: String,
                 nextoperation: (String => Unit), updateState: (String => Unit) = doNothing) = {
    if (message.toLowerCase contains condition) {
      updateState(message)
      if (toSend != null) sender ! PrivMsg(Config.CHANNEL, toSend)
      State.nextOperation = nextoperation
    }
  }

  def matchPrivateMessage(message: String, condition: String, command: (() => Unit)) = if (message contains condition) command()

  def startDay(message: String) = if (!State.isDay) State.isDay = true
  def endDay(message: String) = State.isDay = false
  def doNothing(message: String) = {}
  def initGame(message: String) = {State.isVigilante = false; State.isDay=false; State.blockTalk = false; State.innocents = Set()}
  def vigilanteReveal(message: String) = {
    def split = message.split(" pulls out a gun and")
    State.addInnocent(split(0))
  }

  def updatePlayers(message: String) = {
    def split = message.split(": ")
    State.players = split(1).split(", ")
    State.removePlayer(Config.BOT_NAME)
  }

  def getPlayerFromSeerMessage(message: String) = {
    val split: Array[String] = message.split("You have a dream about ")
    val words: Array[String] = split(1).split(" ")
    words(0)
  }

  def pickTargetAsWolf =
    if (State.innocents isEmpty) State.players(rand.nextInt(State.players.length))
    else State.innocents.head

  def pickTargetAsSeer =
    if (State.innocents isEmpty) State.players(rand.nextInt(State.players.length))
    else {
      val targets = State.players filterNot(p => State.innocents.contains(p))
      targets(rand.nextInt(targets.length))
    }
}

object State {
  var nextOperation: (String => Unit) = null
  var players: Array[String] = new Array[String](0)
  var isVigilante = false
  var isDay = false
  var wolves: Array[String] = new Array[String](0)
  var innocents: Set[String] = Set()
  var currentTarget: String = null
  var blockTalk = false

  def removePlayer(player: String) = {
    players = players.filterNot((person) => person.equals(player))
    removeInnocent(player)
  }

  def addInnocent(player: String) = innocents = innocents + player
  def removeInnocent(player: String) = innocents = innocents - player
}
