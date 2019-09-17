package org.aksw.fox.tools.linking.pt;

import java.io.IOException;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.fox.tools.linking.common.Agdistis;

public class AgdistisDirectPT extends Agdistis {

  final String file = "agdistisPT.properties";
  NEDAlgo_HITS agdistis = null;

  public AgdistisDirectPT() {
    try {
      agdistis = new NEDAlgo_HITS(file);
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected String send(final String text) throws Exception {
    return standardAG(text, agdistis);
  }

}