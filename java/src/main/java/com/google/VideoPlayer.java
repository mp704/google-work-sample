package com.google;

import java.util.*;
import java.util.stream.Collectors;

public class VideoPlayer {

  private final VideoLibrary videoLibrary;
  private Video currentlyPlaying = null;
  private Video currentlyPaused = null;
  private List<VideoPlaylist> playlists = new ArrayList<>();

  public VideoPlayer() {
    this.videoLibrary = new VideoLibrary();
  }

  public void numberOfVideos() {
    System.out.printf("%s videos in the library%n", videoLibrary.getVideos().size());
  }

  /** Prints all videos in the library in lexicographical order */
  public void showAllVideos() {
    System.out.println("Here's a list of all available videos:");
    List<Video> libraryVideos = videoLibrary.getVideos();
    // Sort videos lexicographically, then show each one
    libraryVideos.sort(Comparator.comparing(Video::getTitle));
    libraryVideos.forEach(
        v ->
            System.out.println(
                v.show()
                    + (v.isFlagged() ? " - FLAGGED (reason: " + v.getFlagReason() + ')' : "")));
  }

  /** Plays a video, if it exists in the library and is not flagged, else prints a warning */
  public void playVideo(String videoId) {
    Video videoToPlay = videoLibrary.getVideo(videoId);
    // Print warning message if video does not exist in library
    if (videoToPlay == null) {
      System.out.println("Cannot play video: Video does not exist");
      return;
    }
    // Print warning message if video is flagged
    if (videoToPlay.isFlagged()) {
      System.out.println(
          "Cannot play video: Video is currently flagged (reason: "
              + videoToPlay.getFlagReason()
              + ')');
      return;
    }
    // Playing a (new) video stops any currently playing/paused videos
    if (currentlyPlaying != null) {
      System.out.println("Stopping video: " + currentlyPlaying.getTitle());
    } else if (currentlyPaused != null) {
      System.out.println("Stopping video: " + currentlyPaused.getTitle());
    }
    currentlyPlaying = videoToPlay;
    currentlyPaused = null;
    System.out.println("Playing video: " + currentlyPlaying.getTitle());
  }

  /**
   * Stop the current playing/paused video, printing a warning if no video is currently
   * playing/paused
   */
  public void stopVideo() {
    if (currentlyPlaying != null) {
      System.out.println("Stopping video: " + currentlyPlaying.getTitle());
    } else if (currentlyPaused != null) {
      System.out.println("Stopping video: " + currentlyPaused.getTitle());
    } else {
      System.out.println("Cannot stop video: No video is currently playing");
      return;
    }
    currentlyPlaying = null;
    currentlyPaused = null;
  }

  /** Plays a random, unflagged video in the library, notifying user if no such video exists */
  public void playRandomVideo() {
    Random seed = new Random();
    List<Video> freeVideos =
        videoLibrary.getVideos().stream().filter(v -> !v.isFlagged()).collect(Collectors.toList());
    if (freeVideos.isEmpty()) {
      System.out.println("No videos available");
      return;
    }
    Video randomVideo = freeVideos.get(seed.nextInt(freeVideos.size()));
    playVideo(randomVideo.getVideoId());
  }

  /**
   * Pauses a video, displaying a warning message if no video is playing or the current video is
   * already paused
   */
  public void pauseVideo() {
    if (currentlyPaused != null) {
      System.out.println("Video already paused: " + currentlyPaused.getTitle());
      return;
    }
    if (currentlyPlaying == null) {
      System.out.println("Cannot pause video: No video is currently playing");
      return;
    }
    System.out.println("Pausing video: " + currentlyPlaying.getTitle());
    currentlyPaused = currentlyPlaying;
    currentlyPlaying = null;
  }

  /**
   * Continues any currently paused video, printing an appropriate warning message if no video is
   * paused
   */
  public void continueVideo() {
    if (currentlyPaused != null) {
      currentlyPlaying = currentlyPaused;
      currentlyPaused = null;
      System.out.println("Continuing video: " + currentlyPlaying.getTitle());
      return;
    }
    if (currentlyPlaying == null) {
      System.out.println("Cannot continue video: No video is currently playing");
    } else {
      System.out.println("Cannot continue video: Video is not paused");
    }
  }

