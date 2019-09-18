package org.folio.services.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.folio.dao.EventDescriptorDao;
import org.folio.dao.MessagingModuleDao;
import org.folio.dao.impl.MessagingModuleFilter;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.EventDescriptor;
import org.folio.rest.jaxrs.model.MessagingModule;
import org.folio.rest.jaxrs.model.MessagingModule.ModuleRole;
import org.folio.rest.jaxrs.model.MessagingModuleCollection;
import org.folio.rest.jaxrs.model.PublisherDescriptor;
import org.folio.services.MessagingModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.folio.rest.jaxrs.model.MessagingModule.ModuleRole.PUBLISHER;

/**
 * Implementation for Messaging Module service
 *
 * @see org.folio.services.MessagingModuleService
 */
@Component
public class MessagingModuleServiceImpl implements MessagingModuleService {

  private MessagingModuleDao messagingModuleDao;
  private EventDescriptorDao eventDescriptorDao;

  public MessagingModuleServiceImpl(@Autowired MessagingModuleDao messagingModuleDao,
                                    @Autowired EventDescriptorDao eventDescriptorDao) {
    this.messagingModuleDao = messagingModuleDao;
    this.eventDescriptorDao = eventDescriptorDao;
  }

  @Override
  public Future<Errors> validatePublisherDescriptor(PublisherDescriptor publisherDescriptor) {
    Errors errors = new Errors().withTotalRecords(0);
    List<EventDescriptor> eventDescriptors = publisherDescriptor.getEventDescriptors();
    List<String> eventTypes = eventDescriptors.stream().map(EventDescriptor::getEventType).collect(Collectors.toList());

    return eventDescriptorDao.getByEventTypes(eventTypes)
      .map(existingDescriptorList -> {
        Map<String, EventDescriptor> descriptorsMap = existingDescriptorList.stream()
          .collect(Collectors.toMap(EventDescriptor::getEventType, descriptor -> descriptor));
        for (EventDescriptor eventDescriptor : eventDescriptors) {
          EventDescriptor existingDescriptor = descriptorsMap.get(eventDescriptor.getEventType());
          compareEventDescriptors(eventDescriptor, existingDescriptor, errors);
        }
        return errors.withTotalRecords(errors.getErrors().size());
      });
  }

  private void compareEventDescriptors(EventDescriptor eventDescriptor, EventDescriptor existingDescriptor, Errors errors) {
    if (existingDescriptor == null) {
      errors.getErrors().add(new Error().withMessage(String.format("Event type '%s' is not exists", eventDescriptor.getEventType())));
    } else {
      JsonObject descriptorJson = JsonObject.mapFrom(eventDescriptor);
      JsonObject existingDescriptorJson = JsonObject.mapFrom(existingDescriptor);
      if (!descriptorJson.equals(existingDescriptorJson)) {
        String descriptorContent = JsonObject.mapFrom(existingDescriptor).encodePrettily();
        String message = String.format("Descriptor of event type '%s' does not match to existing descriptor. To declare publisher should use follow descriptor: %s",
          eventDescriptor.getEventType(), descriptorContent);
        errors.getErrors().add(new Error().withMessage(message));
      }
    }
    errors.setTotalRecords(errors.getErrors().size());
  }

  @Override
  public Future<Boolean> savePublisher(PublisherDescriptor publisherDescriptor, String tenantId) {
    List<MessagingModule> messagingModules = createMessagingModules(publisherDescriptor.getEventDescriptors(), PUBLISHER, tenantId);
    return messagingModuleDao.save(publisherDescriptor.getModuleName(), messagingModules)
      .map(savedModules -> true);
  }

  /**
   * Creates Messaging Modules by event descriptors
   * @param eventDescriptorList Event Descriptor list
   * @param moduleRole MessagingModule role
   * @param tenantId tenant id
   * @return Messaging Modules list
   */
  private List<MessagingModule> createMessagingModules(List<EventDescriptor> eventDescriptorList, ModuleRole moduleRole, String tenantId) {
    return eventDescriptorList.stream()
      .map(eventDescriptor -> createMessagingModule(eventDescriptor, moduleRole, tenantId))
      .collect(Collectors.toList());
  }

  /**
   * Creates Messaging Module by eventDescriptor
   *
   * @param eventDescriptor event descriptor
   * @param moduleRole module role
   * @param tenantId tenant id
   * @return MessagingModule
   */
  private MessagingModule createMessagingModule(EventDescriptor eventDescriptor, ModuleRole moduleRole, String tenantId) {
    return new MessagingModule()
      .withId(UUID.randomUUID().toString())
      .withTenantId(tenantId)
      .withEventType(eventDescriptor.getEventType())
      .withModuleRole(moduleRole)
      .withApplied(true);
  }

  @Override
  public Future<Boolean> deleteByModuleNameAndFilter(String moduleName, MessagingModuleFilter filter) {
    return messagingModuleDao.deleteByModuleNameAndFilter(moduleName, filter);
  }

  @Override
  public Future<MessagingModuleCollection> getByEventTypeAndRole(String eventType, ModuleRole role, String tenantId) {
    MessagingModuleFilter messagingModuleFilter = new MessagingModuleFilter();
    messagingModuleFilter.byEventType(eventType);
    messagingModuleFilter.byModuleRole(role);
    messagingModuleFilter.byTenantId(tenantId);
    return messagingModuleDao.get(messagingModuleFilter)
      .map(messagingModules -> new MessagingModuleCollection()
        .withMessagingModules(messagingModules)
        .withTotalRecords(messagingModules.size()));
  }
}