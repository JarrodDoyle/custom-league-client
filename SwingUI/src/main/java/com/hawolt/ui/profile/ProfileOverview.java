package com.hawolt.ui.profile;

import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.leagues.objects.QueueType;
import com.hawolt.client.resources.ledge.leagues.objects.RankedRating;
import com.hawolt.client.resources.ledge.leagues.objects.RankedStatistic;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.client.resources.ledge.summoner.objects.SummonerProfile;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.translucent.TranslucentLabel;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.LazyLoadedImageComponent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 24/09/2023 15:29
 * Author: Twitter @hawolt
 **/

public class ProfileOverview extends ChildUIComponent {
    private final static String UNRANKED_CREST = "https://raw.communitydragon.org/9.5/plugins/rcp-fe-lol-league-tier-names/global/default/assets/images/ranked-crests/unranked.png";
    private final static Map<String, String> RANK_MAPPING = new HashMap<>() {{
        put("GRANDMASTER", "MASTER");
        put("EMERALD", "PLATINUM");
        put("IRON", "BRONZE");
    }};

    private final SummonerProfile summonerProfile;
    private final RankedStatistic rankedStatistic;
    private final Summoner summoner;

    public ProfileOverview(Summoner summoner, SummonerProfile profile, RankedStatistic rankedStatistic) {
        super(new BorderLayout());
        this.setOpaque(false);
        this.summoner = summoner;
        this.summonerProfile = profile;
        this.rankedStatistic = rankedStatistic;

        ChildUIComponent west = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        west.setOpaque(false);
        add(west, BorderLayout.WEST);
        west.add(new TranslucentLabel(summoner.getName(), LTextAlign.CENTER));

        ChildUIComponent center = new ChildUIComponent(new BorderLayout());
        center.setOpaque(false);
        add(center, BorderLayout.CENTER);

        ChildUIComponent southernCenter = new ChildUIComponent(new BorderLayout());
        southernCenter.setOpaque(false);
        center.add(southernCenter, BorderLayout.SOUTH);

        LazyLoadedImageComponent lazyLoadedImageComponent = new LazyLoadedImageComponent(new Dimension(200, 200), 5);
        lazyLoadedImageComponent.setOpaque(false);
        String path = "https://raw.communitydragon.org/7.1/plugins/rcp-fe-lol-hover-card/global/default/%s.png";
        RankedRating rankedRating = rankedStatistic.getRankedRating(QueueType.RANKED_SOLO_5x5);
        rankedRating.getTier().ifPresentOrElse(tier -> {
            String translated = RANK_MAPPING.getOrDefault(tier, tier).toLowerCase();
            ResourceLoader.loadResource(String.format(path, translated), lazyLoadedImageComponent);
        }, () -> {
            ResourceLoader.loadResource(UNRANKED_CREST, lazyLoadedImageComponent);
        });
        southernCenter.add(lazyLoadedImageComponent, BorderLayout.WEST);
    }


    public SummonerProfile getSummonerProfile() {
        return summonerProfile;
    }

    public RankedStatistic getRankedStatistic() {
        return rankedStatistic;
    }

    public Summoner getSummoner() {
        return summoner;
    }
}
