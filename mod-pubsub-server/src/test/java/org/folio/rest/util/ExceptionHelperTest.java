package org.folio.rest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class ExceptionHelperTest {

  @Test
  public void shouldReturnBadRequestResponse() {
    Response response = ExceptionHelper.mapExceptionToResponse(new BadRequestException("Bad request message"));
    assertNotNull(response);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    assertEquals(MediaType.TEXT_PLAIN, response.getMediaType().toString());
    assertEquals("Bad request message", response.getEntity().toString());
  }

  @Test
  public void shouldReturnInternalServerErrorResponse() {
    Response response = ExceptionHelper.mapExceptionToResponse(new InternalServerErrorException("Internal server error message"));
    assertNotNull(response);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    assertEquals(MediaType.TEXT_PLAIN, response.getMediaType().toString());
    assertTrue(response.getEntity().toString().contains("Internal Server Error"));
  }
}
