package com.pickgok.track.model;

public class TrackDTO {
    private int trackId;
    private String title;
    private String artist;
    private String genre;
    private String filePath;

    // Getters and Setters (생략 가능, Eclipse 자동 생성 기능 사용 권장)
    public int getTrackId() { return trackId; }
    public void setTrackId(int trackId) { this.trackId = trackId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}