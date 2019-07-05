package com.vdurmont.emoji;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Loads the emojis from a JSON database.
 *
 * @author Vincent DURMONT [vdurmont@gmail.com]
 */
public class EmojiLoader {
  /**
   * No need for a constructor, all the methods are static.
   */
  private EmojiLoader() {}

  /**
   * Loads a JSONArray of emojis from an InputStream, parses it and returns the
   * associated list of {@link com.vdurmont.emoji.Emoji}s
   *
   * @param stream the stream of the JSONArray
   *
   * @return the list of {@link com.vdurmont.emoji.Emoji}s
   * @throws IOException if an error occurs while reading the stream or parsing
   * the JSONArray
   */
  public static List<Emoji> loadEmojis(InputStream stream) throws IOException {
    JSONArray emojisJSON = new JSONArray(inputStreamToString(stream));
    List<Emoji> emojis = new ArrayList<Emoji>(emojisJSON.length());
    for (int i = 0; i < emojisJSON.length(); i++) {
      Emoji emoji = buildEmojiFromJSON(emojisJSON.getJSONObject(i));
      if (emoji != null) {
        emojis.add(emoji);
      }
    }
    return emojis;
  }

  private static String inputStreamToString(
    InputStream stream
  ) throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
    BufferedReader br = new BufferedReader(isr);
    String read;
    while((read = br.readLine()) != null) {
      sb.append(read);
    }
    br.close();
    return sb.toString();
  }

  private static String codePointToString(String point)
  {
      String ret;
      if (point == null || point.isEmpty())
      {
          return point;
      }
      int unicodeScalar = Integer.parseInt(point, 16);
      if (Character.isSupplementaryCodePoint(unicodeScalar))
      {
          ret = String.valueOf(Character.toChars(unicodeScalar));
      } else {
          ret = String.valueOf((char) unicodeScalar);
      }
      return ret;
  }

  private static String rawCodePointsToString(String rawPoint)
  {
      String[] points = rawPoint.split("-");
      StringBuilder ret = new StringBuilder();
      for (String hexPoint : points){
          ret.append(codePointToString(hexPoint));
      }
      return ret.toString();
  }
  protected static Emoji buildEmojiFromJSON(
    JSONObject json
  ) throws UnsupportedEncodingException {
    if (!json.has("unified")) {
      return null;
    }

    byte[] bytes = rawCodePointsToString(json.getString("unified")).getBytes(StandardCharsets.UTF_8);
    String description = null;
    if (json.has("name") && !json.isNull("name")) {
      description = json.getString("name").toLowerCase();
    }
    boolean supportsFitzpatrick = false;
    if (json.has("supports_fitzpatrick")) {
      supportsFitzpatrick = json.getBoolean("supports_fitzpatrick");
    }
    List<String> aliases = jsonArrayToStringList(json.getJSONArray("short_names"));
    List<String> tags = null;
    List<String> texts = null;
    
    if (json.has("texts") && !json.isNull("texts")) {
        texts = jsonArrayToStringList(json.getJSONArray("texts"));
        texts.add(0, json.getString("text"));
    }
    return new Emoji(description, supportsFitzpatrick, aliases, tags, texts, bytes);
  }

  private static List<String> jsonArrayToStringList(JSONArray array) {
    List<String> strings = new ArrayList<String>(array.length());
    for (int i = 0; i < array.length(); i++) {
      strings.add(array.getString(i));
    }
    return strings;
  }
}
