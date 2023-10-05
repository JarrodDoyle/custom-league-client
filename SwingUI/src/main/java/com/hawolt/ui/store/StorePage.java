package com.hawolt.ui.store;

import com.hawolt.async.Debouncer;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.misc.SortOrder;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LCheckBox;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.component.LHintTextField;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created: 09/08/2023 18:45
 * Author: Twitter @hawolt
 **/

public class StorePage extends ChildUIComponent implements IStorePage {

    private final StoreElementComparator alphabeticalComparator = new StoreElementComparator(StoreSortProperty.NAME, SortOrder.ASCENDING);
    private boolean chromaFilter, tftFilter, ownedFilter, saleFilter = false;
    private final Map<Long, StoreElement> storeElementMap = new HashMap<>();
    private final String[] tft_options = {"TACTICIANS", "ARENA SKINS"};
    private final String[] skins_options = {"SKINS", "CHROMAS"};
    private final Debouncer debouncer = new Debouncer();
    private final StoreElementComparator comparator;
    private final ChildUIComponent inputPanel;
    private final LScrollPane scrollPane;
    private final ChildUIComponent grid;
    private final LeagueClient client;
    private final List<Long> owned;
    private String filter = "";
    private final String name;

    public StorePage(LeagueClient client, String name, List<Long> owned, StoreSortProperty... properties) {
        super(new BorderLayout(0, 5));
        this.client = client;
        this.owned = owned;
        this.name = name;
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        grid = new ChildUIComponent(new GridLayout(0, 4, 15, 15));
        add(component, BorderLayout.NORTH);
        comparator = new StoreElementComparator(properties.length > 0 ? properties[0] : null, SortOrder.DESCENDING);
        inputPanel = createInputPanel(properties);
        this.add(inputPanel, BorderLayout.WEST);
        component.add(grid, BorderLayout.NORTH);
        scrollPane = new LScrollPane(component);
        scrollPane.setBackground(ColorPalette.backgroundColor);
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                StorePage.this.loadViewport();
            }
        });
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        setBorder(new EmptyBorder(5, 5, 5, 0));
    }

    private void loadViewport() {
        Rectangle visible = grid.getVisibleRect();
        for (Component child : grid.getComponents()) {
            if (child instanceof StoreElement element) {
                Rectangle bounds = element.getBounds();
                debouncer.debounce(String.valueOf(element.getItem().getItemId()), () -> {
                    if (bounds.intersects(visible)) {
                        element.getImage().load();
                    } else {
                        element.getImage().unload();
                    }
                }, 100L, TimeUnit.MILLISECONDS);
            }
        }
    }

    @NotNull
    private ChildUIComponent createInputPanel(StoreSortProperty[] properties) {
        LComboBox<StoreSortOption> sortBox = createStoreSortOptionJComboBox(properties);

        ChildUIComponent inputPanel = new ChildUIComponent();
        inputPanel.setBackground(ColorPalette.backgroundColor);
        inputPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 0, 2, Color.DARK_GRAY),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
        inputPanel.setLayout(new GridLayout(10, 1, 0, 5));

        if (this.name.equals("SKINS")) {
            LComboBox<String> options = new LComboBox<>(this.skins_options);
            options.addItemListener(listener -> {
                chromaFilter = Objects.equals(options.getSelectedItem(), this.skins_options[1]);
                debouncer.debounce("chromaFilter", this::updateStoreElements, 150, TimeUnit.MILLISECONDS);
            });
            inputPanel.add(options);
        }
        if (this.name.equals("TFT")) {
            LComboBox<String> options = new LComboBox<>(this.tft_options);
            options.addItemListener(listener -> {
                tftFilter = Objects.equals(options.getSelectedItem(), this.tft_options[1]);
                debouncer.debounce("tftFilter", this::updateStoreElements, 150, TimeUnit.MILLISECONDS);
            });
            inputPanel.add(options);
        }

        LHintTextField search = new LHintTextField("Search...");
        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter = search.getText().toLowerCase();
                debouncer.debounce("searchField", () -> updateStoreElements(), 200, TimeUnit.MILLISECONDS);
            }
        });
        inputPanel.add(search);

        LCheckBox ownedCheck = new LCheckBox("Show Owned");
        ownedCheck.addActionListener(listener -> {
            ownedFilter = !ownedFilter;
            debouncer.debounce("ownedFilter", this::updateStoreElements, 200, TimeUnit.MILLISECONDS);
        });
        inputPanel.add(ownedCheck);

        inputPanel.add(sortBox);

        LCheckBox saleCheck = new LCheckBox("On Sale");
        saleCheck.addActionListener(listener -> {
            saleFilter = !saleFilter;
            debouncer.debounce("saleFilter", this::updateStoreElements, 200, TimeUnit.MILLISECONDS);
        });
        inputPanel.add(saleCheck);

        return inputPanel;
    }

    @NotNull
    private LComboBox<StoreSortOption> createStoreSortOptionJComboBox(StoreSortProperty[] properties) {
        LComboBox<StoreSortOption> sortBox = new LComboBox<>();
        for (StoreSortProperty property : properties) {
            sortBox.addItem(new StoreSortOption(property, SortOrder.DESCENDING));
            sortBox.addItem(new StoreSortOption(property, SortOrder.ASCENDING));
        }
        sortBox.addItemListener(listener -> {
            StoreSortOption option = sortBox.getItemAt(sortBox.getSelectedIndex());
            comparator.setProperty(option.property());
            comparator.setOrder(option.order());
            updateStoreElements();
        });
        return sortBox;
    }

    public ChildUIComponent getGrid() {
        return grid;
    }

    private boolean isOwned(StoreItem item) {
        return owned.contains(item.getItemId());
    }

    public void append(StoreItem item) {
        append(Collections.singletonList(item));
    }

    public void append(List<StoreItem> items) {
        try {
            for (StoreItem item : items) {
                if (item.getCorrectRiotPointCost() == 0 && !item.isBlueEssencePurchaseAvailable()) continue;
                if (item.hasVariantId())
                    if (owned.contains(item.getVariantId())) item.setVariantOwned(true);
                long itemId = item.getItemId();
                StoreElement element = new StoreElement(client, this, item, isOwned(item));
                storeElementMap.put(itemId, element);
                grid.add(element);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        updateStoreElements();
    }

    @Override
    public void removeStoreElement(StoreElement component) {
        grid.remove(component);
        storeElementMap.remove(component.getItem().getItemId());
        updateStoreElements();
    }

    public void updateStoreElements() {
        grid.removeAll();
        storeElementMap.values()
                .stream()
                .sorted(this.alphabeticalComparator)
                .sorted(this.comparator)
                .filter(champion -> champion.getItem().getName().toLowerCase().contains(filter))
                .filter(skin -> skin.getItem().isChroma() == chromaFilter)
                .filter(skin -> skin.getItem().isTFTMapSkin() == tftFilter)
                .filter(item -> owned.contains(item.getItem().getItemId()) == ownedFilter || !owned.contains(item.getItem().getItemId()))
                .filter(item -> item.getItem().hasDiscount() == saleFilter)
                .forEach(this.grid::add);
        revalidate();
        repaint();
    }
}
