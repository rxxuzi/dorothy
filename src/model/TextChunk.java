package model;

public class TextChunk {
    private String keyword;
    private String text;
    private boolean isEncrypted;

    public TextChunk(String keyword, String text, boolean isEncrypted) {
        this.keyword = keyword;
        this.text = text;
        this.isEncrypted = isEncrypted;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getDisplayText(int maxLength) {
        String display = text;
        if (text.length() > maxLength) {
            display = text.substring(0, maxLength) + "...";
        }
        return display;
    }
}