package com.hawolt.client.resources.ledge.preferences.objects.lcupreferences;

import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONObject;

import java.util.Optional;

public class LCUPreferences extends DynamicObject {

    private PartiesPositionPreference partiesPositionPreference;
    private ChampSelectPreference champSelectPreference;

    public LCUPreferences(JSONObject o) {
        super(o);
    }

    public Optional<PartiesPositionPreference> getPartiesPositionPreference() {
        if (partiesPositionPreference != null) return Optional.of(partiesPositionPreference);
        if (has("partiesPositionPreferences")) {
            this.partiesPositionPreference = new PartiesPositionPreference(getByKey("partiesPositionPreferences"));
        } else {
            JSONObject ppp = new JSONObject().put("data", new JSONObject().put("firstPreference", "UNSELECTED").put("secondPreference", "UNSELECTED")).put("schemaVersion", 0);
            this.partiesPositionPreference = new PartiesPositionPreference(ppp);
            this.put("partiesPositionPreference", ppp);
        }
        return Optional.ofNullable(partiesPositionPreference);
    }

    public Optional<ChampSelectPreference> getChampSelectPreference() {
        if (champSelectPreference != null) return Optional.of(champSelectPreference);
        if (has("champ-select")) {
            this.champSelectPreference = new ChampSelectPreference(getByKey("champ-select"));
        } else {
            JSONObject cs = new JSONObject().put("data", new JSONObject()).put("schemaVersion", 1);
            this.champSelectPreference = new ChampSelectPreference(cs);
            this.put("champ-select", cs);
        }
        return Optional.ofNullable(champSelectPreference);
    }

}
