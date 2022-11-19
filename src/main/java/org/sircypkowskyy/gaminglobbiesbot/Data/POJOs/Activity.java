package org.sircypkowskyy.gaminglobbiesbot.Data.POJOs;

import org.bson.Document;

public class Activity {
    public long activityId;
    public String activityName;
    public String activityDescription;
    public String activityIconURL;

    /**
     * Activity POJO.
     * @param activityId Activity ID.
     * @param activityName Activity name.
     * @param activityDescription Activity description.
     * @param activityIconURL Activity icon URL.
     */
    public Activity(long activityId, String activityName, String activityDescription, String activityIconURL) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.activityDescription = activityDescription;
        this.activityIconURL = activityIconURL;
    }

    public static Activity fromDocument(Document document) {
        return new Activity(
                document.getLong("activityId"),
                document.getString("activityName"),
                document.getString("activityDescription"),
                document.getString("activityIconURL")
        );
    }

    public static Document toDocument(Activity activity)
    {
        return new Document("activityId", activity.activityId)
                .append("activityName", activity.activityName)
                .append("activityDescription", activity.activityDescription)
                .append("activityIconURL", activity.activityIconURL);

    }
}
