package org.sircypkowskyy.gaminglobbiesbot;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.sircypkowskyy.gaminglobbiesbot.Data.Datamanager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {


    private static JDA bot;
    public static Datamanager dataManager;

    private static boolean isInDebugMode = false;
    public static String defaultBotPrefix;

    public static void main(String[] args) throws Exception {
        // Read .env file
        var dotenv = Dotenv.configure().load();
        var token = dotenv.get("BOT_TOKEN");
        defaultBotPrefix = dotenv.get("BOT_PREFIX");
        isInDebugMode = dotenv.get("DEBUG_MODE").equals("true");
        init(token);
        dataManager = new Datamanager();
        dataManager.start();


    }

    private static void init(String token) throws Exception {
        bot = JDABuilder.createDefault(
                        Optional.ofNullable(token
                        ).orElseThrow(
                                () -> new Exception("No bot token found")
                        ))
                .setActivity(Activity.playing("Managing Discord gaming lobbies..."))
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

    private static void addCommands() {
        List<CommandData> commandData = new ArrayList<>();

        // ping
        commandData.add(Commands
                .slash("ping", "Ping the bot")
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
                .addOption(OptionType.CHANNEL, "info-channel", "The channel where the new lobby info will be sent", false)
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

        bot.updateCommands().addCommands(commandData).queue();
    }

    public static boolean getIsInDebugMode() {
        return isInDebugMode;
    }
    public static JDA getBot() {
        return bot;
    }
}
