package com.tsystems.dfmg.studies.library.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

public class SecurityInterceptor extends EndpointInterceptorAdapter {

    @Autowired
    private UserContextConfigurer userContextConfigurer;

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        SaajSoapMessage message = (SaajSoapMessage) messageContext.getRequest();
        String[] header = message.getSaajMessage().getMimeHeaders().getHeader("X-Auth-Token");
        if (header != null && header.length > 0) {
            String token = header[0];
            userContextConfigurer.setUser(authenticate(token));
        } else {
            throw new IllegalArgumentException("auth token missing");
        }
        return true;
    }

    private User authenticate(String token) {
        if (!token.equalsIgnoreCase("valid-token")) {
            throw new IllegalArgumentException("invalid auth token");
        }
        User user = new User("valid-user", "READER", "WRITER");
        return user;
    }
}
