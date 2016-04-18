package com.tsystems.dfmg.studies.library.datasource;

import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadRootSmartSoapEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.SoapActionSmartEndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;

@Configuration
@EnableWs
public class WebServiceConfiguration extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/soap/*");
    }

    @Bean(name = "library")
    public DefaultWsdl11Definition libraryWsdlDefinition(XsdSchema librarySchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("LibraryPort");
        wsdl11Definition.setLocationUri("/soap");
        wsdl11Definition.setTargetNamespace("http://tsystems.com/dfmg/studies/library/datasource/domain");
        wsdl11Definition.setSchema(librarySchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema librarySchema() {
        return new SimpleXsdSchema(new ClassPathResource("domain/library.xsd"));
    }

    @Bean
    public EndpointInterceptor securityInterceptor() {
        return new SecurityInterceptor();
    }

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(securityInterceptor());
    }
}
