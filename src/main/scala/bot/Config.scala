package bot

object Config {
  val CHANNEL = "#werewolf"
  val BOT_NAME = "carIes"
  val MASTER_BOT = "twgbot"
  val SERVER = "[your-irc-server]"
  val PORT = 6667

  val trashTalk = List("You're quite suspicious, %s", "%s is not really nice to others. Something's up!", "Are you the wolf, %s?", "%s, I'm watching you!", "%s is a liar!", "You might as well resign, %s",
    "I’m Hungry! Eat your leg, %s!", "%s looks quite hairy.")
  val niceTalk = List("I don't think %s is the wolf.", "%s seems really friendly.", "%s: You are my friend.", "Do you know any new Catalan proverbs, %s?", "I'm sure that %s is innocent.", "%s doesn't look too hairy")
  val proverbs = List("In a shut mouth, no fly will go in, %s!", "%s: It is a poor mouse that has only one hole.", "Even a blind pig may occasionally pick up an acorn, my dear %s",
    "Keep in mind, %s, you'll catch a liar before you'll catch a cripple.", "It is better to be alone than to be in bad company like %s", "Don't forget %s: Well begun, is half done.",
    "A %s made of sugar still tastes bitter.", "If this is war, let peace never come, right %s?", "If the moon touch the food then you can throw it away, %s",  "To the table and to the bed at the first shout, %s!")

  val killRandRange = 0 to 3
  val pvbRandRange = 101 to 101
  val niceTalkRange = 201 to 201
  val trashRandRange = 297 to 300
}

