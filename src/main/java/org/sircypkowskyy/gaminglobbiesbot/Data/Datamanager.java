package org.sircypkowskyy.gaminglobbiesbot.Data;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.bson.Document;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.LobbyModel;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.ServerModel;
import org.sircypkowskyy.gaminglobbiesbot.Data.Models.UserModel;
import org.sircypkowskyy.gaminglobbiesbot.Main;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Datamanager extends Thread {

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
    private void connectClient() {
        var dotenv = Dotenv.configure().load();
        mongoClient = MongoClients.create(dotenv.get("MONGO_URL", "mongodb://localhost/app"));
        // Check if connection is successful
        System.out.println("Connected to MongoDB!\nGetting all databases...");
    }
    private void connectToDatabase() {
        database = mongoClient.getDatabase("GamingLobbiesBot");
        System.out.println("Connected to database: " + database.getName());
    }
    private void populateDatabase() {
        var collectionExists = database.listCollectionNames().into(new ArrayList());

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
    private void closeConnection() {
        mongoClient.close();
    }

    public void registerServer(long guildID) {
        var newServer = new ServerModel(guildID, Main.defaultBotPrefix, 0, 0);
        serversCollection.insertOne(ServerModel.toDocument(newServer));
        System.out.println("Created new server in database: " + guildID);
    }
    public void registerServer(long guildID, long lobbiesCategoryID) {
        var newServer = new ServerModel(guildID, Main.defaultBotPrefix, lobbiesCategoryID, 0);
        serversCollection.insertOne(ServerModel.toDocument(newServer));
        System.out.println("Created new server in database: " + guildID);
    }
    public void registerServer(long guildID, long lobbiesCategoryID, long lobbiesInfoTextChannel) {
        var newServer = new ServerModel(guildID, Main.defaultBotPrefix, lobbiesCategoryID, lobbiesInfoTextChannel);
        serversCollection.insertOne(ServerModel.toDocument(newServer));
        System.out.println("Created new server in database: " + guildID);
    }

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

    public void registerNewUserActivity(long userId, long activityId) {
        var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
        var userActivities = (List<Long>) user.get("userRegisteredActivities");
        userActivities.add(activityId);
        usersCollection.updateOne(new BasicDBObject("userId", userId), new BasicDBObject("$set", new BasicDBObject("userRegisteredActivities", userActivities)));
    }

    public void unregisterUserActivity(long userId, long activityId) {
        var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
        var userActivities = (List<Long>) user.get("userRegisteredActivities");
        userActivities.remove(activityId);
        usersCollection.updateOne(new BasicDBObject("userId", userId), new BasicDBObject("$set", new BasicDBObject("userRegisteredActivities", userActivities)));
    }

    public void registerNewLobby(LobbyModel newLobby) {
        lobbiesCollection.insertOne(LobbyModel.toDocument(newLobby));
    }

    public boolean doesGuildExist(long guildID) {
        var server = new BasicDBObject("guildId", guildID);
        var serverExists = serversCollection.find(server).first();
        return serverExists != null;
    }

    public boolean doesUserExist(long userID) {
        var user = new BasicDBObject("userId", userID);
        var userExists = usersCollection.find(user).first();
        return userExists != null;
    }

    public boolean doesUserHaveActivity(long userID, long activityID) {
        var user = new BasicDBObject("userId", userID);
        var userExists = usersCollection.find(user).first();
        var userActivities = (List<Long>) userExists.get("userRegisteredActivities");
        return userActivities.contains(activityID);
    }

    public List<UserModel> getAllUsersFromIdsList(List<Long> userIds) {
        var users = new ArrayList<UserModel>();
        for (var userId : userIds) {
            var user = usersCollection.find(new BasicDBObject("userId", userId)).first();
            if(user != null)
                users.add(UserModel.fromDocument(user));
        }
        return users;
    }


    public ServerModel getGuildSettings(long guildID) {
        var server = new BasicDBObject("guildId", guildID);
        var serverExists = serversCollection.find(server).first();
        if(serverExists != null)
            return ServerModel.fromDocument(serverExists);
        else
            return null;
    }

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

    public MongoCollection<Document> getLobbies() {
        return lobbiesCollection;
    }

    public void unregisterUser(long userID) {
        var user = new BasicDBObject("userId", userID);
        usersCollection.deleteOne(user);
    }

    public void unregisterGuild(long idLong) {
        var server = new BasicDBObject("guildId", idLong);
        serversCollection.deleteOne(server);
    }

    public void modifyGuildSettings(ServerModel serverModel) {
        var server = new BasicDBObject("guildId", serverModel.guildId);
        var serverExists = serversCollection.find(server).first();
        if(serverExists != null)
        {
            serversCollection.replaceOne(serverExists, ServerModel.toDocument(serverModel));
        }
    }
}
