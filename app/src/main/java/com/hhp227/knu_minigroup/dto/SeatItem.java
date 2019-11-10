package com.hhp227.knu_minigroup.dto;

public class SeatItem {
    public int id;
    public String name;
    public int activeTotal;
    public int occupied;
    public int available;
    public String[] disable;

    public SeatItem(int id, String name, int activeTotal, int occupied, int available, String[] disable) {
        this.id = id;
        this.name = name;
        this.activeTotal = activeTotal;
        this.occupied = occupied;
        this.available = available;
        this.disable = disable;
    }
}
