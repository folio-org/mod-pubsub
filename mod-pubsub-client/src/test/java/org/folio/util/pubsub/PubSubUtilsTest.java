package org.folio.util.pubsub;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.folio.util.pubsub.PubSubUtils.MESSAGING_CONFIG_PATH_PROPERTY;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

@RunWith(JMockit.class)
public class PubSubUtilsTest {

  private static final String MESSAGE_DESCRIPTOR_PATH_WITH_INVALID_FIELD = "config/with_invalid_field";
  private static final String VALID_MESSAGE_DESCRIPTOR_PATH = "config\\valid_config\\";

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void shouldReadMessagingDescriptorByDefaultPathAndReturnFilledDescriptorHolder() throws IOException {
    DescriptorHolder descriptorHolder = PubSubUtils.readMessagingDescriptor();

    Assert.assertNotNull(descriptorHolder.getPublisherDescriptor());
    Assert.assertNotNull(descriptorHolder.getSubscriberDescriptor());
    Assert.assertThat(descriptorHolder.getPublisherDescriptor().getModuleName(), not(isEmptyOrNullString()));
    Assert.assertThat(descriptorHolder.getPublisherDescriptor().getEventDescriptors().size(), is(1));
    Assert.assertThat(descriptorHolder.getSubscriberDescriptor().getModuleName(), not(isEmptyOrNullString()));
    Assert.assertThat(descriptorHolder.getSubscriberDescriptor().getSubscriptionDefinitions().size(), is(1));
  }

  @Test
  public void shouldReadMessagingDescriptorByPathFromSystemProperty() throws IOException {
    mockSystemProperty(MESSAGING_CONFIG_PATH_PROPERTY, VALID_MESSAGE_DESCRIPTOR_PATH);

    DescriptorHolder descriptorHolder = PubSubUtils.readMessagingDescriptor();

    Assert.assertNotNull(descriptorHolder.getPublisherDescriptor());
    Assert.assertNotNull(descriptorHolder.getSubscriberDescriptor());
    Assert.assertThat(descriptorHolder.getPublisherDescriptor().getModuleName(), not(isEmptyOrNullString()));
    Assert.assertThat(descriptorHolder.getPublisherDescriptor().getEventDescriptors().size(), is(1));
    Assert.assertThat(descriptorHolder.getSubscriberDescriptor().getModuleName(), not(isEmptyOrNullString()));
    Assert.assertThat(descriptorHolder.getSubscriberDescriptor().getSubscriptionDefinitions().size(), is(2));
  }

  @Test
  public void shouldReadMessagingDescriptorFromClassPathWhenWasNotFoundByPathFromSystemProperty() throws IOException {
    File descriptorParentFolder = temporaryFolder.newFolder();
    mockSystemProperty(MESSAGING_CONFIG_PATH_PROPERTY, descriptorParentFolder.getAbsolutePath());

    DescriptorHolder descriptorHolder = PubSubUtils.readMessagingDescriptor();

    Assert.assertNotNull(descriptorHolder.getPublisherDescriptor());
    Assert.assertNotNull(descriptorHolder.getSubscriberDescriptor());
    Assert.assertThat(descriptorHolder.getPublisherDescriptor().getModuleName(), not(isEmptyOrNullString()));
    Assert.assertThat(descriptorHolder.getPublisherDescriptor().getEventDescriptors().size(), is(1));
    Assert.assertThat(descriptorHolder.getSubscriberDescriptor().getModuleName(), not(isEmptyOrNullString()));
    Assert.assertThat(descriptorHolder.getSubscriberDescriptor().getSubscriptionDefinitions().size(), is(1));
  }

  @Test
  public void shouldThrowExceptionWhenReadInvalidMessagingDescriptor() throws IOException {
    exceptionRule.expect(IllegalArgumentException.class);
    mockSystemProperty(MESSAGING_CONFIG_PATH_PROPERTY, MESSAGE_DESCRIPTOR_PATH_WITH_INVALID_FIELD);

    PubSubUtils.readMessagingDescriptor();
  }

  private void mockSystemProperty(String propertyName, String propertyValue) {
    new MockUp<System>() {

      @Mock
      public String getenv(String name) {
        if (propertyName.equals(name)) {
          return propertyValue;
        }
        return null;
      }
    };
  }
}
