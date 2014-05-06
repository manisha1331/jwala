package com.siemens.cto.aem.domain.model.app;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class ApplicationCommandTest {

    private CreateApplicationCommand initCreateAndTest(String name, String ctx, Long id) {
        CreateApplicationCommand cac = new CreateApplicationCommand(
                Identifier.id(id, Group.class),
                name, 
                ctx);
        assertEquals(name, cac.getName());
        assertEquals(ctx, cac.getContext());
        assertEquals(Identifier.id(id, Group.class), cac.getGroupId());
        
        return cac;
    }
    
    @Test
    public void testCreateOk() {
        CreateApplicationCommand cac = initCreateAndTest("name", "ctx", 1L);
        cac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateFailName() {
        CreateApplicationCommand cac = initCreateAndTest(null, "ctx", 1L);
        cac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateFailContext() {
        CreateApplicationCommand cac = initCreateAndTest("name", null, 1L);
        cac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateFailId() {
        CreateApplicationCommand cac = initCreateAndTest("name", "ctx", null);
        cac.validateCommand();
    }

    private UpdateApplicationCommand initUpdateAndTest(Long appId, String name, String ctx, Long groupId) {
        UpdateApplicationCommand uac = new UpdateApplicationCommand(
                Identifier.id(appId, Application.class),
                Identifier.id(groupId, Group.class),
                name, 
                ctx
                );
        assertEquals(name, uac.getNewName());
        assertEquals(ctx, uac.getNewContext());
        assertEquals(Identifier.id(appId, Application.class), uac.getId());
        assertEquals(Identifier.id(groupId, Group.class), uac.getNewGroupId());
        
        return uac;
    }
    
    @Test
    public void testUpdateOk() {
        UpdateApplicationCommand uac = initUpdateAndTest(2L, "name", "ctx", 1L);
        uac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailId() {
        UpdateApplicationCommand uac = initUpdateAndTest(null, null, "ctx", 1L);
        uac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailName() {
        UpdateApplicationCommand uac = initUpdateAndTest(2L, null, "ctx", 1L);
        uac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailContext() {
        UpdateApplicationCommand uac = initUpdateAndTest(2L, "name", null, 1L);
        uac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailGroupId() {
        UpdateApplicationCommand uac = initUpdateAndTest(2L, "name", "ctx", null);
        uac.validateCommand();
    }

}