  /**
   * Shows the video currently active, including its pause status; returns a warning message if no
   * videos are playing
   */
  public void showPlaying() {
    if (currentlyPlaying == null && currentlyPaused == null) {
      System.out.println("No video is currently playing");
      return;
    }
    Video currentVideo = currentlyPlaying != null ? currentlyPlaying : currentlyPaused;
    System.out.println(
        "Currently playing: "
            + currentVideo.show()
            + (currentVideo.equals(currentlyPaused) ? " - PAUSED" : ""));
  }

  /**
   * Returns an Optional containing a playlist if it exists in the list, else empty optional. Helper
   * function for playlist methods
   */
  private Optional<VideoPlaylist> findPlaylist(String name) {
    return playlists.stream()
        .filter(p -> p.getName().toUpperCase().equals(name.toUpperCase()))
        .findFirst();
  }

  /** Returns true iff the playlist with the given name exists in the current video player */
  private boolean playlistExists(String name) {
    return findPlaylist(name).isPresent();
  }

  /**
   * Creates a new playlist, adding it to the list of playlists in the current video player. Prints
   * a warning message if a playlist with the same name, in any case, exists
   */
  public void createPlaylist(String playlistName) {
    if (playlistExists(playlistName)) {
      System.out.println("Cannot create playlist: A playlist with the same name already exists");
      return;
    }
    playlists.add(new VideoPlaylist(playlistName));
    System.out.println("Successfully created new playlist: " + playlistName);
  }

  /**
   * Adds a video to a playlist, displaying warning message if playlist/videoId don't exist, if the
   * video is flagged or if the playlist already contains the video
   */
  public void addVideoToPlaylist(String playlistName, String videoId) {
    if (!playlistExists(playlistName)) {
      System.out.println("Cannot add video to " + playlistName + ": Playlist does not exist");
      return;
    }
    VideoPlaylist requiredPlaylist = findPlaylist(playlistName).get();
    if (videoLibrary.getVideo(videoId) == null) {
      System.out.println("Cannot add video to " + playlistName + ": Video does not exist");
      return;
    }
    Video videoToAdd = videoLibrary.getVideo(videoId);
    if (videoToAdd.isFlagged()) {
      System.out.println(
          "Cannot add video to "
              + playlistName
              + ": Video is currently flagged (reason: "
              + videoToAdd.getFlagReason()
              + ')');
      return;
    }
    // If video is already in playlist but is flagged, the primary warning is due to flagging, since
    // even if video wasn't in playlist it wouldn't be added
    if (requiredPlaylist.containsVideo(videoId) && videoToAdd.isFlagged()) {
      System.out.println(
          "Cannot add video to "
              + playlistName
              + ": Video is currently flagged (reason: "
              + videoToAdd.getFlagReason()
              + ')');
      return;
    }
    if (requiredPlaylist.containsVideo(videoId)) {
      System.out.println("Cannot add video to " + playlistName + ": Video already added");
      return;
    }
    requiredPlaylist.addVideo(videoToAdd);
    System.out.println("Added video to " + playlistName + ": " + videoToAdd.getTitle());
  }

  /**
   * Prints all playlists in lexicographical order, ignoring case, displaying message if no
   * playlists exist
   */
  public void showAllPlaylists() {
    if (playlists.isEmpty()) {
      System.out.println("No playlists exist yet");
      return;
    }
    System.out.println("Showing all playlists:");
    playlists.sort(Comparator.comparing(p -> p.getName().toUpperCase()));
    playlists.forEach(p -> System.out.println(" " + p.getName()));
  }

  /**
   * Prints playlist, including name and all unflagged videos in playlist, notifying user if
   * playlist empty and displaying warning if playlist non-existent
   */
  public void showPlaylist(String playlistName) {
    if (!playlistExists(playlistName)) {
      System.out.println("Cannot show playlist " + playlistName + ": Playlist does not exist");
      return;
    }
    VideoPlaylist playlistToShow = findPlaylist(playlistName).get();
    System.out.println("Showing playlist: " + playlistName);
    if (playlistToShow.getVideos().isEmpty()) {
      System.out.println(" No videos here yet");
      return;
    }
    playlistToShow
        .getVideos()
        .forEach(
            v ->
                System.out.println(
                    v.show()
                        + (v.isFlagged() ? " - FLAGGED (reason: " + v.getFlagReason() + ')' : "")));
  }

