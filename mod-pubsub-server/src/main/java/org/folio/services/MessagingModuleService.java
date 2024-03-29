package org.folio.services;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.MessagingModuleCollection;
import org.folio.rest.jaxrs.model.PublisherDescriptor;
import org.folio.rest.jaxrs.model.SubscriberDescriptor;
import org.folio.rest.util.MessagingModuleFilter;
import org.folio.rest.util.OkapiConnectionParams;

/**
 * Messaging Module service
 */
public interface MessagingModuleService {

  /**
   * Validates PublisherDescriptor
   *
   * @param publisherDescriptor publisher descriptor
   * @return future with validation result
   */
  Future<Errors> validatePublisherDescriptor(PublisherDescriptor publisherDescriptor);

  /**
   * Creates publisher of event types specified in publisherDescriptor
   * Deletes previous info for publisher with specified module name in descriptor and by tenant id,
   * before creating a new publisher
   *
   * @param publisherDescriptor publisher descriptor
   * @param tenantId            tenant id
   * @return succeeded future if created publisher, failed future otherwise
   */
  Future<Void> savePublisher(PublisherDescriptor publisherDescriptor, String tenantId);

  /**
   * Checks whether all EventTypes specified in SubscriberDescriptor exist,
   * creates temporary EventDescriptor to allow Subscriber to be registered before the Publisher
   *
   * @param subscriberDescriptor subscriber descriptor
   * @return succeeded future if created missing event types, failed future otherwise
   */
  Future<Void> createMissingEventTypes(SubscriberDescriptor subscriberDescriptor);

  /**
   * Creates subscriber of event types specified in subscriberDescriptor
   * Deletes previous info for subscriber with specified module name in descriptor and by tenant id,
   * before creating a new subscriber
   *
   * @param subscriberDescriptor subscriber descriptor
   * @param params               Okapi connection params
   * @return succeeded future if saved subscriber, failed future otherwise
   */
  Future<Void> saveSubscriber(SubscriberDescriptor subscriberDescriptor, OkapiConnectionParams params);

  /**
   * Deletes module matching filter criteria
   *
   * @param filter MessagingModule filter
   */
  Future<Void> delete(MessagingModuleFilter filter);

  /**
   * Searches for MessagingModules matching filter criteria
   *
   * @param filter MessagingModule filter
   * @return future with MessagingModule collection
   */
  Future<MessagingModuleCollection> get(MessagingModuleFilter filter);
}
