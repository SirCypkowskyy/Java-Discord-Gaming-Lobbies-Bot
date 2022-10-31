package org.sircypkowskyy.gaminglobbiesbot.Data.Models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

import java.util.List;

public class UserModel {
    public long userId;
    public List<Long> userRegisteredActivities;
    public boolean acceptDMs;

    public UserModel(long userId, List<Long> userRegisteredActivities, boolean acceptDMs) {
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
                document.getList("userRegisteredActivities", Long.class),
                document.getBoolean("acceptDMs")
        );
    }
}
