package org.sircypkowskyy.gaminglobbiesbot.Data.Models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

public class LobbyModel {
    public long lobbyChannelId;
    public long lobbyUserOwnerId;
    public long lobbyActivityId;
    public int lobbyMaxPlayers;

    public LobbyModel(long lobbyChannelId, long lobbyUserOwnerId, long lobbyActivityId, int lobbyMaxPlayers) {
        this.lobbyChannelId = lobbyChannelId;
        this.lobbyUserOwnerId = lobbyUserOwnerId;
        this.lobbyActivityId = lobbyActivityId;
        this.lobbyMaxPlayers = lobbyMaxPlayers;
    }

    public static DBObject toDBObject(LobbyModel lobbyModel) {
        return new BasicDBObject("lobbyChannelId", lobbyModel.lobbyChannelId)
                .append("lobbyUserOwnerId", lobbyModel.lobbyUserOwnerId)
                .append("lobbyActivityId", lobbyModel.lobbyActivityId)
                .append("lobbyMaxPlayers", lobbyModel.lobbyMaxPlayers);
    }

    public static Document toDocument(LobbyModel lobbyModel) {
        return new Document("lobbyChannelId", lobbyModel.lobbyChannelId)
                .append("lobbyUserOwnerId", lobbyModel.lobbyUserOwnerId)
                .append("lobbyActivityId", lobbyModel.lobbyActivityId)
                .append("lobbyMaxPlayers", lobbyModel.lobbyMaxPlayers);
    }
}
