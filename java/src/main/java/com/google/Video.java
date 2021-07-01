package com.google;

import java.util.Collections;
import java.util.List;

/** A class used to represent a video. */
class Video {

  private final String title;
  private final String videoId;
  private final List<String> tags;

  private boolean flagged;
  private String flagReason;

  Video(String title, String videoId, List<String> tags) {
    this.title = title;
    this.videoId = videoId;
    this.tags = Collections.unmodifiableList(tags);

    this.flagged = false;
    this.flagReason = null;
  }

  /** Returns the title of the video. */
  String getTitle() {
    return title;
  }

  /** Returns the video id of the video. */
  String getVideoId() {
    return videoId;
  }

  /** Returns a readonly collection of the tags of the video. */
  List<String> getTags() {
    return tags;
  }

  public boolean isFlagged() {
    return flagged;
  }

  public String getFlagReason() {
    return flagReason;
  }

  public void setFlagged(boolean flagged) {
    this.flagged = flagged;
  }

  public void setFlagReason(String flagReason) {
    this.flagReason = flagReason;
  }

  /** Returns an individual video, in the printable format “title (video_id) [tags]. */
  public String show() {
    return getTitle()
        + " ("
        + getVideoId()
        + ") "
        + '['
        + getTags().stream().reduce("", (s, t) -> s + (s.equals("") ? "" : " ") + t)
        + ']';
  }
}
