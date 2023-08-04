package org.folio.rest.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//@Setter
//@Getter
//@AllArgsConstructor
public class ExpiryAwareToken {
  private String token;
  private long maxAge;
  private OkapiConnectionParams okapiParams;

  // TODO: remove
  public ExpiryAwareToken(String token, long maxAge, OkapiConnectionParams okapiParams) {
    this.token = token;
    this.maxAge = maxAge;
    this.okapiParams = okapiParams;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(long maxAge) {
    this.maxAge = maxAge;
  }

  public OkapiConnectionParams getOkapiParams() {
    return okapiParams;
  }

  public void setOkapiParams(OkapiConnectionParams okapiParams) {
    this.okapiParams = okapiParams;
  }
}
