package org.folio.util.pubsub.support;

import static java.lang.String.format;

import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.folio.rest.tools.utils.ModuleName;

public class PomUtils {
  public static String getModuleVersion() {
    return getVersionRecursively("pom.xml");
  }

  private static String getVersionRecursively(String pomPath) {
    try {
      Model model = new MavenXpp3Reader().read(new FileReader(pomPath));
      String version = model.getVersion();
      if (version == null || version.isEmpty()) {
        Parent parent = model.getParent();
        return getVersionRecursively(parent.getRelativePath());
      }
      else {
        return version;
      }
    }
    catch (IOException | XmlPullParserException e) {
      throw new RuntimeException("Failed to parse pom.xml");
    }
  }
}
