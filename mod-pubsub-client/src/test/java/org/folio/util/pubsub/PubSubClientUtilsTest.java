package org.folio.util.pubsub;

import static org.folio.util.pubsub.PubSubClientUtils.MESSAGING_CONFIG_PATH_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.folio.util.pubsub.support.DescriptorHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PubSubClientUtilsTest {

  private static final String MESSAGE_DESCRIPTOR_PATH_WITH_INVALID_FIELD = "config/with_invalid_field";
  private static final String VALID_MESSAGE_DESCRIPTOR_PATH = "config\\valid_config\\";

  @AfterEach
  public void tearDown() {
    System.clearProperty(MESSAGING_CONFIG_PATH_PROPERTY);
  }

  @Test
  public void shouldReadMessagingDescriptorByDefaultPathAndReturnFilledDescriptorHolder() throws IOException {
    DescriptorHolder descriptorHolder = PubSubClientUtils.readMessagingDescriptor();

    assertNotNull(descriptorHolder.getPublisherDescriptor());
    assertNotNull(descriptorHolder.getSubscriberDescriptor());
    assertThat(descriptorHolder.getPublisherDescriptor().getModuleId(), not(isEmptyOrNullString()));
    assertThat(descriptorHolder.getPublisherDescriptor().getEventDescriptors().size(), is(1));
    assertThat(descriptorHolder.getSubscriberDescriptor().getModuleId(), not(isEmptyOrNullString()));
    assertThat(descriptorHolder.getSubscriberDescriptor().getSubscriptionDefinitions().size(), is(1));
  }

  @Test
  public void shouldReadMessagingDescriptorByPathFromSystemProperty() throws IOException {
    System.setProperty(MESSAGING_CONFIG_PATH_PROPERTY, VALID_MESSAGE_DESCRIPTOR_PATH);

    DescriptorHolder descriptorHolder = PubSubClientUtils.readMessagingDescriptor();

    assertNotNull(descriptorHolder.getPublisherDescriptor());
    assertNotNull(descriptorHolder.getSubscriberDescriptor());
    assertThat(descriptorHolder.getPublisherDescriptor().getModuleId(), not(isEmptyOrNullString()));
    assertThat(descriptorHolder.getPublisherDescriptor().getEventDescriptors().size(), is(1));
    assertThat(descriptorHolder.getSubscriberDescriptor().getModuleId(), not(isEmptyOrNullString()));
    assertThat(descriptorHolder.getSubscriberDescriptor().getSubscriptionDefinitions().size(), is(2));
  }

  @Test
  public void shouldReadMessagingDescriptorFromClassPathWhenFileWasNotFoundByPathFromSystemProperty(
    @TempDir File descriptorParentFolder) throws IOException {

    System.setProperty(MESSAGING_CONFIG_PATH_PROPERTY, descriptorParentFolder.getAbsolutePath());

    DescriptorHolder descriptorHolder = PubSubClientUtils.readMessagingDescriptor();

    assertNotNull(descriptorHolder.getPublisherDescriptor());
    assertNotNull(descriptorHolder.getSubscriberDescriptor());
    assertThat(descriptorHolder.getPublisherDescriptor().getModuleId(), not(isEmptyOrNullString()));
    assertThat(descriptorHolder.getPublisherDescriptor().getEventDescriptors().size(), is(1));
    assertThat(descriptorHolder.getSubscriberDescriptor().getModuleId(), not(isEmptyOrNullString()));
    assertThat(descriptorHolder.getSubscriberDescriptor().getSubscriptionDefinitions().size(), is(1));
  }

  @Test
  public void shouldThrowExceptionWhenReadInvalidMessagingDescriptor() {
    System.setProperty(MESSAGING_CONFIG_PATH_PROPERTY, MESSAGE_DESCRIPTOR_PATH_WITH_INVALID_FIELD);
    assertThrows(IllegalArgumentException.class, PubSubClientUtils::readMessagingDescriptor);
  }

  @Test
  public void shouldBuildCorrectModuleId() {
    assertThat(PubSubClientUtils.getModuleId(), matchesPattern("mod-pubsub-[0-9]+\\.[0-9]+\\.[0-9]+"));
  }
}
