package org.sircypkowskyy.gaminglobbiesbot;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.sircypkowskyy.gaminglobbiesbot.Data.DataManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static JDA bot;
    public static DataManager dataManager;
    private static boolean isInDebugMode = false;
    public static String defaultBotPrefix;

    public static void main(String[] args) throws Exception {

        // Read .env file
        var dotenv = Dotenv.configure().ignoreIfMissing().load();
        var token = dotenv.get("BOT_TOKEN");
        defaultBotPrefix = dotenv.get("BOT_PREFIX");
        isInDebugMode = dotenv.get("DEBUG_MODE", "false").equals("true");
        init(token);
        dataManager = new DataManager();
        dataManager.start();
        // Check if lobbies should be destroyed after not being used for more than 1 minute by the owner
        var checkLobbiesAction = new Thread(Main::checkLobbiesValidity);
        checkLobbiesAction.start();

    }

    /**
     * Function to initialize the bot
     * @param token The token of the bot
     * @throws Exception If the bot could not be initialized properly
     */
    private static void init(String token) throws Exception {
        bot = JDABuilder.createDefault(
                        Optional.ofNullable(token
                        ).orElseThrow(
                                () -> new Exception("No bot token found")
                        ))
                .setActivity(Activity.playing("Managing Discord gaming lobbies since 1984"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .addEventListeners(new org.sircypkowskyy.gaminglobbiesbot.Commands(), new org.sircypkowskyy.gaminglobbiesbot.EventHandlers())
                .build();

        addCommands();
        bot.awaitReady();
    }

    /**
     * Function to update all commands to the bot
     */
    private static void addCommands() {
        List<CommandData> commandData = new ArrayList<>();

        // ping
        commandData.add(Commands
                .slash("ping", "Ping the bot")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // help
        commandData.add(Commands
                .slash("help", "Get help")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // register account
        commandData.add(Commands
                .slash("register-to-bot", "Register your account to bot service")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // create lobby
        commandData.add(Commands
                .slash("create-lobby", "Create a new activity lobby")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
                );

        // show active lobbies
        commandData.add(Commands
                .slash("list-lobbies", "Show all active lobbies")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // change server settings
        commandData.add(Commands
                .slash("change-server-settings", "Change server settings")
                .addOption(OptionType.CHANNEL, "category", "The category where the lobbies will be created", true)
                .addOption(OptionType.CHANNEL, "info-channel", "The channel where the new lobby info will be sent", false)
                .addOption(OptionType.STRING, "prefix", "New prefix for the bot commands", false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        // show server settings
        commandData.add(Commands
                .slash("show-server-settings", "Show server settings")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        // unregister account
        commandData.add(Commands
                .slash("unregister-from-bot", "Unregister your account from bot service")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // register server
        commandData.add(Commands
                .slash("register-server", "Register your server to bot service")
                .addOption(OptionType.CHANNEL, "category", "The category where the lobbies will be created", true)
                .addOption(OptionType.CHANNEL, "info-channel", "The channel where the new lobby info will be sent", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        // unregister server
        commandData.add(Commands
                .slash("unregister-server", "Unregister the server from bot service")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        // add activity to your profile
        commandData.add(Commands
                .slash("add-activity", "Add activity to your profile")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // remove activity from your profile
        commandData.add(Commands
                .slash("remove-activity", "Remove activity from your profile")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        // get current activities
        commandData.add(Commands
                .slash("get-activities", "Get your registered activities")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        bot.updateCommands().addCommands(commandData).queue();
    }

    /**
     * Function to check if lobbies should be destroyed after not being used for more than 1 minute since creation
     * by anyone
     */
    private static void checkLobbiesValidity() {
        System.out.println("Starting checking lobbies thread...");

        try {
            TimeUnit.SECONDS.sleep(30);
        }
        catch (InterruptedException e) {
            System.out.println("Process interrupted error");
        }
        var lobbies = dataManager.getLobbies().find().into(new ArrayList<>());
        if(lobbies.isEmpty()) return;

        System.out.println("Checking lobbies validity...");

        for (var lobby : lobbies) {
            // check only lobbies which were created more than 1 minute ago
            if(ChronoUnit.MINUTES.between(lobby.getDate("lobbyCreated").toInstant(), new Date().toInstant()) > 1) {
                var guild = bot.getGuildById(lobby.getLong("lobbyGuildId"));
                if(guild != null) {
                    var lobbyChannel = guild.getVoiceChannelById(lobby.getLong("lobbyChannelId"));
                    if(lobbyChannel != null)
                    {
                        if(lobbyChannel.getMembers().isEmpty()) {
                            lobbyChannel.delete().queue();
                            try {
                                guild.getTextChannelById(lobby.getLong("lobbyInfoMessageChannelId")).retrieveMessageById(lobby.getLong("lobbyInfoMessageId")).queue(x -> {
                                    x.delete().queue();
                                });
                            }
                            catch (Exception ignored) {
                                System.out.println("Info message not found!");
                            }
                            dataManager.getLobbies().deleteOne(lobby);
                        }
                    }
                }

            }
        }
        checkLobbiesValidity();
    }

    /**
     * Function to return info to other classes whether the bot is running in debug mode or not
     * @return true if debug mode is on, false otherwise
     */
    public static boolean getIsInDebugMode() {
        return isInDebugMode;
    }

    /**
     * Function to return the bot instance
     * @return bot instance
     */
    public static JDA getBot() {
        return bot;
    }
}
