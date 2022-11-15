package org.sircypkowskyy.gaminglobbiesbot.Data.Models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class LobbyModel {
    public long lobbyChannelId;
    public long lobbyGuildId;
    public long lobbyUserOwnerId;
    public long lobbyActivityId;
    public int lobbyMaxPlayers;
    public Date lobbyCreated;
    public long lobbyInfoMessageId;
    public long lobbyInfoMessageChannelId;

    public LobbyModel(long lobbyChannelId, long lobbyGuildId, long lobbyUserOwnerId, long lobbyActivityId, int lobbyMaxPlayers, Date lobbyCreated, long lobbyInfoMessageId, long lobbyInfoMessageChannelId) {
        this.lobbyChannelId = lobbyChannelId;
        this.lobbyGuildId = lobbyGuildId;
        this.lobbyUserOwnerId = lobbyUserOwnerId;
        this.lobbyActivityId = lobbyActivityId;
        this.lobbyMaxPlayers = lobbyMaxPlayers;
        this.lobbyCreated = lobbyCreated;
        this.lobbyInfoMessageId = lobbyInfoMessageId;
        this.lobbyInfoMessageChannelId = lobbyInfoMessageChannelId;
    }

    public static DBObject toDBObject(LobbyModel lobbyModel) {
        return new BasicDBObject("lobbyChannelId", lobbyModel.lobbyChannelId)
                .append("lobbyGuildId", lobbyModel.lobbyGuildId)
                .append("lobbyUserOwnerId", lobbyModel.lobbyUserOwnerId)
                .append("lobbyActivityId", lobbyModel.lobbyActivityId)
                .append("lobbyMaxPlayers", lobbyModel.lobbyMaxPlayers)
                .append("lobbyCreated", lobbyModel.lobbyCreated)
                .append("lobbyInfoMessageId", lobbyModel.lobbyInfoMessageId)
                .append("lobbyInfoMessageChannelId", lobbyModel.lobbyInfoMessageChannelId);
    }

    public static Document toDocument(LobbyModel lobbyModel) {
        return new Document("lobbyChannelId", lobbyModel.lobbyChannelId)
                .append("lobbyGuildId", lobbyModel.lobbyGuildId)
                .append("lobbyUserOwnerId", lobbyModel.lobbyUserOwnerId)
                .append("lobbyActivityId", lobbyModel.lobbyActivityId)
                .append("lobbyMaxPlayers", lobbyModel.lobbyMaxPlayers)
                .append("lobbyCreated", lobbyModel.lobbyCreated)
                .append("lobbyInfoMessageId", lobbyModel.lobbyInfoMessageId)
                .append("lobbyInfoMessageChannelId", lobbyModel.lobbyInfoMessageChannelId);
    }

    public static LobbyModel fromDocument(Document document) {
        return new LobbyModel(
                document.getLong("lobbyChannelId"),
                document.getLong("lobbyGuildId"),
                document.getLong("lobbyUserOwnerId"),
                document.getLong("lobbyActivityId"),
                document.getInteger("lobbyMaxPlayers"),
                document.getDate("lobbyCreated"),
                document.getLong("lobbyInfoMessageId"),
                document.getLong("lobbyInfoMessageChannelId")
        );
    }
}
