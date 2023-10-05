package com.hawolt.client.resources.ledge.personalizedoffers.objects;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PersonalOfferCatalog {

    public List<PersonalizedOfferItem> getPersonalOfferCatalog(LeagueClient client) {
        List<PersonalizedOfferItem> list = new ArrayList<>();
        JSONObject object = client.getCachedValue(CacheElement.PERSONALIZED_OFFERS);
        JSONArray offers = object.getJSONArray("offers");
        List<PersonalizedStoreItem> items = new ArrayList<>();
        for (int i = 0; i < offers.length(); i++) {
            JSONObject offer = offers.getJSONObject(i);
            items.add(new PersonalizedStoreItem(offer));
        }
        int champ = 0;
        int skin = 1;
        for (int i = 0; i < (items.size() / 2); i++) {
            list.add(new PersonalizedOfferItem(items.get(champ), items.get(skin)));
            champ = champ + 2;
            skin = skin + 2;
        }
        return list;
    }

}
