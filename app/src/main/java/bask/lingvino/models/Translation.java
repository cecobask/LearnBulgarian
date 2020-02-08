package bask.lingvino.models;

import androidx.annotation.NonNull;

public class Translation {
    private String input;
    private String pronunciationURL;
    private String translation;
    private Boolean expanded;
    private String id;
    private String sourceLang;
    private String targetLang;

    public Translation() {}

    public Translation(String input, String pronunciationURL, String translation, String sourceLang,
                       String targetLang) {
        this.input = input;
        this.pronunciationURL = pronunciationURL;
        this.translation = translation;
        this.expanded = false;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
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

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
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
