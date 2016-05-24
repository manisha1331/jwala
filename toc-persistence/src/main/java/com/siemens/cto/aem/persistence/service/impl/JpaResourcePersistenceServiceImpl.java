package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaResourceInstanceBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaGroupAppConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.ResourceInstanceCrudService;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public class JpaResourcePersistenceServiceImpl implements ResourcePersistenceService {

    private final ResourceInstanceCrudService resourceInstanceCrudService;

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager em;

    public JpaResourcePersistenceServiceImpl(ResourceInstanceCrudService resourceInstanceCrudService) {
        this.resourceInstanceCrudService = resourceInstanceCrudService;
    }

    @Override
    public ResourceInstance createResourceInstance(ResourceInstanceRequest resourceInstanceRequest) {
        return parseFromJpa(this.resourceInstanceCrudService.createResourceInstance(resourceInstanceRequest));
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupId(final Long groupId) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstancesByGroupId(groupId));
    }

    @Override
    public ResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstanceByGroupIdAndName(groupId, name));
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String resourceTypeName) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstancesByGroupIdAndResourceTypeName(groupId, resourceTypeName));
    }

    @Override
    public ResourceInstance updateResourceInstance(ResourceInstance resourceInstance, final ResourceInstanceRequest resourceInstanceRequest) {
        if (resourceInstanceRequest.getAttributes() != null) {
            this.resourceInstanceCrudService.updateResourceInstanceAttributes(resourceInstance.getResourceInstanceId(), resourceInstanceRequest);
        }
        if (!resourceInstanceRequest.getName().equals(resourceInstance.getName())) {
            this.resourceInstanceCrudService.updateResourceInstanceName(resourceInstance.getResourceInstanceId(), resourceInstanceRequest);
        }
        return parseFromJpa(this.resourceInstanceCrudService.getResourceInstance(resourceInstance.getResourceInstanceId()));
    }

    @Override
    public ResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId) {
        return parseFromJpa(this.resourceInstanceCrudService.getResourceInstance(resourceInstanceId));
    }

    @Override
    public void deleteResourceInstance(final Identifier<ResourceInstance> resourceInstanceId) {
        this.resourceInstanceCrudService.deleteResourceInstance(resourceInstanceId);
    }

    @Override
    public void deleteResources(String groupName, List<String> resourceNames) {
        // We have to manually delete the attributes table to prevent constraint violation exceptions.
        // The attributes were defined with @ElementCollection and as such cascade deletion doesn't seem to
        // include related entities. Please see:
        // http://stackoverflow.com/questions/3903202/how-to-do-bulk-delete-in-jpa-when-using-element-collections
        final StringBuilder sb = new StringBuilder();
        for (String resourceName: resourceNames) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("'").append(resourceName).append("'");
        }
        Query nativeQry = em.createNativeQuery("DELETE FROM RESOURCE_INSTANCE_ATTRIBUTES WHERE RESOURCE_INSTANCE_ID IN " +
                "(SELECT r.RESOURCE_INSTANCE_ID FROM RESOURCE_INSTANCE r WHERE r.RESOURCE_INSTANCE_NAME IN (" + sb.toString() + "))");
        nativeQry.executeUpdate();
        em.flush();

        final Query qry = em.createNamedQuery(JpaResourceInstance.DELETE_RESOURCES_QUERY);
        qry.setParameter("groupName", groupName);
        qry.setParameter("resourceNames", resourceNames);
        qry.executeUpdate();
        em.flush();
    }

    private final List<ResourceInstance> parseFromJpa(List<JpaResourceInstance> jpaResourceInstances) {
        List<ResourceInstance> resourceInstances = new ArrayList<>();
        for (JpaResourceInstance resourceInstance: jpaResourceInstances) {
            JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(resourceInstance);
            resourceInstances.add(builder.build());
        }
        return resourceInstances;
    }
    private final ResourceInstance parseFromJpa(JpaResourceInstance jpaResourceInstance) {
        JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(jpaResourceInstance);
        return builder.build();
    }

    @Override
    // NOTE: We're going to use the entity manager here since we are phasing out the CRUD layer soon.
    public List<String> getApplicationResourceNames(final String groupName, final String appName) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_APP_RESOURCE_NAMES);
        q.setParameter("grpName", groupName);
        q.setParameter("appName", appName);
        return q.getResultList();
    }

    @Override
    public String getAppTemplate(final String groupName, final String appName, final String templateName) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_CONTENT);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return (String) q.getSingleResult();
    }
}
