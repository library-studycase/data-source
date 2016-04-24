package com.tsystems.dfmg.studies.library.datasource.domain;

import com.tsystems.dfmg.studies.library.datasource.UserContext;
import com.tsystems.dfmg.studies.library.datasource.UserContextBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class LibraryEndpoint {

    private static final String NAMESPACE_URI = "http://tsystems.com/dfmg/studies/library/datasource/domain";

    private final BookRepository repository;

    @Autowired
    private UserContext userContext;

    @Autowired
    public LibraryEndpoint(BookRepository repository) {
        this.repository = repository;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createRequest")
    @ResponsePayload
    public CreateResponse create(@RequestPayload CreateRequest request) {
        if (!userContext.getUser().getRoles().contains("ADDER")) {
            throw new SecurityException("you do not have permission to create books");
        }
        CreateResponse response = new CreateResponse();
        response.setBook(repository.add(request.getTitle(), request.getDescription(), request.getAuthor(), request.getPages()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "readDetailedRequest")
    @ResponsePayload
    public ReadDetailedResponse readDetailed(@RequestPayload ReadDetailedRequest request) {
        if (!userContext.getUser().getRoles().contains("READER")) {
            throw new SecurityException("you do not have permission to read books");
        }
        ReadDetailedResponse response = new ReadDetailedResponse();
        response.setBook(repository.get(request.getId()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "readListPageRequest")
    @ResponsePayload
    public ReadListPageResponse readListPage(@RequestPayload ReadListPageRequest request) {
        if (!userContext.getUser().getRoles().contains("READER")) {
            throw new SecurityException("you do not have permission to read books");
        }
        ReadListPageResponse response = new ReadListPageResponse();
        response.setPage(repository.getPage(request.getOffset(), request.getLimit(), request.getTitleFilter()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateRequest")
    @ResponsePayload
    public UpdateResponse update(@RequestPayload UpdateRequest request) {
        if (!userContext.getUser().getRoles().contains("MODIFIER")) {
            throw new SecurityException("you do not have permission to update books");
        }
        UpdateResponse response = new UpdateResponse();
        response.setBook(repository.update(request.getId(), request.getTitle(), request.getDescription(),
                request.getAuthor(), request.getPages()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteRequest")
    @ResponsePayload
    public DeleteResponse delete(@RequestPayload DeleteRequest request) {
        if (!userContext.getUser().getRoles().contains("MODIFIER")) {
            throw new SecurityException("you do not have permission to delete books");
        }
        repository.remove(request.getId());
        return new DeleteResponse();
    }
}
