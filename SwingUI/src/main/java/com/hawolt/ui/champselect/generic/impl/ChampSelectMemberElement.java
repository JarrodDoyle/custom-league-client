package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.Swiftrift;
import com.hawolt.async.Debouncer;
import com.hawolt.async.ExecutorManager;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.async.loader.ResourceManager;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.communitydragon.DataTypeConverter;
import com.hawolt.client.resources.communitydragon.champion.ChampionIndex;
import com.hawolt.client.resources.communitydragon.champion.ChampionSource;
import com.hawolt.client.resources.communitydragon.skin.SkinIndex;
import com.hawolt.client.resources.communitydragon.skin.SkinSource;
import com.hawolt.client.resources.communitydragon.spell.SpellIndex;
import com.hawolt.client.resources.communitydragon.spell.SpellSource;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.teambuilder.TeamBuilderLedge;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.service.impl.TeamBuilderService;
import com.hawolt.ui.champselect.context.*;
import com.hawolt.ui.champselect.data.*;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.custom.GraphicalDrawableManager;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created: 30/08/2023 17:03
 * Author: Twitter @hawolt
 **/

public class ChampSelectMemberElement extends ChildUIComponent implements DataTypeConverter<byte[], BufferedImage> {
    private static final String SPRITE_PATH = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg";
    private final ScheduledExecutorService service = ExecutorManager.getScheduledService("trade-blocker");
    private static final Color HIGHLIGHT_NOT_LOCKED = new Color(0xFF, 0xFF, 0xFF, 0x4F);
    private static final Dimension SUMMONER_SPELL_DIMENSION = new Dimension(28, 28);
    private final ResourceManager<BufferedImage> manager = new ResourceManager<>(this);
    private static final Color HIGHLIGHT_PICKING = new Color(155, 224, 155, 179);
    private final ChampSelectTeamType teamType;
    private final Swiftrift swiftrift;
    private final ChampSelectTeam team;

    private int championId, indicatorId, skinId, spell1Id, spell2Id, currentActionSetIndex;
    private BufferedImage sprite, spellOne, spellTwo, clearChampion;
    private boolean picking, locked, self, teamMember;
    private Optional<PickOrderStatus> pickOrderStatus;
    private GraphicalDrawableManager drawables;
    private Optional<TradeStatus> tradeStatus;
    private ScheduledFuture<?> future;
    private String hero, puuid, name;
    private ChampSelectMember member;
    private DraftMode draftMode;


    public ChampSelectMemberElement(Swiftrift swiftrift, ChampSelectTeamType teamType, ChampSelectTeam team, ChampSelectMember member) {
        ColorPalette.addThemeListener(this);
        this.addComponentListener(new ChampSelectSelectMemberResizeAdapter());
        this.setBackground(ColorPalette.backgroundColor);
        this.swiftrift = swiftrift;
        this.teamType = teamType;
        this.member = member;
        this.team = team;
        this.drawables();
    }

    private void configure(ChampSelectContext context, ChampSelectTeamMember teamMember) {
        ChampSelectDataContext dataContext = context.getChampSelectDataContext();
        LeagueClient client = dataContext.getLeagueClient();
        if (client == null) {
            Logger.warn("unable to fetch name for {}, client is null", puuid == null ? "N/A" : puuid);
            return;
        }
        if (puuid != null && puuid.equals(teamMember.getPUUID())) return;
        this.puuid = teamMember.getPUUID();
        Map<String, String> resolver = dataContext.getPUUIDResolver();
        if (!resolver.containsKey(puuid)) {
            SummonerLedge summonerLedge = dataContext.getLeagueClient().getLedge().getSummoner();
            try {
                this.name = summonerLedge.resolveSummonerByPUUD(teamMember.getPUUID()).getName();
                switch (teamMember.getNameVisibilityType()) {
                    case "UNHIDDEN" ->
                            dataContext.cache(teamMember.getPUUID(), String.format("%s (%s)", name, getHiddenName()));
                    case "HIDDEN" -> dataContext.cache(teamMember.getPUUID(), getHiddenName());
                    case "VISIBLE" -> dataContext.cache(teamMember.getPUUID(), name);
                }
            } catch (IOException e) {
                Logger.error("Failed to retrieve name for {}", teamMember.getPUUID());
            }
        } else {
            this.name = resolver.get(puuid);
        }
        this.repaint();
    }

