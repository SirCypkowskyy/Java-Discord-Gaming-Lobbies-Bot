package org.sircypkowskyy.gaminglobbiesbot.Data.POJOs;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class Activity {
    private ObjectId id;
    @BsonProperty(value = "activityId")
    private long activityId;
    @BsonProperty(value = "activityName")
    private String activityName;

}
