package com.google;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** A class used to represent a Playlist */
class VideoPlaylist {
  private String name;
  private List<Video> videos = new ArrayList<>();

  public VideoPlaylist(String name) {
    this.name = name;
  }

  public List<Video> getVideos() {
    return videos;
  }

  public void addVideo(Video video) {
    videos.add(video);
  }

  public void removeVideo(Video video) {
    videos.remove(video);
  }

  public void removeAllVideos() {
    videos.clear();
  }

  /** Returns true iff playlist contains the given video */
  public boolean containsVideo(String videoId) {
    return getVideos().stream()
        .map(Video::getVideoId)
        .collect(Collectors.toList())
        .contains(videoId);
  }

  /** Prints all unflagged videos in playlist, notifying user if playlist empty */
  public void printVideos() {
    System.out.println("Showing playlist: " + getName());
    if (videos.isEmpty()) {
      System.out.println(" No videos here yet");
      return;
    }
    videos.forEach(
        v ->
            System.out.println(
                v.show()
                    + (v.isFlagged() ? " - FLAGGED (reason: " + v.getFlagReason() + ')' : "")));
  }

  public String getName() {
    return name;
  }
}
