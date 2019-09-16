package org.aksw.fox.tools;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.Fox;
import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;

/**
 * The tool version it set in the maven pom.xml file.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
abstract public class ATool implements ITool {

  protected final CfgManager cfgManager = new CfgManager(Fox.cfgFolder);

  protected static Properties versions = new Properties();
  static {
    try {
      versions.load(ITool.class.getResourceAsStream("/versions.properties"));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  protected CountDownLatch cdl = null;

  public static String getToolVersion(final String name) {
    String version = versions.getProperty(name, "n/a");
    if (version == null) {
      LOG.warn("Version not found for: " + name);
      version = "n/a";
    }
    return version;
  }

  @Override
  public String getToolVersion() {
    return ATool.getToolVersion(this.getClass().getName());
  }

  @Override
  public String getToolName() {
    return getClass().getName();
  }

  @Override
  public abstract void run();

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    this.cdl = cdl;
  }
}
