package bot.logic

import scala.util.Random
import bot.Config

trait SpeechEvent {
  def generateMessage(): List[String]
}

trait PlayerMessageSelectorEvent {
  def playerBasedRandomEvent(rand: Random, messages: List[String]) = {
    val player = State.players(rand.nextInt(State.players.length))
    List(messages(rand.nextInt(messages.length)).format(player))
  }
}

case class TrashTalk(rand: Random) extends SpeechEvent with PlayerMessageSelectorEvent {
  def generateMessage() = if (rand.nextInt(10) == 0) {
    val target = State.players(rand.nextInt(State.players.length))
    State.isDay = false
    List(s"I'm the seer, it's ${target}!", s"!vote ${target}")
  }
  else playerBasedRandomEvent(rand, Config.trashTalk)
}

case class NiceTalk(rand: Random) extends SpeechEvent with PlayerMessageSelectorEvent {
  def generateMessage() = playerBasedRandomEvent(rand, Config.niceTalk)
}

case class Proverb(rand: Random) extends SpeechEvent with PlayerMessageSelectorEvent {
  def generateMessage() = playerBasedRandomEvent(rand, Config.proverbs)
}

case class TryKill(rand: Random) extends SpeechEvent {
  def generateMessage() = {
    val victim = generateVictim
    if (victim isDefined) {
      State.currentTarget = victim.get
      List(generateKillMessage(victim.get))
    } else List()
  }

  def generateVictim : Option[String] = {
    val player = State.players(rand.nextInt(State.players.length))
    if (player.equals(State.currentTarget) || State.innocents.contains(player)) None else Some(player)
  }

  def generateKillMessage(victim: String): String = {
    if (State.isVigilante) {
      State.isVigilante = false
      String.format("!shoot %s", victim)
    } else {
      String.format("!vote %s", victim)
    }
  }
}

case class NoEvent() extends SpeechEvent{def generateMessage() = List()}