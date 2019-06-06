package me.fzzy.robofzzy4j.commands

import me.fzzy.robofzzy4j.Command
import me.fzzy.robofzzy4j.listeners.VoiceListener
import me.fzzy.robofzzy4j.util.CommandCost
import me.fzzy.robofzzy4j.util.CommandResult
import sx.blah.discord.Discord4J
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IVoiceChannel
import java.io.File


object Play : Command {

    override val cooldownMillis: Long = 1000 * 60 * 10
    override val votes: Boolean = true
    override val description = "Plays audio in the voice channel"
    override val usageText: String = "play <videoUrl>"
    override val allowDM: Boolean = true
    override val price: Int = 4
    override val cost: CommandCost = CommandCost.CURRENCY

    override fun runCommand(message: IMessage, args: List<String>): CommandResult {

        val userVoiceChannel = message.author.getVoiceStateForGuild(message.guild).channel
                ?: return CommandResult.fail("i cant do that unless youre in a voice channel")
        if (args.isEmpty()) return CommandResult.fail("thats not how you use that command $usageText")

        return play(userVoiceChannel, args[0], message.longID)
    }

    fun play(channel: IVoiceChannel, url: String, messageId: Long = 0, playTimeSeconds: Int = 60, playTimeAdjustment: Int = 40): CommandResult {

        val currentTime = System.currentTimeMillis()

        Discord4J.LOGGER.info("attempting to get media from $url")

        Thread {
            val rt = Runtime.getRuntime()
            val process = rt.exec("youtube-dl -x --audio-format mp3 --no-playlist $url -o \"cache${File.separator}$currentTime.%(ext)s\"")
            process.waitFor()

            val file = File("cache${File.separator}$currentTime.mp3")

            if (file.exists())
                VoiceListener.playTempAudio(channel, file, true, 0.25F, playTimeSeconds, playTimeAdjustment, messageId)
        }.start()

        return CommandResult.success()
    }
}