package bask.lingvino.models;

import androidx.annotation.NonNull;

public class Translation {
    private String input;
    private String pronunciationURL;
    private String translation;
    private Boolean expanded;
    private String id;

    public Translation() {}

    public Translation(String input, String pronunciationURL, String translation) {
        this.input = input;
        this.pronunciationURL = pronunciationURL;
        this.translation = translation;
        this.expanded = false;
    }

    public String getInput() {
        return input;
    }

    public String getTranslation() {
        return translation;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Translation{" +
                "input='" + input + '\'' +
                ", pronunciationURL='" + pronunciationURL + '\'' +
                ", translation='" + translation + '\'' +
                ", expanded=" + expanded +
                ", id='" + id + '\'' +
                '}';
    }
}
