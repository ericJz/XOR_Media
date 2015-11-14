package com.zw.player.playerclient.service;

import java.util.ArrayList;

public class SongsList {
  public static ArrayList<Songs> SongsList;

  public SongsList() {
    if (SongsList == null) {
      SongsList = DatabaseHelper.getSongsList();
    }
  }
}
