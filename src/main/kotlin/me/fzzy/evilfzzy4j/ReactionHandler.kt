package me.fzzy.evilfzzy4j

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object ReactionHandler : ListenerAdapter() {

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if (event.user.idLong == Bot.client.selfUser.idLong) return
        val msg = event.channel.retrieveMessageById(event.messageId).complete() ?: return
        if (event.user.idLong == msg.author.idLong) return

        val guild = FzzyGuild.getGuild(event.guild.id)

        guild.saveMessage(msg)
    }

}