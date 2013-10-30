package bot.logic

import akka.actor.ActorRef
import scala.util.Random
import org.conbere.irc.Messages.PrivMsg
import bot.Config

class TalkFlow(sender: ActorRef, channel: String) extends Thread {
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

  def trashTalk = {
    val player = State.players(rand.nextInt(State.players.length))
    sender ! PrivMsg(channel, Config.trashTalk(rand.nextInt(Config.trashTalk.length)).format(player))
  }

  def niceTalk = {
    val player = State.players(rand.nextInt(State.players.length))
    sender ! PrivMsg(channel, Config.niceTalk(rand.nextInt(Config.niceTalk.length)).format(player))
  }

  def proverb = {
    val player = State.players(rand.nextInt(State.players.length))
    sender ! PrivMsg(channel, Config.proverbs(rand.nextInt(Config.proverbs.length)).format(player))
  }

  override def run() = {
    while(State.isDay) {
      Thread.sleep(1000)
      val random: Int = rand.nextInt(300)
      if (random < 2) tryKill
      if (random > 100 && random < 103) proverb
      if (random > 200 && random < 202) niceTalk
      if (random > 297) trashTalk
    }
  }
}
