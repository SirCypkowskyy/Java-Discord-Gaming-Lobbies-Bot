package org.sircypkowskyy.gaminglobbiesbot.Data.Models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.sircypkowskyy.gaminglobbiesbot.Data.POJOs.Activity;

import java.util.List;

public class UserModel {
    public long userId;
    public List<Document> userRegisteredActivities;
    public boolean acceptDMs;

    /**
     * UserModel object.
     * @param userId User ID.
     * @param userRegisteredActivities List of activities user is registered to.
     * @param acceptDMs Whether user accepts DMs from bot.
     */
    public UserModel(long userId, List<Document> userRegisteredActivities, boolean acceptDMs) {
        this.userId = userId;
        this.userRegisteredActivities = userRegisteredActivities;
        this.acceptDMs = acceptDMs;
    }

    public static DBObject toDBObject(UserModel userModel) {
        return new BasicDBObject("userId", userModel.userId)
                .append("userRegisteredActivities", userModel.userRegisteredActivities)
                .append("acceptDMs", userModel.acceptDMs);
    }

    public static Document toDocument(UserModel userModel) {
        return new Document("userId", userModel.userId)
                .append("userRegisteredActivities", userModel.userRegisteredActivities)
                .append("acceptDMs", userModel.acceptDMs);
    }

    public static UserModel fromDocument(Document document) {
        return new UserModel(
                document.getLong("userId"),
                document.getList("userRegisteredActivities", Document.class),
                document.getBoolean("acceptDMs")
        );
    }
}
