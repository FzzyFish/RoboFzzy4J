package me.fzzy.eventvoter.commands

import me.fzzy.eventvoter.Command
import me.fzzy.eventvoter.Funcs
import me.fzzy.eventvoter.cli
import me.fzzy.eventvoter.imageProcessQueue
import me.fzzy.eventvoter.thread.ImageProcessTask
import org.apache.commons.io.FileUtils
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.audio.AudioPlayer
import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

class Tts : Command {

    override val cooldownMillis: Long = 60 * 5 * 1000
    override val attemptDelete: Boolean = true
    override val description = "Joins the voice channel and plays text to speech"
    override val usageText: String = "-tts [text]"
    override val allowDM: Boolean = true

    override fun runCommand(event: MessageReceivedEvent, args: List<String>) {
        if (args.isNotEmpty()) {
            var text = ""
            for (arg in args) {
                text += " $arg"
            }
            text = text.substring(1)
            imageProcessQueue.addToQueue(object : ImageProcessTask {

                var processingMessage: IMessage? = null
                override fun run(): Any? {

                    val fileName = "${System.currentTimeMillis()}.mp3"
                    val speech = Funcs.getTextToSpeech(text)
                    FileUtils.writeByteArrayToFile(File(fileName), speech)
                    var sound = File(fileName)
                    imageProcessQueue.addToQueue(object : ImageProcessTask {
                        override fun run(): Any? {
                            val userVoiceChannel = event.author.getVoiceStateForGuild(event.guild).channel
                            val audioP = AudioPlayer.getAudioPlayerForGuild(event.guild)
                            if (userVoiceChannel != null) {

                                val stream = AudioSystem.getAudioFileFormat(sound)
                                val frames = stream.frameLength
                                val durationInSeconds = (frames + 0.0) / stream.format.frameRate

                                userVoiceChannel.join()
                                Thread.sleep(100)
                                audioP.clear()
                                Thread.sleep(100)
                                try {
                                    audioP.queue(sound)
                                } catch (e: IOException) {
                                    // File not found
                                } catch (e: UnsupportedAudioFileException) {
                                    e.printStackTrace()
                                }
                                Thread.sleep((durationInSeconds * 1000).toLong() + 1000)
                                cli.ourUser.getVoiceStateForGuild(event.guild).channel?.leave()
                                sound.delete()
                            }
                            return null
                        }

                        override fun queueUpdated(position: Int) {
                        }
                    })
                    return null
                }

                override fun queueUpdated(position: Int) {
                }
            })
        }
    }
}