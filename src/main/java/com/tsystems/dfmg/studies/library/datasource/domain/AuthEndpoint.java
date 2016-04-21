package com.tsystems.dfmg.studies.library.datasource.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AuthEndpoint {

    private static final String NAMESPACE_URI = "http://tsystems.com/dfmg/studies/library/datasource/domain";

    @Autowired
    private UserBisService userBisService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "readUserBisRequest")
    @ResponsePayload
    public ReadUserBisResponse read(@RequestPayload ReadUserBisRequest request) {
        ReadUserBisResponse response = new ReadUserBisResponse();
        response.setUserBis(userBisService.get(request.getLogin(), request.getPassword()));
        return response;
    }

}
