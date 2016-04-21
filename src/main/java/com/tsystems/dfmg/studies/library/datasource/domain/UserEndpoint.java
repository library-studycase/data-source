package com.tsystems.dfmg.studies.library.datasource.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;

@Endpoint
public class UserEndpoint {

    private static final String NAMESPACE_URI = "http://tsystems.com/dfmg/studies/library/datasource/domain";

    @Autowired
    private UserRepository repository;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "readUserRequest")
    @ResponsePayload
    public ReadUserResponse read(@RequestPayload ReadUserRequest request) {
        ReadUserResponse response = new ReadUserResponse();
        response.setBook(repository.get(request.getId()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createUserRequest")
    @ResponsePayload
    public CreateUserResponse create(@RequestPayload CreateUserRequest request) {
        CreateUserResponse response = new CreateUserResponse();
        response.setUser(repository.add(request.getLogin(), request.getPassword(), request.getRoles()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserRequest")
    @ResponsePayload
    public UpdateUserResponse update(@RequestPayload UpdateUserRequest request) {
        UpdateUserResponse response = new UpdateUserResponse();
        response.setBook(repository.update(request.getId(), request.getLogin(), request.getPassword(), request.getRoles()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteUserRequest")
    @ResponsePayload
    public DeleteUserResponse delete(@RequestPayload DeleteUserRequest request) {
        repository.remove(request.getId());
        return new DeleteUserResponse();
    }

}
