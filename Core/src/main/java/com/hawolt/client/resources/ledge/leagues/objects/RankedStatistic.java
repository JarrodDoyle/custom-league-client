package com.hawolt.client.resources.ledge.leagues.objects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 24/09/2023 03:11
 * Author: Twitter @hawolt
 **/

public class RankedStatistic {
    private final Map<QueueType, RankedRating> map = new HashMap<>();

    public RankedStatistic(JSONObject object) {
        JSONArray queues = object.getJSONArray("queues");
        for (int i = 0; i < queues.length(); i++) {
            JSONObject reference = queues.getJSONObject(i);
            QueueType type = QueueType.valueOf(reference.getString("queueType"));
            map.put(type, new RankedRating(reference));
        }
    }

    public Map<QueueType, RankedRating> getMapping() {
        return map;
    }

    public RankedRating getRankedRating(QueueType queueType) {
        return map.get(queueType);
    }

    @Override
    public String toString() {
        return "RankedStatistic{" +
                "map=" + map +
                '}';
    }
}