    private void adjust() {
        Dimension dimension = getSize();
        clearChampion = Scalr.resize(sprite, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, dimension.width);
        repaint();
    }

    private void drawables() {
        this.drawables = new GraphicalDrawableManager(this);
        GraphicalIndicatorButton swapOrder = new GraphicalIndicatorButton();
        ResourceLoader.loadLocalResource("assets/cs_swap_order.png", swapOrder);
        swapOrder.addExecutionListener(event -> Swiftrift.service.execute(() -> {
            if (event.getInitiator() instanceof MouseEvent mouseEvent) {
                TeamBuilderLedge ledge = swiftrift.getLeagueClient().getLedge().getTeamBuilder();
                pickOrderStatus.ifPresent(trade -> {
                    try {
                        switch (mouseEvent.getButton()) {
                            case 1 -> ledge.acceptPickOrderSwap(trade.getId());
                            case 3 -> ledge.declinePickOrderSwap(trade.getId());
                        }
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                });
            }
        }));
        GraphicalIndicatorButton swapChampion = new GraphicalIndicatorButton();
        ResourceLoader.loadLocalResource("assets/cs_swap_champion.png", swapChampion);
        swapChampion.addExecutionListener(event -> Swiftrift.service.execute(() -> {
            if (event.getInitiator() instanceof MouseEvent mouseEvent) {
                TeamBuilderService service = swiftrift.getLeagueClient().getRTMPClient().getTeamBuilderService();
                tradeStatus.ifPresent(trade -> {
                    try {
                        switch (mouseEvent.getButton()) {
                            case 1 -> service.acceptTradeV1Blocking(trade.getId());
                            case 3 -> service.declineTradeV1Blocking(trade.getId());
                        }
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                });
            }
        }));
        swapChampion.setVisible(false);
        addMouseListener(drawables);
        addMouseMotionListener(drawables);
        drawables.register("order", swapOrder);
        drawables.register("champion", swapChampion);
    }

    private void setClearChampion(int championId) {
        ChampionIndex championIndex = ChampionSource.CHAMPION_SOURCE_INSTANCE.get();
        this.hero = championIndex.getChampion(championId).getName();
    }

    private void setChampionId(int championId) {
        this.championId = championId;
        this.setClearChampion(championId);
        this.updateSprite(championId, championId * 1000);
    }

    private void updateChamp(int championId) {
        if (championId == 0) return;
        setChampionId(championId);
    }

    private void updateChamp(ChampSelectMember member) {
        if (this.championId == member.getChampionId()) return;
        if (member.getChampionId() == 0) return;
        setChampionId(member.getChampionId());
    }

    private void updateChampIndicator(ChampSelectTeamMember member) {
        if (this.indicatorId == member.getChampionPickIntent()) return;
        this.indicatorId = member.getChampionPickIntent();
        if (indicatorId != 0) {
            this.setClearChampion(indicatorId);
            this.updateSprite(indicatorId, indicatorId * 1000);
        } else if (member.getChampionId() == 0 && !picking) {
            this.clearChampion = null;
            this.sprite = null;
        }
    }

    private void updateSkin(ChampSelectTeamMember member) {
        if (this.skinId == member.getSkinId()) return;
        if (member.getSkinId() != 0) {
            this.skinId = member.getSkinId();
            SkinIndex skinIndex = SkinSource.SKIN_SOURCE_INSTANCE.get();
            this.updateSprite(member.getChampionId(), skinIndex.getSkin(member.getSkinId()).getId());
        }
    }

    private void updateSprite(int championId, int skinId) {
        manager.load(
                String.format(
                        SPRITE_PATH,
                        championId,
                        skinId
                ),
                bufferedImage -> {
                    this.sprite = Scalr.crop(bufferedImage, 300, 50, 640, 400);
                    this.adjust();
                }
        );
    }

    private void updateSpellOne(ChampSelectTeamMember member) {
        if (this.spell1Id == member.getSpell1Id()) return;
        if (member.getSpell1Id() != 0) {
            this.spell1Id = member.getSpell1Id();
            SpellIndex spellIndex = SpellSource.SPELL_SOURCE_INSTANCE.get();
            manager.load(
                    spellIndex.getSpell(spell1Id).getIconPath(),
                    bufferedImage -> {
                        ChampSelectMemberElement.this.spellOne = Scalr.resize(
                                bufferedImage,
                                Scalr.Method.ULTRA_QUALITY,
                                Scalr.Mode.FIT_TO_HEIGHT,
                                SUMMONER_SPELL_DIMENSION.width,
                                SUMMONER_SPELL_DIMENSION.height
                        );
                    }
            );
        }
    }

    private void updateSpellTwo(ChampSelectTeamMember member) {
        if (this.spell2Id == member.getSpell2Id()) return;
        if (member.getSpell2Id() != 0) {
            this.spell2Id = member.getSpell2Id();
            SpellIndex spellIndex = SpellSource.SPELL_SOURCE_INSTANCE.get();
            manager.load(
                    spellIndex.getSpell(spell2Id).getIconPath(),
                    bufferedImage -> {
                        ChampSelectMemberElement.this.spellTwo = Scalr.resize(
                                bufferedImage,
                                Scalr.Method.ULTRA_QUALITY,
                                Scalr.Mode.FIT_TO_HEIGHT,
                                SUMMONER_SPELL_DIMENSION.width,
                                SUMMONER_SPELL_DIMENSION.height
                        );
                    }
            );
        }
    }


    public void init(ChampSelectContext context) {
        ExecutorService loader = ExecutorManager.getService("name-loader");
        loader.execute(() -> {
            if (!(member instanceof ChampSelectTeamMember teamMember)) return;
            GraphicalIndicatorButton swapOrder = drawables.getGraphicalComponent("order");
            swapOrder.setVisible(true);
            configure(context, teamMember);
        });
    }

    private void update(ChampSelectContext context, ChampSelectMember member) {
        ChampSelectInteractionContext interactionContext = context.getChampSelectInteractionContext();
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        ChampSelectUtilityContext utilityContext = context.getChampSelectUtilityContext();
        this.updateChamp(this.member = member);
        GraphicalIndicatorButton swapOrder = drawables.getGraphicalComponent("order");
        long remaining = settingsContext.getCurrentTimeRemainingMillis() - (System.currentTimeMillis() - settingsContext.getLastUpdate()) - 5000L;
        if (remaining > 0) {
            if (future != null) future.cancel(true);
            future = service.schedule(() -> {
                swapOrder.setVisible(false);
                ChampSelectMemberElement.this.repaint();
            }, remaining, TimeUnit.MILLISECONDS);
        }

        this.pickOrderStatus = interactionContext.getPickSwap(member.getCellId());
        this.currentActionSetIndex = settingsContext.getCurrentActionSetIndex();
        this.tradeStatus = interactionContext.getTrade(member.getCellId());
        this.teamMember = utilityContext.isTeamMember(member);
        this.picking = utilityContext.isPicking(member);
        this.locked = utilityContext.isLockedIn(member);
        this.draftMode = settingsContext.getDraftMode();
        this.self = utilityContext.isSelf(member);

        if (teamType == ChampSelectTeamType.ALLIED) {
            ChampSelectTeamMember teamMember = (ChampSelectTeamMember) this.member;
            this.updateChampIndicator(teamMember);
            this.configure(context, teamMember);
            this.updateSpellOne(teamMember);
            this.updateSpellTwo(teamMember);
            this.updateSkin(teamMember);

            interactionContext.getPickSwap().ifPresentOrElse(trade -> {
                if (trade.getCellId() != this.member.getCellId()) return;
                swapOrder.setHighlight(true);
            }, () -> swapOrder.setHighlight(false));

            GraphicalIndicatorButton swapChampion = drawables.getGraphicalComponent("champion");
            interactionContext.getActiveTrade().ifPresentOrElse(trade -> {
                if (trade.getCellId() != this.member.getCellId()) return;
                swapChampion.setHighlight(true);
            }, () -> swapChampion.setHighlight(false));

            int baseX = team == ChampSelectTeam.PURPLE ? 5 : getSize().width - 5 - 28;
            swapOrder.update(new Rectangle(baseX, 38, 28, 28));
            if (remaining > 5000L) {
                utilityContext.getOwnPickPhase().ifPresent(phase -> {
                    swapOrder.setVisible(!phase.isCompleted() && !utilityContext.isLockedIn(this.member));
                    repaint();
                });
            }
            TradeStatus[] tradeStatuses = interactionContext.getTrades();
            swapChampion.update(new Rectangle(baseX, 71, 28, 28));
            for (TradeStatus status : tradeStatuses) {
                if (status.getCellId() != this.member.getCellId()) continue;
                swapChampion.setVisible(!"INVALID".equals(status.getState()));
            }

            if (utilityContext.isPicking(this.member)) {
                utilityContext.getCurrent()
                        .stream()
                        .filter(actionObject -> actionObject.getActorCellId() == this.member.getCellId())
                        .findAny()
                        .ifPresent(actionObject -> {
                            if (settingsContext.getCurrentActionSetIndex() > 0 || settingsContext.getActionSetMapping().size() == 1) {
                                updateChamp(actionObject.getChampionId());
                            }
                        });
            }
        }
        this.repaint();
    }

    public void handle(ChampSelectContext context, ChampSelectMember member) {
        int initialCounter;
        LeagueClient leagueClient = context.getChampSelectDataContext().getLeagueClient();
        if (leagueClient != null) {
            int counter = leagueClient.getCachedValue(CacheElement.CHAMP_SELECT_COUNTER);
            initialCounter = counter + 1;
        } else {
            // LOCAL: adjust this depending on local test data
            initialCounter = 5;
        }
        if (context.getChampSelectSettingsContext().getCounter() == initialCounter) {
            init(context);
        }
        update(context, member);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimension = getSize();

        //DRAW CHAMPION/SKIN IMAGE
        if (clearChampion != null) {
            int imageX = (dimension.width >> 1) - (clearChampion.getWidth() >> 1);
            int imageY = (dimension.height >> 1) - (clearChampion.getHeight() >> 1);
            g.drawImage(clearChampion, imageX, imageY, null);
        }
        //INDICATE PICKING OR STATUS NOT LOCKED IN

        if (member != null && draftMode != DraftMode.ARAM) {
            if (!locked || (draftMode == DraftMode.DRAFT && currentActionSetIndex <= 0)) {
                g.setColor(HIGHLIGHT_NOT_LOCKED);
                g.fillRect(0, 0, dimension.width, dimension.height);
            }
        }
        if (member == null) {
            g.setColor(HIGHLIGHT_NOT_LOCKED);
            g.fillRect(0, 0, dimension.width, dimension.height);
        }

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(new Font("Dialog", Font.BOLD, 20));
        graphics2D.setColor(Color.BLACK);
        FontMetrics metrics = graphics2D.getFontMetrics();

        if (member != null) {
            //DRAW SUMMONER SPELLS
            if (spellOne != null && spellTwo != null) paintSummonerSpells(g);

            //DRAW ROLES
            if (teamType == ChampSelectTeamType.ALLIED) {
                ChampSelectTeamMember teamMember = (ChampSelectTeamMember) member;

                String position = !"UTILITY".equals(teamMember.getAssignedPosition()) ? teamMember.getAssignedPosition() : "SUPPORT";
                if (!"NONE".equals(position)) {
                    int positionWidth = metrics.stringWidth(position);
                    int positionX = switch (team) {
                        case BLUE -> 5;
                        case PURPLE -> dimension.width - 5 - positionWidth;
                    };
                    drawTextWithShadow(graphics2D, position, positionX, 5 + metrics.getAscent());
                }

                int guidelineY = dimension.height - 7;
                int baseY = switch (member.getNameVisibilityType()) {
                    case "UNHIDDEN" -> guidelineY - metrics.getAscent() - 7;
                    default -> guidelineY;
                };
                String resolved = name == null ? getHiddenName() : name;
                String name = switch (member.getNameVisibilityType()) {
                    case "HIDDEN", "UNHIDDEN" -> getHiddenName();
                    default -> String.valueOf(resolved);
                };
                int hiddenNameX = switch (team) {
                    case BLUE -> dimension.width - 5 - metrics.stringWidth(name);
                    case PURPLE -> 5;
                };
                drawTextWithShadow(graphics2D, name, hiddenNameX, baseY);
                if (baseY != guidelineY) {
                    int visibleNameX = switch (team) {
                        case BLUE -> dimension.width - 5 - metrics.stringWidth(resolved);
                        case PURPLE -> 5;
                    };
                    drawTextWithShadow(graphics2D, resolved, visibleNameX, guidelineY);
                }
            }

            //DRAW CHAMPION NAME
            if (hero != null) {
                int heroX = switch (team) {
                    case BLUE -> 5;
                    case PURPLE -> dimension.width - 5 - metrics.stringWidth(hero);
                };
                drawTextWithShadow(graphics2D, hero, heroX, dimension.height - 7);
            }
        }

        //BORDER
        if (member != null && (!locked && picking)) {
            g.setColor(HIGHLIGHT_PICKING);
            g.drawRect(0, 0, dimension.width - 1, dimension.height - 1);
        } else {
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, dimension.width - 1, dimension.height - 1);
        }

        if (member == null) return;

        if (self) {
            g.setColor(new Color(217, 160, 74, 255));
            g.drawRect(1, 1, dimension.width - 3, dimension.height - 3);
        } else if (teamMember) {
            drawables.draw(graphics2D);
        }
    }

    public String getHiddenName() {
        return switch (member.getCellId() % 5) {
            case 0 -> "Raptor";
            case 1 -> "Krug";
            case 2 -> "Murk Wolf";
            case 3 -> "Gromp";
            default -> "Scuttle Crab";
        };
    }

    private void drawTextWithShadow(Graphics2D graphics2D, String text, int x, int y) {
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawString(text, x + 1, y + 1);
        graphics2D.setColor(ColorPalette.textColor);
        graphics2D.drawString(text, x, y);
    }

    private void paintSummonerSpells(Graphics g) {
        Dimension dimension = getSize();
        //g.setColor(Color.BLACK);
        switch (team) {
            case BLUE -> {
                int baseX = dimension.width - 5 - SUMMONER_SPELL_DIMENSION.width;
                g.drawImage(spellTwo, baseX, 5, null);
                //g.drawImage(PaintHelper.circleize(spellTwo, ColorPalette.CARD_ROUNDING), baseX, 5, null);
                g.drawRect(baseX, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
                g.drawImage(spellOne, baseX - 5 - SUMMONER_SPELL_DIMENSION.width, 5, null);
                //g.drawImage(PaintHelper.circleize(spellOne, ColorPalette.CARD_ROUNDING), baseX - 5 - SUMMONER_SPELL_DIMENSION.width, 5, null);
                g.drawRect(baseX - 5 - SUMMONER_SPELL_DIMENSION.width, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
            }
            case PURPLE -> {
                int baseX = 5;
                g.drawImage(spellOne, baseX, 5, null);
                //g.drawImage(PaintHelper.circleize(spellOne, ColorPalette.CARD_ROUNDING), baseX, 5, null);
                g.drawRect(baseX, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
                g.drawImage(spellTwo, baseX + SUMMONER_SPELL_DIMENSION.width + 5, 5, null);
                //g.drawImage(PaintHelper.circleize(spellTwo, ColorPalette.CARD_ROUNDING), baseX + SUMMONER_SPELL_DIMENSION.width + 5, 5, null);
                g.drawRect(baseX + SUMMONER_SPELL_DIMENSION.width + 5, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
            }
        }
    }

    @Override
    public BufferedImage apply(byte[] b) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(b));
    }

    private class ChampSelectSelectMemberResizeAdapter extends ComponentAdapter {
        private static final Debouncer debouncer = new Debouncer();

        @Override
        public void componentResized(ComponentEvent e) {
            if (sprite == null) return;
            debouncer.debounce(
                    String.valueOf(member.getCellId()),
                    ChampSelectMemberElement.this::adjust,
                    200, TimeUnit.MILLISECONDS
            );
        }
    }
}
