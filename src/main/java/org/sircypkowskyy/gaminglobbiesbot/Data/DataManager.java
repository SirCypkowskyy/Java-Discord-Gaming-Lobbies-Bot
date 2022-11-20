package org.sircypkowskyy.gaminglobbiesbot.Data;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.bson.Document;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.LobbyModel;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.ServerModel;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.UserModel;
import org.sircypkowskyy.gaminglobbiesbot.Data.POJOs.Activity;
import org.sircypkowskyy.gaminglobbiesbot.Main;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * DataManager class is responsible for managing all data operations.
 */
public class DataManager extends Thread {

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> serversCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> lobbiesCollection;

    @Override
    public void run() {
        super.run();
        connectClient();
        connectToDatabase();
        populateDatabase();
    }

    /**
     * Function responsible for connecting to MongoDB client.
     */
    private void connectClient() {
        var dotenv = Dotenv.configure().ignoreIfMissing().load();
        var pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        var codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);

        var clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(dotenv.get("MONGO_URL", "mongodb://localhost/app")))
                .codecRegistry(codecRegistry)
                .build();

        mongoClient = MongoClients.create(clientSettings);
        // Check if connection is successful
        System.out.println("Connected to MongoDB!\nGetting all databases...");
    }

    /**
     * Function responsible for connecting to MongoDB bot database.
     */
    private void connectToDatabase() {
        database = mongoClient.getDatabase("GamingLobbiesBot");
        System.out.println("Connected to database: " + database.getName());
    }

    /**
     * Function responsible for populating database with collections.
     */
    private void populateDatabase() {
        var collectionExists = database.listCollectionNames().into(new ArrayList<>());
        // Servers
        if (!collectionExists.contains("Server")) {
            database.createCollection("Server");
            System.out.println("Created collection: Server");
        }
        else
            System.out.println("Collection already exists: Server");
        // Users
        if (!collectionExists.contains("User")) {
            database.createCollection("User");
            System.out.println("Created collection: User");
        }
        else
            System.out.println("Collection already exists: User");

        // Lobbies
        if (!collectionExists.contains("Lobby")) {
            database.createCollection("Lobby");
            System.out.println("Created collection: Lobby");
        }
        else
            System.out.println("Collection already exists: Lobby");

        serversCollection = database.getCollection("Server");
        usersCollection = database.getCollection("User");
        lobbiesCollection = database.getCollection("Lobby");

    }

    /**
     * Function responsible for closing MongoDB client.
     */
    private void closeConnection() {
        mongoClient.close();
    }

    /**
     * Function responsible for registration of new server (guild) to the database.
     * @param guildID ID of the guild.
     */
    public void registerServer(long guildID) {
        var newServer = new ServerModel(guildID, Main.defaultBotPrefix, 0, 0);
        serversCollection.insertOne(ServerModel.toDocument(newServer));
        System.out.println("Created new server in database: " + guildID);
    }

    /**
     * Function responsible for registration of new server (guild) to the database.
     * @param guildID ID of the guild.
     * @param lobbiesCategoryID ID of the lobbies category.
     */
    public void registerServer(long guildID, long lobbiesCategoryID) {
        var newServer = new ServerModel(guildID, Main.defaultBotPrefix, lobbiesCategoryID, 0);
        serversCollection.insertOne(ServerModel.toDocument(newServer));
        System.out.println("Created new server in database: " + guildID);
    }

    /**
     * Function responsible for registration of new server (guild) to the database.
     * @param guildID ID of the guild.
     * @param lobbiesCategoryID ID of the lobbies category.
     * @param lobbiesInfoTextChannel ID of the lobbies info text channel.
     */
    public void registerServer(long guildID, long lobbiesCategoryID, long lobbiesInfoTextChannel) {
        var newServer = new ServerModel(guildID, Main.defaultBotPrefix, lobbiesCategoryID, lobbiesInfoTextChannel);
        serversCollection.insertOne(ServerModel.toDocument(newServer));
        System.out.println("Created new server in database: " + guildID);
    }

    /**
     * Function responsible for registration of new user to the database.
     * @param acceptDMs Boolean value indicating if user accepts DMs from the bot.
     * @param event Modal interaction event.
     */
    public void registerNewUser(boolean acceptDMs, @Nonnull ModalInteractionEvent event) {

        if(doesUserExist(event.getUser().getIdLong()))
        {
            System.out.println("User already exists in database: " + event.getUser().getName());
            event.reply("You are already registered!").setEphemeral(true).queue();
        }
        else {
            // create new user
            var newUser = new UserModel(event.getUser().getIdLong(), new ArrayList<>(), acceptDMs);
            usersCollection.insertOne(UserModel.toDocument(newUser));
            System.out.println("Created new user in database: " + event.getUser().getName());
            event.reply("You are now registered!").setEphemeral(true).queue();
        }
    }

    /**
     * Function responsible for registration of new user's activity to the database.
     * @param userId ID of the user.
     * @param activity activity to be registered.
     */
    public void registerNewUserActivity(long userId, Activity activity) {
        var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
        assert user != null;
        var userActivities = user.getList("userRegisteredActivities", Document.class);
        userActivities.add(Activity.toDocument(activity));
        usersCollection.updateOne(new BasicDBObject("userId", userId), new BasicDBObject("$set", new BasicDBObject("userRegisteredActivities", userActivities)));
    }

    /**
     * Function responsible for removing user's activity from the database.
     * @param userId ID of the user.
     * @param activityId ID of the activity.
     */
    public void unregisterUserActivity(long userId, long activityId) {
        var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
        assert user != null;
        var userActivities = user.getList("userRegisteredActivities", Document.class);
        userActivities.removeIf(activity -> activity.getLong("activityId") == activityId);
        usersCollection.updateOne(new BasicDBObject("userId", userId), new BasicDBObject("$set", new BasicDBObject("userRegisteredActivities", userActivities)));
    }

    /**
     * Function responsible for registration of new lobby to the database.
     * @param newLobby Lobby model.
     */
    public void registerNewLobby(LobbyModel newLobby) {
        lobbiesCollection.insertOne(LobbyModel.toDocument(newLobby));
    }

    /**
     * Function responsible for answering if guild exists in the database.
     * @param guildID ID of the guild.
     * @return Boolean value indicating if guild exists in the database.
     */
    public boolean doesGuildExist(long guildID) {
        var server = new BasicDBObject("guildId", guildID);
        var serverExists = serversCollection.find(server).first();
        return serverExists != null;
    }

    /**
     * Function responsible for answering if user exists in the database.
     * @param userID ID of the user.
     * @return Boolean value indicating if user exists in the database.
     */
    public boolean doesUserExist(long userID) {
        var user = new BasicDBObject("userId", userID);
        var userExists = usersCollection.find(user).first();
        return userExists != null;
    }

    /**
     * Function responsible for answering if user owns any active lobby.
     * @param userID ID of the user.
     * @return Boolean value indicating if user owns any active lobby.
     */
    public boolean doesUserHasRegisteredLobby(long userID) {
        var lobby = new BasicDBObject("lobbyUserOwnerId", userID);
        var lobbyExists = lobbiesCollection.find(lobby).first();
        return lobbyExists != null;
    }

    /**
     * Function responsible for answering if user has given activity registered.
     * @param userID ID of the user.
     * @param activityID ID of the activity.
     * @return Boolean value indicating if user has given activity registered.
     */
    public boolean doesUserHaveActivity(long userID, long activityID) {
        var user = new BasicDBObject("userId", userID);
        var userExists = usersCollection.find(user).first();
        assert userExists != null;
        var userActivities = userExists.getList("userRegisteredActivities", Document.class);
        return userActivities.stream().anyMatch(activity -> activity.getLong("activityId") == activityID);
    }

    /**
     * Function responsible for returning all registered users from potential users list.
     * @param userIds List of potential users.
     * @return List of registered users.
     */
    public List<UserModel> getAllUsersFromIdsList(List<Long> userIds) {
        var users = new ArrayList<UserModel>();
        for (var userId : userIds) {
            var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
            if(user != null)
                users.add(UserModel.fromDocument(user));
        }
        return users;
    }

    /**
     * Function responsible for returning current server (guild) settings.
     * @param guildID ID of the guild.
     * @return Server model of the server (guild).
     */
    public ServerModel getGuildSettings(long guildID) {
        var server = new BasicDBObject("guildId", guildID);
        var serverExists = serversCollection.find(server).first();
        if(serverExists != null)
            return ServerModel.fromDocument(serverExists);
        else
            return null;
    }

    /**
     * Function responsible for returning current server (guild) bot prefix for non-slash commands.
     * @param guildID ID of the guild.
     * @return Bot prefix on the server (guild) for non-slash commands.
     */
    public String getGuildPrefix(long guildID) {
        var collection = database.getCollection("Server");
        var server = new BasicDBObject("guildId", guildID);
        var serverExists = collection.find(server).first();
        if(serverExists != null)
        {
            return serverExists.getString("guildBotPrefix");
        }
        else {
            return Main.defaultBotPrefix;
        }
    }

    /**
     * Function responsible for returning all registered lobbies.
     * @return List of all registered lobbies.
     */
    public MongoCollection<Document> getLobbies() {
        return lobbiesCollection;
    }

    /**
     * Function responsible for unregistration of user from the database.
     * @param userID ID of the user.
     */
    public void unregisterUser(long userID) {
        var user = new BasicDBObject("userId", userID);
        usersCollection.deleteOne(user);
    }

    /**
     * Function responsible for unregistration of server (guild) from the database.
     * @param idLong ID of the server (guild).
     */
    public void unregisterGuild(long idLong) {
        var server = new BasicDBObject("guildId", idLong);
        serversCollection.deleteOne(server);
    }

    /**
     * Function responsible for modification of current server (guild) settings on the database.
     * @param serverModel Server model of the server (guild).
     */
    public void modifyGuildSettings(ServerModel serverModel) {
        var server = new BasicDBObject("guildId", serverModel.guildId);
        var serverExists = serversCollection.find(server).first();
        if(serverExists != null)
        {
            serversCollection.replaceOne(serverExists, ServerModel.toDocument(serverModel));
        }
    }

    public List<Activity> getAllUserRegisteredActivities(long userId) {
        var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
        assert user != null;
        var userActivities = user.getList("userRegisteredActivities", Document.class);
        return userActivities.stream().map(Activity::fromDocument).collect(Collectors.toList());
    }

    public boolean isUserOwnerOfLobby(long userId, long lobbyId) {
        var lobby = lobbiesCollection.find(new BasicDBObject("lobbyChannelId", lobbyId)).first();
        if(lobby == null)
            return false;

        return lobby.getLong("lobbyUserOwnerId") == userId;
    }
}
