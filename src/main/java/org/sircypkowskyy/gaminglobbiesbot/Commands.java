package org.sircypkowskyy.gaminglobbiesbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.LobbyModel;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.ServerModel;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Commands extends ListenerAdapter {

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

        if(Main.getIsInDebugMode() && (!event.getAuthor().isBot() && !event.getAuthor().isSystem()))
            System.out.println("\nNew event:\n" + message + "\nBy: " + event.getAuthor().getName() + "\nOn guild: " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");


        // var eventsPrefix = SQLiteDataSource.getGuildPrefix(Objects.requireNonNull(event.getGuild()).getIdLong());
        var eventsPrefix = Main.dataManager.getGuildPrefix(Objects.requireNonNull(event.getGuild()).getIdLong());
        var pattern = Pattern.compile("[" + eventsPrefix+"]\\w+" , Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(message);
        if(matcher.find()) {
            switch (removePrefixFromStr(message, eventsPrefix)) {
                case "prefix" -> {
                    event.getMessage().reply("Prefix is: " + eventsPrefix).queue();
                }
                case "getMyActivities" -> {
                    if(Objects.requireNonNull(event.getMember()).getActivities().size() > 0) {
                        event.getMessage().reply("Your activities are: ").queue();
                        for (var activity : event.getMember().getActivities()) {
                            if(activity.isRich())
                            {
                                var embed =  new EmbedBuilder()
                                        .setTitle(activity.getName())
                                        .setDescription(activity.getUrl())
                                        .setColor(0x00ff00);

                                if(activity.asRichPresence().getLargeImage() != null)
                                    embed.setThumbnail(activity.asRichPresence().getLargeImage().getUrl());

                                event.getMessage().replyEmbeds(
                                        embed.build()
                                ).queue();
                            }
                        }
                    } else {
                        event.getMessage().reply("You don't have any activity").queue();
                    }
                }
                default -> {}
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        switch (event.getComponentId())
        {
            case "select-activity" -> registerNewLobby(event);
            case "select-activity-to-add" -> addActivityToUser(event);
            case "select-activity-to-remove" -> removeActivityFromUser(event);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(event.getComponentId().startsWith("join-lobby-"))
            handleButtonJoinLobby(event, Long.parseLong(event.getComponentId().replace("join-lobby-", "")));
        else if(event.getComponentId().startsWith("leave-lobby-"))
            handleButtonLeaveLobby(event, Long.parseLong(event.getComponentId().replace("leave-lobby-", "")));
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        switch (event.getModalId())
        {
            case "register-to-bot" -> handleRegisterToBot(event);
            case "register-new-lobby" -> handleRegisterNewLobby(event);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping" -> event.reply("Pong!").setEphemeral(true).queue();
            case "list-lobbies" -> listLobbiesOnServer(event);
            case "register-to-bot" -> registerToBot(event);
            case "create-lobby" -> createLobby(event);
            case "change-server-settings" -> changeServerSettings(event);
            case "unregister-from-bot" -> unregisterFromBot(event);
            case "register-server" -> registerServer(event);
            case "unregister-server" -> unregisterServer(event);
            case "show-server-settings" -> showServerSettings(event);
            case "add-activity" -> addActivity(event);
            case "remove-activity" -> removeActivity(event);
            default -> {}
        }
    }

    private void handleButtonJoinLobby(@NotNull ButtonInteractionEvent event, long lobbyId) {
        var user = event.getUser();
        var lobby = event.getGuild().getVoiceChannelById(lobbyId);
        // check if user is the owner of the lobby
        var lobbyModel = Main.dataManager.getLobbies().find().into(new ArrayList<>());
        var potentialLobby = lobbyModel.stream().filter(x -> x.getLong("lobbyChannelId") == lobbyId).findFirst().orElse(null);
        if(potentialLobby != null && potentialLobby.getLong("lobbyUserOwnerId") == user.getIdLong()) {
            event.reply("You are the owner of this lobby\nYou can't join your own lobby again!").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }

        if(lobby != null) {
            var lobbyManager = lobby.getManager();
            lobbyManager.putPermissionOverride(Objects.requireNonNull(event.getMember()), EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), EnumSet.of(Permission.VOICE_MOVE_OTHERS)).queue();
        }
        event.reply("You have joined the lobby successfully").setEphemeral(true).queue(
                message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
        );
    }

    private void handleButtonLeaveLobby(@NotNull ButtonInteractionEvent event, long lobbyId) {
        var user = event.getUser();
        var lobby = event.getGuild().getVoiceChannelById(lobbyId);
        var actionReplied = false;
        if(lobby != null) {
            var lobbies = Main.dataManager.getLobbies();
            var potentialLobby = lobbies.find().into(new ArrayList<>()).stream().filter(x -> x.getLong("lobbyChannelId") == lobbyId).findFirst().orElse(null);
            if(potentialLobby != null && potentialLobby.getLong("lobbyUserOwnerId") == user.getIdLong() ) {
                try {
                    event.getChannel().retrieveMessageById(potentialLobby.getLong("lobbyInfoMessageId")).queue(x -> {
                        x.delete().queue();
                    });
                }
                catch (Exception ignored) {
                    System.out.println("Info message not found!");
                }
                lobby.delete().queue();
                event.reply("Lobby deleted!").setEphemeral(true).queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
                lobbies.deleteOne(potentialLobby);
                actionReplied = true;
            }
            else {
                var lobbyManager = lobby.getManager();
                lobbyManager.removePermissionOverride(Objects.requireNonNull(event.getMember())).queue();
            }

        }
        if(!actionReplied)
            event.reply("You have left the lobby successfully").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
    }

    private void removeActivity(@NotNull SlashCommandInteractionEvent event) {
        // Check if user exists in database
        if(!Main.dataManager.doesUserExist(event.getUser().getIdLong()))
        {
            event.reply("You are not registered to bot. Use `/register-to-bot` to register").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }
        var selectMenu = SelectMenu.create("select-activity-to-remove").setRequiredRange(1, 1);
        for (var activity : event.getMember().getActivities()) {
            if(activity.isRich())
            {
                selectMenu.addOption(activity.getName(), activity.getName());
            }
        }
        event.reply("Select activity you want to want to unfollow")
                .addActionRow(selectMenu.build())
                .setEphemeral(true)
                .queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
    }

    private void removeActivityFromUser(@NotNull SelectMenuInteractionEvent event) {
        var activityName = event.getValues().get(0);
        var activity = event.getMember().getActivities().stream().filter(x -> x.getName().equals(activityName)).findFirst().orElse(null);
        if(activity == null)
        {
            event.reply("You don't follow this activity").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }
        Main.dataManager.unregisterUserActivity(event.getUser().getIdLong(), activity.asRichPresence().getApplicationIdLong());
        event.reply("You unfollowed " + activity.getName() + " activity.").setEphemeral(true).queue(
                message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
        );
    }

    private void addActivity(@NotNull SlashCommandInteractionEvent event) {
        // Check if user exists in database
        if(!Main.dataManager.doesUserExist(event.getUser().getIdLong()))
        {
            event.reply("You are not registered to bot. Use /register-to-bot to register").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }
        var selectMenu = SelectMenu.create("select-activity-to-add").setRequiredRange(1, 1);
        for (var activity : event.getMember().getActivities()) {
            if(activity.isRich())
            {
                selectMenu.addOption(activity.getName(), activity.getName());
            }
        }
        event.reply("Select activity you want to want to follow")
                .addActionRow(selectMenu.build())
                .setEphemeral(true)
                .queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
    }

    private void addActivityToUser(@NotNull SelectMenuInteractionEvent event) {
        var activityName = event.getValues().get(0);
        var activity = event.getMember().getActivities().stream().filter(x -> x.getName().equals(activityName)).findFirst().orElse(null);
        if(activity == null)
        {
            event.reply("Activity not found").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }
        var activityId = activity.asRichPresence().getApplicationIdLong();
        if(!Main.dataManager.doesUserHaveActivity(event.getUser().getIdLong(),activityId))
        {
            Main.dataManager.registerNewUserActivity(event.getUser().getIdLong(), activityId);
            event.reply("Activity added").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
        } else {
            event.reply("You already follow this activity").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
        }
    }

    private void registerServer(@NotNull SlashCommandInteractionEvent event) {

        if(Main.dataManager.getGuildSettings(event.getGuild().getIdLong()) != null)
        {
            event.reply("This server is already registered").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }

        var serverId = event.getGuild().getIdLong();
        var category = Objects.requireNonNull(event.getOption("category")).getAsChannel().getIdLong();

        if(event.getGuild().getCategoryById(category) == null)
        {
            event.reply("**ERROR!**\nYou chose normal channel instead of channels Category for temporary server lobbies!").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }
        long infoChannelID = 0;
        if(event.getOption("info-channel") != null)
        {
            infoChannelID = event.getOption("info-channel").getAsChannel().getIdLong();
            if(event.getGuild().getTextChannelById(infoChannelID) == null)
            {
                event.reply("**ERROR!**\nYou chose different channel instead of Text Channel for new lobbies on server info channel!").setEphemeral(true).queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
                return;
            }
        }
        Main.dataManager.registerServer(serverId, category, infoChannelID);
        event.reply("Server registered").setEphemeral(true).queue(
                message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
        );
    }

    private void showServerSettings(@NotNull SlashCommandInteractionEvent event) {
        var guildSettings = Main.dataManager.getGuildSettings(Objects.requireNonNull(event.getGuild()).getIdLong());

        if(guildSettings == null)
        {
            event.reply("This server is not registered to the bot").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }
        event.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Server settings")
                        .setColor(0x00ff00)
                        .setDescription("Settings of the server " + event.getGuild().getName())
                        .setAuthor("GamingLobbiesBot", null, event.getJDA().getSelfUser().getAvatarUrl())
                        .setTimestamp(event.getTimeCreated())
                        .addField("Prefix", guildSettings.guildBotPrefix, false)
                        .addField("Lobby category", event.getGuild().getCategoryById(guildSettings.guildLobbiesCategoryId) != null
                                ? event.getGuild().getCategoryById(guildSettings.guildLobbiesCategoryId).getName() : "**lobbies category not set**", false)
                        .addField("Lobbies announcement channel ", event.getGuild().getTextChannelById(guildSettings.guildLobbyInfoChannelId) != null ?
                                "<#" + Long.toString(guildSettings.guildLobbyInfoChannelId) + ">" : "Not set!", false)
                        .build()
        ).queue();
    }

    private void unregisterServer(@NotNull SlashCommandInteractionEvent event) {
        if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR))
        {
            Main.dataManager.unregisterGuild(Objects.requireNonNull(event.getGuild()).getIdLong());

            System.out.println("Unregistered server " + event.getGuild().getName());
            System.out.println("Deleting all lobbies from server " + event.getGuild().getName());
            // Delete all lobbies on guild
            var lobbies = Main.dataManager.getLobbies().find().into(new ArrayList<>());
                for (var lobby : lobbies) {
                    if(lobby.getLong("lobbyGuildId") == event.getGuild().getIdLong())
                    {
                        // Check if lobby still exists
                        if(event.getGuild().getVoiceChannelById(lobby.getLong("lobbyChannelId")) != null)
                        {
                            event.getGuild().getVoiceChannelById(lobby.getLong("lobbyChannelId")).delete().queue();
                        }
                        Main.dataManager.getLobbies().deleteOne(lobby);
                    }
            }
            System.out.println("Deleted all lobbies from server " + event.getGuild().getName());

            event.reply("Server unregistered").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
        } else {
            event.reply("You don't have permission to do that").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
        }
    }

    private void unregisterFromBot(@NotNull SlashCommandInteractionEvent event) {
        Main.dataManager.unregisterUser(event.getUser().getIdLong());
        event.reply("You have been unregistered from the bot").setEphemeral(true).queue(
                message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
        );
    }


    private void changeServerSettings(@NotNull SlashCommandInteractionEvent event) {
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildSettings = Main.dataManager.getGuildSettings(guildId);

        String guildPrefix = guildSettings.guildBotPrefix;

        if(event.getOption("prefix") != null)
            guildPrefix = Objects.requireNonNull(event.getOption("prefix")).getAsString();

        var lobbiesCategory = event.getOption("category").getAsChannel().getIdLong();

        // check if lobbiesCategory is a category
        if(event.getGuild().getCategoryById(lobbiesCategory) == null)
        {
            event.reply("Lobbies category must be a category!").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }

        long lobbiesAnnouncementChannel = 0;
        if(event.getOption("info-channel") != null) {
            lobbiesAnnouncementChannel = event.getOption("info-channel").getAsChannel().getIdLong();
            if(event.getGuild().getTextChannelById(lobbiesAnnouncementChannel) == null)
            {
                event.reply("Lobbies announcement channel must be a text channel!").setEphemeral(true).queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
                return;
            }
        }

        Main.dataManager.modifyGuildSettings(new ServerModel(guildId, guildPrefix, lobbiesCategory, lobbiesAnnouncementChannel));
        event.reply("Server settings changed").setEphemeral(true).queue(
                message -> {
                    message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS);
                }
        );
    }

    private void createLobby(@NotNull SlashCommandInteractionEvent event) {

        if(!Main.dataManager.doesUserExist(event.getUser().getIdLong()))
        {
            event.reply("You are not registered to the bot!\nRegister first to create lobbies!").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
            );
            return;
        }

        if(Main.dataManager.doesUserHasRegisteredLobby(event.getUser().getIdLong()))
        {
            event.reply("You already have a lobby!\nExit or wait for your lobby to expire in order to be able to create new lobby!").setEphemeral(true).queue(
                    message -> message.deleteOriginal().queueAfter(20, TimeUnit.SECONDS)
            );
            return;
        }

        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildSettings = Main.dataManager.getGuildSettings(guildId);
        if(guildSettings != null) {
            if(guildSettings.guildLobbiesCategoryId == 0) {
                event.reply("This server is not setup for this bot yet!\nServer needs to be registered").setEphemeral(true).queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
                return;
            }
            // try to get category and new lobbies info channel
            var category = event.getGuild().getCategoryById(guildSettings.guildLobbiesCategoryId);
            if(category == null) {
                event.reply("Lobbies category is not set or was deleted!").setEphemeral(true).queue(
                        message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
                );
                return;
            }

            var infoChannel = event.getGuild().getTextChannelById(guildSettings.guildLobbyInfoChannelId);
            if(infoChannel == null) {
                event.reply("Lobbies info channel is not set or was deleted!").setEphemeral(true).queue(
                        s -> {
                            s.deleteOriginal().queueAfter(30, TimeUnit.SECONDS);
                        }
                );
                return;
            }

            var userActivities = event.getMember().getActivities().stream().filter(Activity::isRich).toList();
            if(!userActivities.isEmpty()) {
                var selectMenu = SelectMenu.create("select-activity").setRequiredRange(1, 1);
                for (var activity : userActivities) {
                        selectMenu.addOption(activity.getName(), Objects.requireNonNull(activity.asRichPresence()).getApplicationId(), Emoji.fromUnicode("\uD83C\uDFAE"));
                }
                event.reply("Choose activity for your lobby")
                        .addActionRow(selectMenu.build())
                        .setEphemeral(true)
                        .queue(s ->{
                            s.deleteOriginal().queueAfter(30, TimeUnit.SECONDS);
                        });
            }
            else
            {
                event.reply("You need to have at least one activity active in order to create lobby!").setEphemeral(true).queue(
                        s ->{
                            s.deleteOriginal().queueAfter(30, TimeUnit.SECONDS);
                        });
            }

        }
        else {
            event.reply("This server is not setup for this bot yet!\nServer needs to be registered").setEphemeral(true).queue(
                    s ->{
                        s.deleteOriginal().queueAfter(30, TimeUnit.SECONDS);
                    });
        }
    }

    // for lobbies based on discord activities
    private void registerNewLobby(@NotNull SelectMenuInteractionEvent event) {


        var chosenActivity = event.getValues().get(0);

        var lobbyName = TextInput.create("lobby-name", "Lobby name", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("Lobby name")
                .build();

        var lobbyDescription = TextInput.create("lobby-description", "Lobby description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Lobby description")
                .build();

        var lobbyMaxPlayers = TextInput.create("lobby-max-players", "Max players (0 for no limits)", TextInputStyle.SHORT)
                .setValue("0")
                .setRequired(true)
                .setMinLength(1)
                .setMaxLength(1)
                .build();

        var activityId = TextInput.create("activity-id", "Activity ID (DON'T CHANGE)", TextInputStyle.SHORT)
                .setValue(chosenActivity)
                .build();

        var modal = Modal.create("register-new-lobby", "Register new lobby")
                .addActionRows(
                        ActionRow.of(lobbyName),
                        ActionRow.of(lobbyDescription),
                        ActionRow.of(lobbyMaxPlayers),
                        ActionRow.of(activityId)
                )
                .build();

        event.replyModal(modal).queue();

    }

    private void registerToBot(@NotNull SlashCommandInteractionEvent event) {
        if(Main.dataManager.doesUserExist(event.getUser().getIdLong()))
        {
            event.reply("You are already in the database!").setEphemeral(true).queue();
            return;
        }

        var getDMs = TextInput.create("get-dms", "Receive DMs about new lobbies (yes/no)", TextInputStyle.SHORT)
                .setPlaceholder("yes/no")
                .setRequired(true)
                .setMaxLength(3)
                .setMinLength(2)
                .build();

        var modal = Modal.create("register-to-bot", "Register your account to bot service")
                .addActionRows(ActionRow.of(getDMs))
                .build();
        event.replyModal(modal).queue();
    }

    private void handleRegisterToBot(@Nonnull ModalInteractionEvent event) {
        boolean getDMs = event.getValue("get-dms").getAsString().equals("yes");
        Main.dataManager.registerNewUser(getDMs, event);
    }

    private void handleRegisterNewLobby(@NotNull ModalInteractionEvent event) {
        var guildSettings = Main.dataManager.getGuildSettings(event.getGuild().getIdLong());
        var lobbyName = Objects.requireNonNull(event.getValue("lobby-name")).getAsString();
        var lobbyDescription = Objects.requireNonNull(event.getValue("lobby-description")).getAsString();
        var lobbyActivityId = Long.parseLong(Objects.requireNonNull(event.getValue("activity-id")).getAsString());

        int lobbyMaxPlayers = 0;
        try {
            lobbyMaxPlayers = Integer.parseInt(event.getValue("lobby-max-players").getAsString());
        }
        catch (NumberFormatException ignored) {}

        var newChannel = event.getGuild().createVoiceChannel(lobbyName)
                .setParent(event.getGuild().getCategoryById(guildSettings.guildLobbiesCategoryId))
                .addMemberPermissionOverride(Objects.requireNonNull(event.getMember()).getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.MANAGE_CHANNEL), EnumSet.of(Permission.VOICE_MOVE_OTHERS))
                .addPermissionOverride(event.getGuild().getPublicRole(), EnumSet.noneOf(Permission.class), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();


        var activity = event.getMember().getActivities().stream().filter(a -> a.isRich() && a.asRichPresence().getApplicationIdLong() == lobbyActivityId).findFirst().orElseThrow();

        var botSelfAvatar = event.getJDA().getSelfUser().getAvatarUrl();
        Long messageID = null;
        String messageLink = null;
        if(guildSettings.guildLobbyInfoChannelId != 0) {
            var lobbyInfoEmbed = new EmbedBuilder()
                    .setTitle("New game lobby!")
                    .setDescription("New " + activity.getName() + " lobby has been created!")
                    .setThumbnail(activity.asRichPresence().getLargeImage() == null ? (activity.asRichPresence().getSmallImage() == null ? botSelfAvatar : activity.asRichPresence().getSmallImage().getUrl()) : activity.asRichPresence().getLargeImage().getUrl())
                    .setTimestamp(Instant.now())
                    .setAuthor(event.getMember().getUser().getAsTag(), null, event.getMember().getUser().getEffectiveAvatarUrl())
                    .addField("Lobby name", lobbyName, false)
                    .addField("Activity", activity.getName(), false)
                    .addField("Lobby description", lobbyDescription, false)
                    .addField("Lobby Channel", "<#" + newChannel.getId() + ">", false)
                    .addField("Lobby Host", "<@" + event.getMember().getId() + ">", false)
                    .addField("Max players", lobbyMaxPlayers == 0 ? "No limits" : Integer.toString(lobbyMaxPlayers), false)
                    .setColor(Color.RED);
            int finalLobbyMaxPlayers = lobbyMaxPlayers;
            event.getGuild().getTextChannelById(guildSettings.guildLobbyInfoChannelId).sendMessageEmbeds(lobbyInfoEmbed.build())
                    .setActionRows(ActionRow.of(Button.primary("join-lobby-" + newChannel.getId(), "Join lobby")), ActionRow.of(Button.danger("leave-lobby-" + newChannel.getId(), "Leave lobby")))
                    .queue(
                            message -> {
                                var newLobby = new LobbyModel(newChannel.getIdLong(),
                                        event.getGuild().getIdLong(),
                                        event.getMember().getIdLong(),
                                        lobbyActivityId,
                                        finalLobbyMaxPlayers,
                                        new Date(),
                                        message.getIdLong(),
                                        event.getGuild().getTextChannelById(guildSettings.guildLobbyInfoChannelId).getIdLong()
                                );
                                registerNewLobby(newLobby);
                            }
                    );
            messageLink = event.getGuild().getTextChannelById(guildSettings.guildLobbyInfoChannelId).getLatestMessageId();
            messageLink = "https://discord.com/channels/" + event.getGuild().getId() + "/" + guildSettings.guildLobbyInfoChannelId + "/" + messageLink;
        }

        var usersOnServer = event.getGuild().getMembers();

        var botUsers = Main.dataManager.getAllUsersFromIdsList(usersOnServer.stream().map(ISnowflake::getIdLong).toList());

        if(botUsers != null && !botUsers.isEmpty())
        {
            var embed = new EmbedBuilder()
                    .setTitle("New " + activity.getName() +" lobby on " + event.getGuild().getName() + " server!")
                    .setDescription("New lobby has been created on " + event.getGuild().getName() + " by " + "<@" + event.getMember().getId() + ">" + "!")
                    .setColor(Color.RED)
                    .setThumbnail(activity.asRichPresence().getLargeImage() == null ? (activity.asRichPresence().getSmallImage() == null ? botSelfAvatar : activity.asRichPresence().getSmallImage().getUrl()) : activity.asRichPresence().getLargeImage().getUrl())
                    .setTimestamp(Instant.now())
                    .addField("Lobby name", lobbyName, false)
                    .addField("Activity", activity.getName(), false)
                    .addField("Lobby description", lobbyDescription, false)
                    .addField("Lobby Channel", "<#" + newChannel.getId() + ">", false)
                    .addField("Lobby Host", "<@" + event.getMember().getId() + ">", false)
                    .addField("Max players", lobbyMaxPlayers == 0 ? "No limits" : Integer.toString(lobbyMaxPlayers), false)
                    .build();
            for(var user : botUsers) {
                if(user.acceptDMs && user.userId != event.getMember().getIdLong())
                {
                    var userOnServer = usersOnServer.stream().filter(m -> m.getIdLong() == user.userId).findFirst().orElse(null);
                        userOnServer.getUser().openPrivateChannel().queue(c -> c.sendMessageEmbeds(embed)
                                .queue(
                                        message -> message.delete().queueAfter(3, TimeUnit.HOURS)
                                ));
                        if(messageLink != null)
                        {
                            String finalMessageLink = messageLink;
                            userOnServer.getUser().openPrivateChannel().queue(c -> c.sendMessage(finalMessageLink)
                                    .queue(
                                            message -> message.delete().queueAfter(3, TimeUnit.HOURS)
                                    ));
                        }
                        // userOnServer.getUser().openPrivateChannel().queue(c -> c.sendMessage("https://discord.com/channels/" + event.getGuild().getId() + "/" + newChannel.getId()).queue());
                }
            }
        }
        event.reply("Lobby created!").setEphemeral(true).queue(
                message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
        );
    }

    private void listLobbiesOnServer(@NotNull SlashCommandInteractionEvent event) {
        event.reply("List of lobbies on this server:").setEphemeral(true).queue(
                message -> message.deleteOriginal().queueAfter(30, TimeUnit.SECONDS)
        );
    }

    private void registerNewLobby(LobbyModel newLobby) {
        Main.dataManager.registerNewLobby(newLobby);
    }
}
