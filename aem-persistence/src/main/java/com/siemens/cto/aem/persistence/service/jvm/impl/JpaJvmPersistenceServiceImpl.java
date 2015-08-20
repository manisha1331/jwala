package com.siemens.cto.aem.persistence.service.jvm.impl;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UploadServerXmlTemplateCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmCrudService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;

import java.util.ArrayList;
import java.util.List;

public class JpaJvmPersistenceServiceImpl implements JvmPersistenceService {

    private final JvmCrudService jvmCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;

    public JpaJvmPersistenceServiceImpl(final JvmCrudService theJvmCrudService,
                                        final GroupJvmRelationshipService theGroupJvmRelationshipService) {
        jvmCrudService = theJvmCrudService;
        groupJvmRelationshipService = theGroupJvmRelationshipService;
    }

    @Override
    public Jvm createJvm(final Event<CreateJvmCommand> aJvmToCreate) {
        final JpaJvm jpaJvm = jvmCrudService.createJvm(aJvmToCreate);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate) {
        final JpaJvm jpaJvm = jvmCrudService.updateJvm(aJvmToUpdate);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {
        final JpaJvm jpaJvm = jvmCrudService.getJvm(aJvmId);
        return jvmFrom(jpaJvm);
    }

    @Override
    public List<Jvm> getJvms() {
        return jvmsFrom(jvmCrudService.getJvms());
    }

    @Override
    public List<Jvm> findJvms(final String aName) {
        return jvmsFrom(jvmCrudService.findJvms(aName));
    }

    @Override
    public List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup) {
        return jvmsFrom(jvmCrudService.findJvmsBelongingTo(aGroup));
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        groupJvmRelationshipService.removeRelationshipsForJvm(aJvmId);
        jvmCrudService.removeJvm(aJvmId);
    }

    @Override
    public Jvm removeJvmFromGroups(final Identifier<Jvm> aJvmId) {
        groupJvmRelationshipService.removeRelationshipsForJvm(aJvmId);
        return getJvm(aJvmId);
    }

    @Override
    public JpaJvmConfigTemplate uploadServerXml(Event<UploadServerXmlTemplateCommand> event) {
        final JpaJvmConfigTemplate jpaJvmConfigTemplate = jvmCrudService.uploadServerXml(event);
        return jpaJvmConfigTemplate;
    }

    @Override
    public String getJvmTemplate(String templateName, Identifier<Jvm> jvmId) {
        return jvmCrudService.getJvmTemplate(templateName, jvmId);
    }

    protected Jvm jvmFrom(final JpaJvm aJpaJvm) {
        return new JpaJvmBuilder(aJpaJvm).build();
    }

    protected List<Jvm> jvmsFrom(final List<JpaJvm> someJpaJvms) {
        final List<Jvm> jvms = new ArrayList<>();
        for (final JpaJvm jpaJvm : someJpaJvms) {
            jvms.add(jvmFrom(jpaJvm));
        }
        return jvms;
    }
}
