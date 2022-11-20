package org.sircypkowskyy.gaminglobbiesbot.Data.Models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

import java.util.Date;

/**
 * Model for a lobby.
 */
public class LobbyModel {
    public long lobbyChannelId;
    public long lobbyGuildId;
    public long lobbyUserOwnerId;
    public long lobbyActivityId;
    public int lobbyMaxPlayers;
    public Date lobbyCreated;
    public long lobbyInfoMessageId;
    public long lobbyInfoMessageChannelId;

    public boolean lobbyIsPrivate;

    /**
     * Model for a lobby.
     * @param lobbyChannelId The voice channel ID of the lobby.
     * @param lobbyGuildId The guild ID of the lobby.
     * @param lobbyUserOwnerId The user ID of the lobby owner.
     * @param lobbyActivityId The activity ID of the lobby.
     * @param lobbyMaxPlayers The maximum number of players in the lobby.
     * @param lobbyCreated The date the lobby was created.
     * @param lobbyInfoMessageId The message ID of the lobby info message.
     * @param lobbyInfoMessageChannelId The channel ID of the lobby info message.
     * @param lobbyIsPrivate Whether the lobby is private.
     */
    public LobbyModel(long lobbyChannelId, long lobbyGuildId, long lobbyUserOwnerId, long lobbyActivityId, int lobbyMaxPlayers, Date lobbyCreated, long lobbyInfoMessageId, long lobbyInfoMessageChannelId, boolean lobbyIsPrivate) {
        this.lobbyChannelId = lobbyChannelId;
        this.lobbyGuildId = lobbyGuildId;
        this.lobbyUserOwnerId = lobbyUserOwnerId;
        this.lobbyActivityId = lobbyActivityId;
        this.lobbyMaxPlayers = lobbyMaxPlayers;
        this.lobbyCreated = lobbyCreated;
        this.lobbyInfoMessageId = lobbyInfoMessageId;
        this.lobbyInfoMessageChannelId = lobbyInfoMessageChannelId;
        this.lobbyIsPrivate = lobbyIsPrivate;
    }

    /**
     * Converts a LobbyModel to a DBObject.
     * @param lobbyModel The LobbyModel to convert.
     * @return The DBObject.
     */
    public static DBObject toDBObject(LobbyModel lobbyModel) {
        return new BasicDBObject("lobbyChannelId", lobbyModel.lobbyChannelId)
                .append("lobbyGuildId", lobbyModel.lobbyGuildId)
                .append("lobbyUserOwnerId", lobbyModel.lobbyUserOwnerId)
                .append("lobbyActivityId", lobbyModel.lobbyActivityId)
                .append("lobbyMaxPlayers", lobbyModel.lobbyMaxPlayers)
                .append("lobbyCreated", lobbyModel.lobbyCreated)
                .append("lobbyInfoMessageId", lobbyModel.lobbyInfoMessageId)
                .append("lobbyInfoMessageChannelId", lobbyModel.lobbyInfoMessageChannelId)
                .append("lobbyIsPrivate", lobbyModel.lobbyIsPrivate);
    }

    /**
     * Converts a lobby model to a document.
     * @param lobbyModel The lobby model to convert.
     * @return The document.
     */
    public static Document toDocument(LobbyModel lobbyModel) {
        return new Document("lobbyChannelId", lobbyModel.lobbyChannelId)
                .append("lobbyGuildId", lobbyModel.lobbyGuildId)
                .append("lobbyUserOwnerId", lobbyModel.lobbyUserOwnerId)
                .append("lobbyActivityId", lobbyModel.lobbyActivityId)
                .append("lobbyMaxPlayers", lobbyModel.lobbyMaxPlayers)
                .append("lobbyCreated", lobbyModel.lobbyCreated)
                .append("lobbyInfoMessageId", lobbyModel.lobbyInfoMessageId)
                .append("lobbyInfoMessageChannelId", lobbyModel.lobbyInfoMessageChannelId)
                .append("lobbyIsPrivate", lobbyModel.lobbyIsPrivate);
    }

    /**
     * Converts a document to a lobby model.
     * @param document The document to convert.
     * @return The lobby model.
     */
    public static LobbyModel fromDocument(Document document) {
        return new LobbyModel(
                document.getLong("lobbyChannelId"),
                document.getLong("lobbyGuildId"),
                document.getLong("lobbyUserOwnerId"),
                document.getLong("lobbyActivityId"),
                document.getInteger("lobbyMaxPlayers"),
                document.getDate("lobbyCreated"),
                document.getLong("lobbyInfoMessageId"),
                document.getLong("lobbyInfoMessageChannelId"),
                document.getBoolean("lobbyIsPrivate")
        );
    }
}
