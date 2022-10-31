package org.sircypkowskyy.gaminglobbiesbot;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventHandlers extends ListenerAdapter {

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        List<Document> lobbies = Main.dataManager.getLobbies().find().into(new ArrayList<>());
        var lobby = lobbies.stream().filter(x -> x.getLong("lobbyChannelId") == event.getChannelLeft().getIdLong()).findFirst();
        if(lobby.isEmpty())
            return;

        if(event.getMember().getIdLong() == lobby.get().getLong("lobbyUserOwnerId")) {
            Main.dataManager.getLobbies().deleteOne(lobby.get());
            if(event.getGuild().getVoiceChannelById(lobby.get().getLong("lobbyChannelId")) != null)
                event.getGuild().getVoiceChannelById(lobby.get().getLong("lobbyChannelId")).delete().queue();
        }
    }
}
