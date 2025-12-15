package org.folio.rest.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class SimpleConfigurationReaderTest {

  @Test
  void shouldReadValueFromSystemProperty() {
    String expectedValue = "validProperty";
    System.setProperty("test.props", expectedValue);
    String actualValue = SimpleConfigurationReader.getValue("validProperty", "test.props", null);
    Assert.assertEquals(expectedValue, actualValue);
  }

  @Test
  void shouldReturnSpringValueIfNoSysProperty() {
    String expectedValue = "validProperty";
    System.setProperty("test.props", expectedValue);
    String actualValue = SimpleConfigurationReader.getValue(null, "test.props", null);
    Assert.assertEquals(expectedValue, actualValue);
  }

  @Test
  void shouldReturnDefaultValueIfSysAndSpringPropertiesAreEmpty() {
    String defaultValue = "default";
    String actualValue = SimpleConfigurationReader.getValue(null, "test2.props", "default");
    Assert.assertEquals(defaultValue, actualValue);
  }
}

