package org.folio.kafka;

@FunctionalInterface
public interface BackPressureGauge<G, L, T> {
  /**
   * Returns true if a threshold is exceeded, otherwise false
   *
   * @param globalLoad
   * @param localLoad
   * @param threshold
   * @return true if a threshold is exceeded, otherwise false
   */
  boolean isThresholdExceeded(G globalLoad, L localLoad, T threshold);
}
