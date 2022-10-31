package org.sircypkowskyy.gaminglobbiesbot.Data.Models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

public class ServerModel {
    public long guildId;
    public String guildBotPrefix;
    public long guildLobbiesCategoryId;
    public long guildLobbyInfoChannelId;

    public ServerModel(long guildId, String guildBotPrefix, long guildLobbiesCategoryId, long guildLobbyInfoChannelId) {
        this.guildId = guildId;
        this.guildBotPrefix = guildBotPrefix;
        this.guildLobbiesCategoryId = guildLobbiesCategoryId;
        this.guildLobbyInfoChannelId = guildLobbyInfoChannelId;
    }

    public static DBObject toDBObject(ServerModel serverModel) {
        return new BasicDBObject("guildId", serverModel.guildId)
                .append("guildBotPrefix", serverModel.guildBotPrefix)
                .append("guildLobbiesCategoryId", serverModel.guildLobbiesCategoryId)
                .append("guildLobbyInfoChannelId", serverModel.guildLobbyInfoChannelId);
    }

    public static Document toDocument(ServerModel serverModel) {
        return new Document("guildId", serverModel.guildId)
                .append("guildBotPrefix", serverModel.guildBotPrefix)
                .append("guildLobbiesCategoryId", serverModel.guildLobbiesCategoryId)
                .append("guildLobbyInfoChannelId", serverModel.guildLobbyInfoChannelId);
    }

    public static ServerModel fromDocument(Document document) {
        return new ServerModel(
                document.getLong("guildId"),
                document.getString("guildBotPrefix"),
                document.getLong("guildLobbiesCategoryId"),
                document.getLong("guildLobbyInfoChannelId")
        );
    }
}
