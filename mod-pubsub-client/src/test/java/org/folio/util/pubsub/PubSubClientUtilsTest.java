package org.folio.util.pubsub;

import org.folio.util.pubsub.support.DescriptorHolder;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.folio.util.pubsub.PubSubClientUtils.MESSAGING_CONFIG_PATH_PROPERTY;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;

public class PubSubClientUtilsTest {

  private static final String MESSAGE_DESCRIPTOR_PATH_WITH_INVALID_FIELD = "config/with_invalid_field";
  private static final String VALID_MESSAGE_DESCRIPTOR_PATH = "config\\valid_config\\";

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @After
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
  public void shouldReadMessagingDescriptorFromClassPathWhenFileWasNotFoundByPathFromSystemProperty() throws IOException {
    File descriptorParentFolder = temporaryFolder.newFolder();
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
  public void shouldThrowExceptionWhenReadInvalidMessagingDescriptor() throws IOException {
    exceptionRule.expect(IllegalArgumentException.class);
    System.setProperty(MESSAGING_CONFIG_PATH_PROPERTY, MESSAGE_DESCRIPTOR_PATH_WITH_INVALID_FIELD);

    PubSubClientUtils.readMessagingDescriptor();
  }

  @Test
  public void shouldBuildCorrectModuleId() {
    // Passing parent pom.xml path to get the correct version because mod-pubsub-client is a submodule.
    // Typically, modules will call PubSubClientUtils.getModuleId() to use their own pom.xml
    assertThat(PubSubClientUtils.getModuleId("../pom.xml"), startsWith("mod-pubsub"));

    // No version here because mod-pubsub-client's own pom.xml doesn't specify a version.
    assertThat(PubSubClientUtils.getModuleId(), is("mod-pubsub"));

    assertThat(PubSubClientUtils.getModuleId("not-a-pom.xml"), is("mod-pubsub"));
  }

}
