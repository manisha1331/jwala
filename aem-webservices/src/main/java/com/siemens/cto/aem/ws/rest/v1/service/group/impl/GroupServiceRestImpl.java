package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;

public class GroupServiceRestImpl implements GroupServiceRest {

    private final Logger logger;
    private final GroupService groupService;

    public GroupServiceRestImpl(final GroupService theGroupService) {
        groupService = theGroupService;
        logger = LoggerFactory.getLogger(GroupServiceRestImpl.class);
    }

    @Override
    public Response getGroups(final PaginationParamProvider paginationParamProvider) {
        final PaginationParameter pagination = paginationParamProvider.getPaginationParameter();
        logger.debug("Get Groups requested with pagination: {}", pagination);
        return ResponseBuilder.ok(groupService.getGroups(pagination));
    }

    @Override
    public Response getGroup(final Identifier<Group> aGroupId) {
        logger.debug("Get Group requested: {}", aGroupId);
        return ResponseBuilder.ok(groupService.getGroup(aGroupId));
    }

//    @Override
    public Response createGroup(final String aNewGroupName) {
        logger.debug("Create Group requested: {}", aNewGroupName);
        //TODO We must put the user originating the request in here from however we get it
        return ResponseBuilder.created(groupService.createGroup(aNewGroupName,
                                                                new User("hardCodedUser")));
    }
}
