package org.aksw.fox.nerlearner.reader;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.data.EntityTypes;
import org.aksw.simba.knowledgeextraction.commons.io.Compress;

public class WikinerReader extends ANERReader {

  protected StringBuilder input = new StringBuilder();
  protected HashMap<String, String> entities = new HashMap<>();
  protected HashMap<String, List<SimpleEntry<String, Integer>>> disambEntities = new HashMap<>();

  public static void main(final String[] a) throws IOException {

    final String[] files = new String[1];
    files[0] = "input/Wikiner/aij-wikiner-en-wp3.bz2";

    final WikinerReader r = new WikinerReader(files);

    LOG.info("maxSentences: " + maxSentences);
    LOG.info("real size: " + r.getEntities().size());

    for (final Entry<?, ?> e : r.getEntities().entrySet()) {
      LOG.info(e);
    }
    // LOG.info(r.getInput());
  }

  /**
   * Constructor for loading class.
   */
  public WikinerReader() {}

  public WikinerReader(final String[] inputPaths) throws IOException {
    initFiles(inputPaths);
  }

  @Override
  public void initFiles(final String[] initFiles) throws IOException {
    super.initFiles(initFiles);

    readData();
  }

  /**
   * Tags are:
   *
   * [I-MISC, B-LOC, I-PER, B-PER, I-LOC, B-MISC, I-ORG, B-ORG, O]
   *
   * @throws IOException
   */
  protected void readData() throws IOException {
    int sentenceCount = 0;
    StringBuilder inputLine = new StringBuilder();
    StringBuilder currentEntity = new StringBuilder();
    String currentTag = "";
    for (int i = 0; i < inputFiles.length; i++) {
      if (maxSentences > 0 && sentenceCount >= maxSentences) {
        break;
      }
      for (final String line : Compress.bzip2ToList(inputFiles[i].getAbsolutePath())) {
        if (maxSentences > 0 && sentenceCount >= maxSentences) {
          break;
        }
        final String[] taggedWords = line.split(" ");

        for (int ii = 0; ii < taggedWords.length; ii++) {
          final String[] tags = taggedWords[ii].split("\\|");

          if (tags.length > 1) {
            final String word = tags[0];
            final String nerTag = tags[2];

            inputLine.append(word).append(" ");

            if (currentTag.isEmpty()) {
              if (!nerTag.equals("O")) {
                currentTag = nerTag.split("-")[1];
                currentEntity.append(word).append(" ");
              }
            } else {

              if (nerTag.endsWith(currentTag)) {
                currentEntity.append(word).append(" ");
              } else {
                // TODO: remove magic stuff
                if (currentTag.endsWith("PER")) {
                  currentTag = EntityTypes.P;
                } else if (currentTag.endsWith("LOC")) {
                  currentTag = EntityTypes.L;
                } else if (currentTag.endsWith("ORG")) {
                  currentTag = EntityTypes.O;
                } else {
                  currentTag = ""; // unsupported tag
                }

                if (!currentTag.isEmpty()) {

                  final String e = currentEntity.toString().trim();
                  if (entities.get(e) == null) {

                    entities.put(e, currentTag);
                  } else {

                    if (!entities.get(e).equals(currentTag)) {

                      if (disambEntities.get(e) == null) {
                        disambEntities.put(e, new ArrayList<SimpleEntry<String, Integer>>());
                        disambEntities.get(e).add(new SimpleEntry<>(entities.get(e), 1));
                      }
                      {

                        boolean found = false;
                        for (final SimpleEntry<String, Integer> disambEntry : disambEntities
                            .get(e)) {
                          if (disambEntry.getKey().equals(currentTag)) {
                            disambEntry.setValue(disambEntry.getValue() + 1);
                            found = true;
                          }
                        }
                        if (!found) {
                          disambEntities.get(e).add(new SimpleEntry<>(currentTag, 1));
                        }
                      }
                    }
                  }
                }

                currentEntity = new StringBuilder();
                currentTag = "";

                if (!nerTag.equals("O")) {
                  currentTag = nerTag.split("-")[1];
                  currentEntity.append(word).append(" ");
                }
              }
            }
          }
        } // line end
        input.append(inputLine).append(System.lineSeparator());
        inputLine = new StringBuilder();
        sentenceCount++;
      }
    }
    LOG.info("sentences: " + sentenceCount);
    // removes disambs
    for (final Entry<String, List<SimpleEntry<String, Integer>>> entry : disambEntities
        .entrySet()) {
      entities.remove(entry.getKey());
    }
  }

  @Override
  public String input() {
    return input.toString().trim();
  }

  @Override
  public Map<String, String> getEntities() {
    return entities;
  }
}
