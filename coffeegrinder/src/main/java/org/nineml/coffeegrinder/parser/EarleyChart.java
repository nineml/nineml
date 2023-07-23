package org.nineml.coffeegrinder.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The chart used for Earley parsing.
 */
public class EarleyChart {
    private final ArrayList<ArrayList<EarleyItem>> chart;
    private final ArrayList<HashSet<EarleyItem>> rowmaps;

    protected EarleyChart() {
        chart = new ArrayList<>();
        rowmaps = new ArrayList<>();
    }

    /**
     * How big is the chart?
     * @return the number of rows in the chart.
     */
    public int size() {
        return chart.size();
    }

    /**
     * Get a row from the chart.
     * <p>The chart will be enlarged if necessary.</p>
     * @param row the row number (0-indexed).
     * @return the contents of the row.
     */
    public List<EarleyItem> get(int row) {
        assureRow(row);
        return chart.get(row);
    }

    /**
     * Determine if an item is in the chart.
     * <p>This method will be faster than a linear search of the row.</p>
     * @param row the row to search
     * @param item the item to search form
     * @return true if the row contains the item
     */
    public boolean contains(int row, EarleyItem item) {
        assureRow(row);
        return rowmaps.get(row).contains(item);
    }

    protected void clear() {
        chart.clear();
        rowmaps.clear();
    }

    protected ArrayList<ArrayList<EarleyItem>> rows() {
        return chart;
    }

    protected void add(int row, EarleyItem item) {
        assureRow(row);
        rowmaps.get(row).add(item);
        chart.get(row).add(item);
    }

    private void assureRow(int row) {
        if (chart.size() <= row) {
            chart.add(new ArrayList<>());
            rowmaps.add(new HashSet<>());
        }
    }
}
