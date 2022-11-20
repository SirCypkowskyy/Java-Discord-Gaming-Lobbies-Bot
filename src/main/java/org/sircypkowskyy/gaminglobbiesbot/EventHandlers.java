package org.sircypkowskyy.gaminglobbiesbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Functionality for handling discord events
 */
public class EventHandlers extends ListenerAdapter {

    public static String removePrefixFromStr(String s, String prefix)
    {
        if (s != null && prefix != null && s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        // Check if event happened outside a guild
        try {
            event.getGuild().getName();
        }
        catch (Exception e) {
            return;
        }

        var message = event.getMessage().getContentRaw();

        if(event.getAuthor().isBot() || event.getAuthor().isSystem())
            return;

        if(Main.getIsInDebugMode())
            System.out.println("\nNew event:\n" + message + "\nOn channel: " + event.getChannel().getId() + "\nBy: " + event.getAuthor().getName() + "\nOn guild: " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");

        var eventsPrefix = Main.dataManager.getGuildPrefix(Objects.requireNonNull(event.getGuild()).getIdLong());
        var pattern = Pattern.compile("[" + eventsPrefix+"]\\w+" , Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(message);
        if(matcher.find()) {
            var finalTxtCommand = removePrefixFromStr(message, eventsPrefix);
            switch (finalTxtCommand) {
                case "prefix" -> event.getMessage().reply("Prefix is: " + eventsPrefix).queue();
                case "getMyActivities" -> {
                    var userActivities = Main.dataManager.getAllUserRegisteredActivities(event.getAuthor().getIdLong());
                    if(userActivities != null && !userActivities.isEmpty()) {
                        event.getMessage().reply("Check DMs from bot for your registered activities").queue();
                        for (var activity : userActivities) {

                            var embed =  new EmbedBuilder()
                                    .setTitle(activity.activityName)
                                    .setDescription(activity.activityDescription)
                                    .setColor(0x00ff00)
                                    .setThumbnail(activity.activityIconURL);

                            event.getAuthor().openPrivateChannel().queue(
                                    (channel) -> channel.sendMessageEmbeds(embed.build()).queue()
                            );
                        }
                    } else {
                        event.getMessage().reply("You don't have any registered activity").queue();
                    }
                }
                default -> {
                    if(finalTxtCommand.contains("addToLobby"))
                        handleAddToLobbyCommand(event, finalTxtCommand);
                    else if(finalTxtCommand.contains("removeFromLobby"))
                        handleRemoveFromLobbyCommand(event, finalTxtCommand);
                    else if(finalTxtCommand.contains("changeLobbyName"))
                        handleChangeLobbyName(event, finalTxtCommand);
                    else if(finalTxtCommand.contains("changeLobbyDescription"))
                        handleChangeLobbyDescription(event, finalTxtCommand);
                    else if(finalTxtCommand.contains("deleteLobby"))
                        handleDeleteLobby(event, finalTxtCommand);
                }
            }
        }
    }

    private void handleDeleteLobby(MessageReceivedEvent event, String finalTxtCommand) {
        if(!isLobbyChannelOwner(event))
            return;

        event.getChannel().asVoiceChannel().delete().queue(
                (channel) -> {
                    var lobbies = Main.dataManager.getLobbies().find().into(new ArrayList<>());
                    var targetLobby = lobbies.stream().filter(lobby -> lobby.getLong("lobbyChannelId") == event.getChannel().getIdLong()).findFirst().orElse(null);
                    if(targetLobby != null)
                        Main.dataManager.getLobbies().deleteOne(targetLobby);
                    else
                        System.out.println("Minor Error - Lobby not found in database!");

                },
                (error) -> {
                    event.getMessage().reply("Error while deleting lobby").queue();
                }
        );
    }

    private void handleChangeLobbyDescription(MessageReceivedEvent event, String finalTxtCommand) {

        // TODO: Add description change functionality in lobby info message

        if(!isLobbyChannelOwner(event))
            return;

        var newDescription = finalTxtCommand.replace("changeLobbyDescription ", "");
        if(newDescription.isBlank())
        {
            event.getMessage().reply("Description cannot be empty").queue();
            return;
        }

        var lobbies = Main.dataManager.getLobbies().find().into(new ArrayList<>());
        var targetLobby = lobbies.stream().filter(lobby -> lobby.getLong("lobbyChannelId") == event.getChannel().getIdLong()).findFirst().orElse(null);
        if(targetLobby != null)
        {
            // Update lobby description
            // var message = event.getGuild().getTextChannelById(targetLobby.getLong("lobbyInfoMessageChannelId")).editMessageById()
            targetLobby.put("lobbyDescription", newDescription);
            Main.dataManager.getLobbies().replaceOne(targetLobby, targetLobby);
            event.getMessage().reply("Lobby description changed").queue();
        }
        else
        {
            System.out.println("Minor Error - Lobby not found in database!");
        }
    }

    private void handleChangeLobbyName(MessageReceivedEvent event, String finalTxtCommand) {
        // TODO: Add name change functionality in lobby info message

        if(!isLobbyChannelOwner(event))
            return;

        var newName = finalTxtCommand.replace("changeLobbyName ", "");
        if(newName.isBlank())
        {
            event.getMessage().reply("Name cannot be empty").queue();
            return;
        }

        var lobbies = Main.dataManager.getLobbies().find().into(new ArrayList<>());
        var targetLobby = lobbies.stream().filter(lobby -> lobby.getLong("lobbyChannelId") == event.getChannel().getIdLong()).findFirst().orElse(null);
        event.getChannel().asVoiceChannel().getManager().setName(newName).queue();
        event.getMessage().reply("Lobby name changed").queue();

        if(targetLobby != null)
        {
            // Update lobby name
            targetLobby.put("lobbyName", newName);
            Main.dataManager.getLobbies().replaceOne(targetLobby, targetLobby);
        }
        else
        {
            System.out.println("Minor Error - Lobby not found in database!");
        }

    }

    private void handleRemoveFromLobbyCommand(MessageReceivedEvent event, String finalTxtCommand) {
        if(!isLobbyChannelOwner(event))
            return;

        var lobby = event.getChannel().asVoiceChannel();
        try{
            var usersToBeRemoved = event.getMessage().getMentions().getMembers();
            if(usersToBeRemoved.isEmpty())
            {
                event.getMessage().reply("You need to mention users to remove from the lobby").queue();
                return;
            }
            for (var userToBeRemovedId : usersToBeRemoved)
            {
                if(userToBeRemovedId.getIdLong() == event.getAuthor().getIdLong())
                {
                    event.getMessage().reply("You can't remove yourself from the allowed users in the lobby").queue();
                    return;
                }

                var manager = event.getChannel().asVoiceChannel().getManager();

                var userToBeRemoved = event.getGuild().getMemberById(userToBeRemovedId.getIdLong());
                manager.putPermissionOverride(
                        Objects.requireNonNull(userToBeRemoved),
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT),
                        EnumSet.of(Permission.VOICE_MOVE_OTHERS)
                ).queue();

                event.getMessage().reply("User <@" + userToBeRemovedId  + "> was removed from the lobby").queue();
            }
        }
        catch (Exception ignored)
        {
            event.getMessage().reply("Please provide a proper user to be removed from the lobby!").queue();
        }

    }

    private void handleAddToLobbyCommand(MessageReceivedEvent event, String finalTxtCommand) {
        if(!isLobbyChannelOwner(event)) {
            return;
        }
            try{
                var usersToBeAdded = event.getMessage().getMentions().getMembers();
                if(usersToBeAdded.isEmpty())
                {
                    event.getMessage().reply("You need to mention users to add to lobby").queue();
                    return;
                }
                for (var userToBeAddedId : usersToBeAdded)
                {
                    if(userToBeAddedId.getIdLong() == event.getAuthor().getIdLong())
                    {
                        event.getMessage().reply("You can't add yourself to the lobby").queue();
                        return;
                    }

                    var manager = event.getChannel().asVoiceChannel().getManager();

                    var userToBeAdded = event.getGuild().getMemberById(userToBeAddedId.getIdLong());
                    manager.putPermissionOverride(
                            Objects.requireNonNull(userToBeAdded),
                            EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT),
                            EnumSet.of(Permission.VOICE_MOVE_OTHERS)
                    ).queue();

                    event.getMessage().reply("User <@" + userToBeAdded.getIdLong()  + "> added to lobby").queue();
                }
            }
            catch (Exception ignored)
            {
                event.getMessage().reply("Please provide a proper user to be added").queue();
            }
    }

    private boolean isLobbyChannelOwner(MessageReceivedEvent event) {
        try {
            event.getChannel().asVoiceChannel();
        }
        catch (Exception e) {
            return false;
        }
        return Main.dataManager.isUserOwnerOfLobby(event.getAuthor().getIdLong(), event.getChannel().getIdLong());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        // Commented for now
        // Lobbies shouldn't be deleted when the host leaves due to the possibility of the host unexpectedly leaving

//        List<Document> lobbies = Main.dataManager.getLobbies().find().into(new ArrayList<>());
//        var lobby = lobbies.stream().filter(x -> x.getLong("lobbyChannelId") == event.getChannelLeft().getIdLong()).findFirst();
//
//        if(lobby.isEmpty())
//            return;
//
//        if(event.getMember().getIdLong() == lobby.get().getLong("lobbyUserOwnerId")) {
//            try {
//                event.getGuild().getTextChannelById(lobby.get().getLong("lobbyInfoMessageChannelId")).retrieveMessageById(lobby.get().getLong("lobbyInfoMessageId")).queue(x -> {
//                    x.delete().queue();
//                });
//            }
//            catch (Exception ignored) {
//                System.out.println("Info message not found!");
//            }
//            Main.dataManager.getLobbies().deleteOne(lobby.get());
//            if(event.getGuild().getVoiceChannelById(lobby.get().getLong("lobbyChannelId")) != null)
//                event.getGuild().getVoiceChannelById(lobby.get().getLong("lobbyChannelId")).delete().queue();
//        }
    }
}
