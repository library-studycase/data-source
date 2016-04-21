package com.tsystems.dfmg.studies.library.datasource;

import com.tsystems.dfmg.studies.library.datasource.domain.LibraryEndpoint;
import com.tsystems.dfmg.studies.library.datasource.domain.User;
import com.tsystems.dfmg.studies.library.datasource.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

public class SecurityInterceptor extends EndpointInterceptorAdapter {

    @Autowired
    private UserContextConfigurer userContextConfigurer;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (endpoint instanceof MethodEndpoint) {
            MethodEndpoint methodEndpoint = (MethodEndpoint) endpoint;
            if (methodEndpoint.getMethod().getDeclaringClass() != LibraryEndpoint.class) {
                return true;
            }

            SaajSoapMessage message = (SaajSoapMessage) messageContext.getRequest();
            String[] header = message.getSaajMessage().getMimeHeaders().getHeader("Native-Id");
            if (header != null && header.length > 0) {
                String nativeId = header[0];
                userContextConfigurer.setUser(authenticate(nativeId));
            } else {
                throw new IllegalArgumentException("auth token missing");
            }
        }
        return true;
    }

    private User authenticate(String nativeId) {
        User User = userRepository.get(Integer.parseInt(nativeId));
        if (User == null) {
            throw new IllegalArgumentException("user is not authorized");
        }
        return User;
    }

}
