package org.aksw.fox.tools.linking.common;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.agdistis.datatypes.Document;
import org.aksw.agdistis.datatypes.DocumentText;
import org.aksw.agdistis.datatypes.NamedEntitiesInText;
import org.aksw.agdistis.datatypes.NamedEntityInText;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Voc;
import org.aksw.fox.tools.linking.AbstractLinking;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Agdistis extends AbstractLinking {

  public static final String CFG_KEY_AGDISTIS_ENDPOINT = "agdistis.endpoint";

  // maps AGDISTIS index to real index
  protected Map<Integer, Entity> indexToEntities = new HashMap<>();
  protected String endpoint;

  public Agdistis() {}

  public Agdistis(final Class<?> classs) {
    endpoint = cfgManager.getCfg(classs).getString(CFG_KEY_AGDISTIS_ENDPOINT);
  }

  @Override
  public void setUris(final List<Entity> entities, final String input) {
    LOG.info("AGDISTISLookup ...");

    String agdistis_input = makeInput(entities, input);

    LOG.info("AGDISTISLookup sending...");
    String agdistis_output = "";
    try {
      agdistis_output = send(agdistis_input);
      agdistis_input = null;
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    LOG.info("AGDISTISLookup sending done.");

    addURItoEntities(agdistis_output, entities);

    LOG.info("AGDISTISLookup done..");
    indexToEntities.clear();

    this.entities = entities;
  }

  /**
   * Iterates over ordered entities to change the input text for Agdistis with meta tags. Later, to
   * map entities from Agdistis output to the entities, we store the index of an entity to itself in
   * {@link #indexToEntities}.
   *
   * @param entities
   * @param input
   * @return Agdistis input
   */
  private String makeInput(final List<Entity> entities, final String input) {

    String agdistis_input = "";
    int last = 0;

    // sorted by entity index
    for (final Entity entity : entities.stream().sorted().collect(Collectors.toList())) {

      agdistis_input += input.substring(last, entity.getBeginIndex());

      agdistis_input += "<entity>" + entity.getText() + "</entity>";

      last = entity.getBeginIndex() + entity.getText().length();
      indexToEntities.put(entity.getBeginIndex(), entity);
    }
    return agdistis_input.concat(input.substring(last));
  }

  protected String send(final String agdistis_input) throws Exception {

    // String data = parameter + agdistis_input;
    final String urlParameters =
        "text=" + URLEncoder.encode(agdistis_input, "UTF-8") + "&type=agdistis&heuristic=false";
    final URL url = new URL(endpoint);

    final HttpURLConnection http = (HttpURLConnection) url.openConnection();

    http.setRequestMethod("POST");
    http.setDoInput(true);
    http.setDoOutput(true);
    http.setUseCaches(false);
    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    http.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
    // http.setRequestProperty("Content-Length",
    // String.valueOf(data.length()));

    final OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
    writer.write(urlParameters);
    writer.flush();

    return IOUtils.toString(http.getInputStream(), "UTF-8");
  }

  /**
   *
   * @param json agdistis output
   * @param entities
   */
  protected void addURItoEntities(final String json, final List<Entity> entities) {

    if (json != null && json.length() > 0) {
      final JSONArray array = new JSONArray(json);

      for (int i = 0; i < array.length(); i++) {

        final Integer agdistisindex = array.getJSONObject(i).getInt("start");
        final String disambiguatedURL = (String) array.getJSONObject(i).get("disambiguatedURL");

        if (agdistisindex != null && agdistisindex > -1) {

          final Entity entity = indexToEntities.get(agdistisindex);

          if (disambiguatedURL == null) {
            URI uri;
            try {
              uri = new URI(Voc.ns_fox_resource + entity.getText().replaceAll(" ", "_"));
              entity.setUri(uri.toASCIIString());
            } catch (final URISyntaxException e) {
              entity.setUri(Voc.ns_fox_resource + entity.getText());
              LOG.error(entity.getUri() + "\n", e);
            }
          } else {
            entity.setUri(urlencode(disambiguatedURL));
          }
        }

      }
    }
  }

  protected String urlencode(final String disambiguatedURL) {
    try {
      final String encode = URLEncoder.encode(disambiguatedURL
          .substring(disambiguatedURL.lastIndexOf('/') + 1, disambiguatedURL.length()), "UTF-8");
      return disambiguatedURL.substring(0, disambiguatedURL.lastIndexOf('/') + 1).concat(encode);

    } catch (final Exception e) {
      LOG.error(disambiguatedURL + "\n", e);
      return disambiguatedURL;
    }
  }

  // new
  public String standardAG(final String text, final NEDAlgo_HITS agdistis) {
    final JSONArray arr = new JSONArray();

    final Document d = textToDocument(text);
    agdistis.run(d, null);

    for (final NamedEntityInText namedEntity : d.getNamedEntitiesInText()) {
      if (!namedEntity.getNamedEntityUri().contains("http")) {
        namedEntity.setNamedEntity(Voc.akswNotInWiki + namedEntity.getSingleWordLabel());
      }
      final JSONObject obj = new JSONObject();
      obj.put("namedEntity", namedEntity.getLabel());
      obj.put("start", namedEntity.getStartPos());
      obj.put("offset", namedEntity.getLength());
      obj.put("disambiguatedURL", namedEntity.getNamedEntityUri());
      arr.put(obj);
    }
    return arr.toString();

  }

  public Document textToDocument(final String preAnnotatedText) {
    final Document document = new Document();
    final ArrayList<NamedEntityInText> list = new ArrayList<>();
    int startpos = 0, endpos = 0;
    final StringBuilder sb = new StringBuilder();
    startpos = preAnnotatedText.indexOf("<entity>", startpos);
    while (startpos >= 0) {
      sb.append(preAnnotatedText.substring(endpos, startpos));
      startpos += 8;
      endpos = preAnnotatedText.indexOf("</entity>", startpos);
      final int newStartPos = sb.length();
      final String entityLabel = preAnnotatedText.substring(startpos, endpos);
      list.add(new NamedEntityInText(newStartPos, entityLabel.length(), entityLabel, ""));
      sb.append(entityLabel);
      endpos += 9;
      startpos = preAnnotatedText.indexOf("<entity>", startpos);
    }

    final NamedEntitiesInText nes = new NamedEntitiesInText(list);
    final DocumentText text =
        new DocumentText(preAnnotatedText.replaceAll("<entity>", "").replaceAll("</entity>", ""));

    document.addText(text);
    document.addNamedEntitiesInText(nes);
    return document;
  }
}
