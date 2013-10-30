package bot

object Config {
  val CHANNEL = "#werewolf"
  val BOT_NAME = "carIes"
  val MASTER_BOT = "twgbot"
  val SERVER = "irc.live.chonp.net"
  val PORT = 6667

  val trashTalk = List("You're quite suspicious, %s", "%s is not really nice to others. Something's up!", "Are you the wolf, %s?", "%s, I'm watching you!", "%s is a liar!", "You might as well resign, %s")
  val niceTalk = List("I don't think %s is the wolf.", "%s seems really friendly.", "%s: You are my friend.", "Do you know any new Catalan proverbs, %s?")
  val proverbs = List("In a shut mouth, no fly will go in, %s!", "%s: It is a poor mouse that has only one hole.", "Even a blind pig may occasionally pick up an acorn, my dear %s",
    "Keep in mind, %s, you'll catch a liar before you'll catch a cripple.", "It is better to be alone than to be in bad company like %s", "Don't forget %s: Well begun, is half done.",
    "A %s made of sugar still tastes bitter.")

  //  val CHANNEL = "#testchannel"
  //  val BOT_NAME = "carIes"
  //  val MASTER_BOT = "kristof"
}
