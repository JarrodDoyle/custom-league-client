package com.hawolt.ui.champselect.generic;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.IncompleteRunePageException;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.settings.UserSettings;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created: 02/09/2023 19:19
 * Author: Twitter @hawolt
 **/

public class ChampSelectRuneComponent extends ChampSelectUIComponent {
    private final Map<String, ChampSelectRuneSelection> map = new HashMap<>();
    private final CardLayout layout = new CardLayout();
    private final ChildUIComponent main = new ChildUIComponent(layout);
    private final AbstractRenderInstance instance;
    private final LComboBox<String> selection;
    private final LFlatButton close, save;
    private final String patch;

    public ChampSelectRuneComponent(AbstractRenderInstance instance, String patch) {
        this.patch = patch;
        this.instance = instance;
        this.setLayout(new BorderLayout());
        this.instance.register(this);
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        ChildUIComponent header;
        add(header = new ChildUIComponent(new BorderLayout()), BorderLayout.NORTH);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        header.add(close = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT), BorderLayout.EAST);
        close.addActionListener(listener -> setRuneSelection());
        close.setRoundingCorners(true, false, true, false);
        close.setRounding(ColorPalette.CARD_ROUNDING);

        ChildUIComponent bar = new ChildUIComponent(null);
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        header.add(bar, BorderLayout.WEST);
        LFlatButton delete = new LFlatButton("❌", LTextAlign.CENTER, HighlightType.BOTTOM);
        delete.addActionListener(listener -> removeCurrentPage());
        bar.add(delete);
        LFlatButton create = new LFlatButton("➕", LTextAlign.CENTER, HighlightType.BOTTOM);
        create.addActionListener(listener -> addRunePage(UUID.randomUUID().toString()));
        bar.add(create);
        bar.add(save = new LFlatButton("✔", LTextAlign.CENTER, HighlightType.BOTTOM));
        save.addActionListener(listener -> setRuneSelection());
        LFlatButton rename = new LFlatButton("\uD83D\uDD01", LTextAlign.CENTER, HighlightType.BOTTOM);
        rename.addActionListener(listener -> {
            String name = Swiftrift.showInputDialog("New name");
            if (name == null) return;
            rename(name);
        });
        bar.add(rename);
        bar.add(selection = new LComboBox<>());
        selection.setBorder(null);
        add(main, BorderLayout.CENTER);

        addRunePage("DEFAULT");
        /* TODO load runes from settings
        ChampSelectContext context = instance.getContext();
        if (context == null) {
            addRunePage("DEFAULT");
        } else {
            Swiftrift swiftrift = context.getChampSelectInterfaceContext().getLeagueClientUI();
            UserSettings settings = swiftrift.getSettingService().getUserSettings();
        }*/

        selection.addActionListener(listener -> {
            showRunePageByName(selection.getItemAt(selection.getSelectedIndex()));
            setRuneSelection();
        });
    }

    private void remove(String current, ChampSelectRuneSelection page) {
        int currentIndex = selection.getSelectedIndex();
        main.remove(page);
        map.remove(current);
        selection.removeItemAt(currentIndex);
    }

    private void removeCurrentPage() {
        getCurrentRunePageName().ifPresent(current -> {
            getCurrentRunePage().ifPresent(page -> {
                remove(current, page);
            });
        });
    }

    private void rename(String name) {
        getCurrentRunePageName().ifPresent(current -> {
            getCurrentRunePage().ifPresent(page -> {
                int currentIndex = selection.getSelectedIndex();
                remove(current, page);
                selection.insertItemAt(name, currentIndex);
                selection.setSelectedIndex(currentIndex);
                main.add(name, page);
                map.put(name, page);
                showRunePageByName(name);
                revalidate();
            });
        });
    }

    private void addRunePage(String name) {
        if (map.containsKey(name)) {
            Swiftrift.showMessageDialog("You already have a page with this name");
        } else {
            ChampSelectRuneSelection page = new ChampSelectRuneSelection(instance, patch);
            selection.addItem(name);
            selection.setSelectedItem(name);
            map.put(name, page);
            main.add(name, page);
            showRunePageByName(name);
        }
    }

    private void showRunePageByName(String name) {
        this.layout.show(main, name);
        this.revalidate();
    }

    private Optional<String> getCurrentRunePageName() {
        Object item = selection.getSelectedItem();
        return Optional.ofNullable(item == null ? null : item.toString());
    }

    private Optional<ChampSelectRuneSelection> getCurrentRunePage() {
        Object item = selection.getSelectedItem();
        String name = item == null ? "" : item.toString();
        return Optional.ofNullable(map.getOrDefault(name, null));
    }

    public void setRuneSelection() {
        getCurrentRunePage().ifPresent(panel -> {
            Swiftrift.service.execute(() -> {
                ChampSelectContext context = instance.getContext();
                try {
                    LeagueClient client = context.getChampSelectDataContext().getLeagueClient();
                    JSONObject runes = panel.getSelectedRunes();
                    if (client != null) {
                        client.getLedge().getPerks().setRunesForCurrentRegistration(runes);
                    }
                    //TODO save to local settings
                    Swiftrift swiftrift = context.getChampSelectInterfaceContext().getLeagueClientUI();
                    UserSettings settings = swiftrift.getSettingService().getUserSettings();

                } catch (IncompleteRunePageException e) {
                    Swiftrift.showMessageDialog("Rune Page incomplete");
                } catch (IOException e) {
                    Swiftrift.showMessageDialog("Failed to save Rune Page");
                }
            });
        });
    }

    public LFlatButton getCloseButton() {
        return close;
    }
}
