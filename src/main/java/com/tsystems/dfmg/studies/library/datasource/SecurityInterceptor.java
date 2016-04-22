package com.tsystems.dfmg.studies.library.datasource;

import com.tsystems.dfmg.studies.library.datasource.domain.LibraryEndpoint;
import com.tsystems.dfmg.studies.library.datasource.domain.User;
import com.tsystems.dfmg.studies.library.datasource.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import java.util.Iterator;

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

            String nativeId = null;

            if (message.getEnvelope() != null && message.getEnvelope().getHeader() != null) {
                Iterator<SoapHeaderElement> soapHeaderElementIterator = message.getEnvelope().getHeader().examineAllHeaderElements();
                while (soapHeaderElementIterator.hasNext()) {
                    SoapHeaderElement next = soapHeaderElementIterator.next();
                    if (next.getName().getLocalPart().equalsIgnoreCase("Native-Id")) {
                        nativeId = next.getText();
                        break;
                    }
                }
            }

            if (nativeId == null) {
                String[] header = message.getSaajMessage().getMimeHeaders().getHeader("Native-Id");
                if (header != null && header.length > 0) {
                    nativeId = header[0];
                }
            }

            if (nativeId == null) {
                throw new IllegalArgumentException("auth token missing");
            } else {
                userContextConfigurer.setUser(authenticate(nativeId));
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
