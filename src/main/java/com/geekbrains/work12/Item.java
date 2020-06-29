package com.geekbrains.work12;

import javax.persistence.*;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "val")
    int val;

    public void setVal(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public Item() {
    }

    public Item(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return String.format("Item [ id = %d, val = %d ]", id, val);
    }
}

