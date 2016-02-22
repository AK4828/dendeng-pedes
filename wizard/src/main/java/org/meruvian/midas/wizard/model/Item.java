package org.meruvian.midas.wizard.model;

/**
 * Created by ludviantoovandi on 18/02/15.
 */
public class Item {
    private String key;
    private String value;
    private String displayName;

    public Item(String key, String value) {
        this(key, value, "");
    }

    public Item(String key, String value, String displayName) {
        this.key = key;
        this.value = value;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return displayName == null || "".equalsIgnoreCase(displayName) ? key : displayName;
    }
}
