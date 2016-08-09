package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JvmServiceRestImpl implements JvmServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceRestImpl.class);

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;
    private final ResourceService resourceService;
    private static JvmServiceRestImpl instance;

    @Context
    private MessageContext context;

    public JvmServiceRestImpl(final JvmService theJvmService,
                              final JvmControlService theJvmControlService,
                              final ResourceService theResourceService) {
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
        resourceService = theResourceService;
    }

    @Override
    public Response getJvms() {
        LOGGER.debug("Get JVMs requested");
        final List<Jvm> jvms = new ArrayList<Jvm>();
        for (Jvm jvm : jvmService.getJvms()) {
            // TODO why are we sending the decrypted password back to the browser in the response??
            jvms.add(jvm.toDecrypted());
        }
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getJvm(final Identifier<Jvm> aJvmId) {
        LOGGER.debug("Get JVM requested: {}", aJvmId);
        Jvm aJvm = jvmService.getJvm(aJvmId).toDecrypted();
        return ResponseBuilder.ok(aJvm);
    }

    @Override
    public Response createJvm(final JsonCreateJvm aJvmToCreate, final AuthenticatedUser aUser) {
        final User user = aUser.getUser();
        LOGGER.info("Create JVM requested: {} by user {}", aJvmToCreate, user.getId());
        Jvm jvm = jvmService.createJvm(aJvmToCreate.toCreateAndAddRequest(), user);
        return ResponseBuilder.created(jvm);
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate, final AuthenticatedUser aUser) {
        LOGGER.info("Update JVM requested: {} by user {}", aJvmToUpdate, aUser.getUser().getId());
        return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmRequest(), aUser.getUser()));
    }

    @Override
    public Response removeJvm(final Identifier<Jvm> aJvmId, final AuthenticatedUser user) {
        LOGGER.info("Delete JVM requested: {} by user {}", aJvmId, user.getUser().getId());
        jvmService.removeJvm(aJvmId, user.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId, final JsonControlJvm aJvmToControl, final AuthenticatedUser aUser) {
        LOGGER.debug("Control JVM requested: {} {} by user {}", aJvmId, aJvmToControl, aUser.getUser().getId());
        final CommandOutput commandOutput = jvmControlService.controlJvm(new ControlJvmRequest(aJvmId, aJvmToControl.toControlOperation()), aUser.getUser());
        if (commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput);
        } else {
            final String standardError = commandOutput.getStandardError();
            final String standardOutput = commandOutput.getStandardOutput();
            String errMessage = standardError != null && !standardError.isEmpty() ? standardError : standardOutput;
            LOGGER.error("Control JVM unsuccessful: " + errMessage);
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
        }
    }

    @Override
    public Response generateAndDeployJvm(final String jvmName, final AuthenticatedUser user) {
        LOGGER.info("Generate and deploy JVM {} by user {}", jvmName, user.getUser().getId());
        return ResponseBuilder.ok(jvmService.generateAndDeployJvm(jvmName, user.getUser()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    @Override
    public Response generateAndDeployFile(final String jvmName, final String fileName, AuthenticatedUser user) {
        LOGGER.info("Generate and deploy file {} to JVM {} by user {}", fileName, jvmName, user.getUser().getId());
        return ResponseBuilder.ok(jvmService.generateAndDeployFile(jvmName, fileName, user.getUser()));
    }

    protected void createConfigFile(String path, String configFileName, String templateContent) throws IOException {
        File configFile = new File(path + configFileName);
        if (configFileName.endsWith(".bat")) {
            templateContent = templateContent.replaceAll("\n", "\r\n");
        }
        FileUtils.writeStringToFile(configFile, templateContent);
    }

    @Override
    public Response diagnoseJvm(Identifier<Jvm> aJvmId) {
        LOGGER.info("Diagnose JVM {}", aJvmId);
        String diagnosis = jvmService.performDiagnosis(aJvmId);

        return Response.ok(diagnosis).build();
    }

    @Override
    public Response getResourceNames(final String jvmName) {
        LOGGER.debug("Get resource names {}", jvmName);
        return ResponseBuilder.ok(jvmService.getResourceTemplateNames(jvmName));
    }

    @Override
    public Response getResourceTemplate(final String jvmName, final String resourceTemplateName,
                                        final boolean tokensReplaced) {
        LOGGER.debug("Get resource template {} for JVM {} : tokens replaced={}", resourceTemplateName, jvmName, tokensReplaced);
        return ResponseBuilder.ok(jvmService.getResourceTemplate(jvmName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response updateResourceTemplate(final String jvmName, final String resourceTemplateName,
                                           final String content) {
        LOGGER.info("Update the resource template {} for JVM {}", resourceTemplateName, jvmName);
        LOGGER.debug(content);
        try {
            return ResponseBuilder.ok(jvmService.updateResourceTemplate(jvmName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.debug("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response previewResourceTemplate(final String jvmName, final String groupName, final String template) {
        LOGGER.debug("Preview resource template for JVM {} in group {} with content {}", jvmName, groupName, template);
        try {
            return ResponseBuilder.ok(jvmService.previewResourceTemplate(jvmName, groupName, template));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing resource.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }

    public static JvmServiceRest get() {
        return instance;
    }
}
