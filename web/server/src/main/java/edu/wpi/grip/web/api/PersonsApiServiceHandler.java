package edu.wpi.grip.web.api;


import edu.wpi.grip.web.swagger.api.NotFoundException;
import edu.wpi.grip.web.swagger.api.PersonsApiService;
import edu.wpi.grip.web.swagger.model.Person;

import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class PersonsApiServiceHandler implements PersonsApiService {

  @Override
  public Response personsGet(Double size, SecurityContext securityContext) throws NotFoundException {
    Person person = new Person();
    person.setName("Jonathan Leitschuh");
    person.setSingle(false);
    return Response.ok(Collections.singletonList(person)).build();
  }
}
