package com.siemens.cto.aem.ws.rest.v1.response;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;

/**
 * Created by z003bpej on 2/21/14.
 */
public class ApplicationResponse {

    private final MessageResponseStatus responseStatus;
    private final Object applicationResponseContent;

    public ApplicationResponse(final MessageResponseStatus theMessageResponseStatus,
                               final Object theContent) {
        responseStatus = theMessageResponseStatus;
        applicationResponseContent = theContent;
    }

    public String getMsgCode() {
        return responseStatus.getMessageCode();
    }

    public String getMessage() {
        return responseStatus.getMessage();
    }

    public Object getApplicationResponseContent() {
        return applicationResponseContent;
    }

}
