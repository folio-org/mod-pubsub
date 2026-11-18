package org.folio.rest.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class SimpleConfigurationReaderTest {

  @Test
  void shouldReadValueFromSystemProperty() {
    String expectedValue = "validProperty";
    System.setProperty("test.props", expectedValue);
    String actualValue = SimpleConfigurationReader.getValue("validProperty", "test.props", null);
    assertThat(actualValue, is(expectedValue));
  }

  @Test
  void shouldReturnSpringValueIfNoSysProperty() {
    String expectedValue = "validProperty";
    System.setProperty("test.props", expectedValue);
    String actualValue = SimpleConfigurationReader.getValue(null, "test.props", null);
    assertThat(actualValue, is(expectedValue));
  }

  @Test
  void shouldReturnDefaultValueIfSysAndSpringPropertiesAreEmpty() {
    String defaultValue = "default";
    String actualValue = SimpleConfigurationReader.getValue(null, "test2.props", "default");
    assertThat(actualValue, is(defaultValue));
  }
}