  /**
   * Removes video with given name from given playlist, displaying a warning if either do not exist,
   * or the video is not in the playlist
   */
  public void removeFromPlaylist(String playlistName, String videoId) {
    // If either video nor playlist exist, display warning message (playlist check takes priority)
    if (!playlistExists(playlistName)) {
      System.out.println("Cannot remove video from " + playlistName + ": Playlist does not exist");
      return;
    }
    if (videoLibrary.getVideo(videoId) == null) {
      System.out.println("Cannot remove video from " + playlistName + ": Video does not exist");
      return;
    }
    Video videoToRemove = videoLibrary.getVideo(videoId);
    VideoPlaylist requiredPlaylist = findPlaylist(playlistName).get();
    if (!requiredPlaylist.getVideos().contains(videoToRemove)) {
      System.out.println("Cannot remove video from " + playlistName + ": Video is not in playlist");
      return;
    }
    requiredPlaylist.removeVideo(videoToRemove);
    System.out.println("Removed video from " + playlistName + ": " + videoToRemove.getTitle());
  }

  /** Clears a playlist, displaying a warning if it does not exist */
  public void clearPlaylist(String playlistName) {
    if (!playlistExists(playlistName)) {
      System.out.println("Cannot clear playlist " + playlistName + ": Playlist does not exist");
      return;
    }
    findPlaylist(playlistName).get().removeAllVideos();
    System.out.println("Successfully removed all videos from " + playlistName);
  }

  /**
   * Deletes a playlist from list of playlists inside current video player, displaying a warning if
   * playlist does not exist
   */
  public void deletePlaylist(String playlistName) {
    if (!playlistExists(playlistName)) {
      System.out.println("Cannot delete playlist " + playlistName + ": Playlist does not exist");
      return;
    }
    playlists.remove(findPlaylist(playlistName));
    System.out.println("Deleted playlist: " + playlistName);
  }

  //  /** Helper method for searchDisplayAndPlayVideos(). Displays sorted, unflagged videos,
  // notifying user if no videos to display */
  //  private void displayVideosNumerically(List<Video> videosToDisplay, String searchString) {
  //    if (videosToDisplay.isEmpty()) {
  //      System.out.println("No search results for " + searchString);
  //      return;
  //    }
  //    System.out.println("Here are the results for " + searchString + ':');
  //    // Keep track of number of results in order to numerical display them
  //    int i = 1;
  //    for (Video video : videosToDisplay) {
  //      System.out.println(" " + i + ") " + video.show());
  //      i++;
  //    }
  //  }
  //
  //  /** Helper method for searchDisplayAndPlayVideos(). Enables user to specify a video to play,
  // returning if user doesn't answer or enters invalid number */
  //  private void enableUserToPlayVideo(List<Video> videosToChooseFrom) {
  //    System.out.println(
  //            "Would you like to play any of the above? If yes, specify the number of the
  // video.");
  //    System.out.println("If your answer is not a valid number, we will assume it's a no.");
  //    Scanner scanner = new Scanner(System.in);
  //    String answer = scanner.nextLine();
  //    try {
  //      int answerNumber = Integer.parseInt(answer);
  //      if (answerNumber <= videosToChooseFrom.size()) {
  //        playVideo(videosToChooseFrom.get(answerNumber - 1).getVideoId());
  //      }
  //    } finally {
  //      return;
  //    }
  //  }

