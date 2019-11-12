package org.folio.util.pubsub;

/**
 * Signals that some errors were occurred during module registration in PubSub.
 */
public class ModuleRegistrationException extends Exception {

  public ModuleRegistrationException(String s) {
    super(s);
  }
}