  /**
   * Searches for videos, displays and enables the user to play the videos whose title contains the
   * given search term.
   */
  public void searchVideos(String searchTerm) {
    List<Video> videosWithSearchTerm =
        videoLibrary.getVideos().stream()
            .filter(
                v ->
                    v.getTitle().toUpperCase().contains(searchTerm.toUpperCase()) && !v.isFlagged())
            .collect(Collectors.toList());
    videosWithSearchTerm.sort(Comparator.comparing(Video::getTitle));
    if (videosWithSearchTerm.isEmpty()) {
      System.out.println("No search results for " + searchTerm);
      return;
    }
    System.out.println("Here are the results for " + searchTerm + ':');
    // Keep track of number of results in order to numerical display them
    int i = 1;
    for (Video video : videosWithSearchTerm) {
      System.out.println(" " + i + ") " + video.show());
      i++;
    }
    System.out.println(
        "Would you like to play any of the above? If yes, specify the number of the video.");
    System.out.println("If your answer is not a valid number, we will assume it's a no.");
    Scanner scanner = new Scanner(System.in);
    String answer = scanner.nextLine();
    try {
      int answerNumber = Integer.parseInt(answer);
      if (answerNumber <= videosWithSearchTerm.size()) {
        playVideo(videosWithSearchTerm.get(answerNumber - 1).getVideoId());
      }
    } finally {
      return;
    }
  }

  /**
   * Searches for videos, displays and enables the user to play the videos whose list of tags
   * contains the given search tag
   */
  public void searchVideosWithTag(String videoTag) {
    List<Video> videosWithVideoTag =
        videoLibrary.getVideos().stream()
            .filter(
                v ->
                    v.getTags().stream()
                            .map(String::toUpperCase)
                            .collect(Collectors.toList())
                            .contains(videoTag.toUpperCase())
                        && !v.isFlagged())
            .collect(Collectors.toList());
    videosWithVideoTag.sort(Comparator.comparing(Video::getTitle));
    if (videosWithVideoTag.isEmpty()) {
      System.out.println("No search results for " + videoTag);
      return;
    }
    System.out.println("Here are the results for " + videoTag + ':');
    // Keep track of number of results in order to numerical display them
    int i = 1;
    for (Video video : videosWithVideoTag) {
      System.out.println(" " + i + ") " + video.show());
      i++;
    }
    System.out.println(
        "Would you like to play any of the above? If yes, specify the number of the video.");
    System.out.println("If your answer is not a valid number, we will assume it's a no.");
    Scanner scanner = new Scanner(System.in);
    String answer = scanner.nextLine();
    try {
      int answerNumber = Integer.parseInt(answer);
      if (answerNumber <= videosWithVideoTag.size()) {
        playVideo(videosWithVideoTag.get(answerNumber - 1).getVideoId());
      }
    } finally {
      return;
    }
  }

  /** Flags a video, without given a reason */
  public void flagVideo(String videoId) {
    flagVideo(videoId, "Not supplied");
  }

  /**
   * Flags a video, storing the reason and stopping the video if it's currently active. If video is
   * already flagged or does not exist, displays a warning message.
   */
  public void flagVideo(String videoId, String reason) {
    Video videoToFlag = videoLibrary.getVideo(videoId);
    if (videoToFlag == null) {
      System.out.println("Cannot flag video: Video does not exist");
      return;
    }
    if (videoToFlag.isFlagged()) {
      System.out.println("Cannot flag video: Video is already flagged");
      return;
    }
    if (currentlyPlaying != null && currentlyPlaying.equals(videoToFlag)) {
      stopVideo();
    } else if (currentlyPaused != null && currentlyPaused.equals(videoToFlag)) {
      stopVideo();
    }
    videoToFlag.setFlagged(true);
    videoToFlag.setFlagReason(reason == "" ? "Not supplied" : reason);
    System.out.println(
        "Successfully flagged video: "
            + videoToFlag.getTitle()
            + " (reason: "
            + videoToFlag.getFlagReason()
            + ')');
  }

  /**
   * Unflags a previously flagged video, displaying warning messages if the video is not flagged or
   * does not exist
   */
  public void allowVideo(String videoId) {
    if (videoLibrary.getVideo(videoId) == null) {
      System.out.println("Cannot remove flag from video: Video does not exist");
      return;
    }
    Video videoToUnflag = videoLibrary.getVideo(videoId);
    if (!videoToUnflag.isFlagged()) {
      System.out.println("Cannot remove flag from video: Video is not flagged");
      return;
    }
    videoToUnflag.setFlagged(false);
    videoToUnflag.setFlagReason(null);
    System.out.println("Successfully removed flag from video: " + videoToUnflag.getTitle());
  }
}
