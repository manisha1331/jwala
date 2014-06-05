package com.siemens.cto.aem.domain.model.webserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class CreateWebServerCommandTest {
    private static final String HOST = "host";

    private static final String NAME = "name";

    private static final Integer portNumber = Integer.valueOf(10000);
    private static final Integer httpsPort = Integer.valueOf(20000);

    List<Identifier<Group>> groupIds = new ArrayList<Identifier<Group>>();

    final Collection<Identifier<Group>> groupIdsOne = null;
    final Collection<Identifier<Group>> groupIdsTwo = new ArrayList<Identifier<Group>>();
    final Collection<Identifier<Group>> groupIdsThree = null;
    final Collection<Identifier<Group>> groupIdsFour = new ArrayList<Identifier<Group>>();

    final CreateWebServerCommand webServer = new CreateWebServerCommand(groupIds, NAME, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerOne = new CreateWebServerCommand(groupIdsOne, NAME, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerTwo = new CreateWebServerCommand(groupIdsTwo, NAME, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerThree = new CreateWebServerCommand(groupIdsThree, NAME, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerFour = new CreateWebServerCommand(groupIdsFour, NAME, HOST, portNumber, httpsPort);

    final CreateWebServerCommand webServerFive = new CreateWebServerCommand(groupIdsFour, NAME, null, portNumber, httpsPort);
    final CreateWebServerCommand webServerSix = new CreateWebServerCommand(groupIdsFour, NAME, null, portNumber, httpsPort);

    final CreateWebServerCommand webServerSeven = new CreateWebServerCommand(groupIdsFour, null, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerEight = new CreateWebServerCommand(groupIdsFour, NAME, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerNine = new CreateWebServerCommand(groupIdsFour, null, HOST, portNumber, httpsPort);
    final CreateWebServerCommand webServerTen = new CreateWebServerCommand(groupIdsFour, "otherName", HOST, portNumber, httpsPort);

    final CreateWebServerCommand webServerEleven = new CreateWebServerCommand(groupIdsFour, "otherName", HOST, null, null);
    final CreateWebServerCommand webServerTwelve = new CreateWebServerCommand(groupIdsFour, "otherName", HOST, null, null);
    final CreateWebServerCommand webServerThirteen = new CreateWebServerCommand(groupIdsFour, "otherName", HOST, 0, 0);
    final CreateWebServerCommand webServerNulls = new CreateWebServerCommand(null, null, null, null, null);

    @Test
    public void testHashCode() {
        assertEquals(webServerTwo.hashCode(), webServerFour.hashCode());
        assertEquals(31 * 31 * 31 * 31, webServerNulls.hashCode());
    }

    @Test
    public void testGetGroups() {
        assertEquals(0, webServer.getGroups().size());
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, webServer.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals(HOST, webServer.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(portNumber, webServer.getPort());
    }

    @Test
    public void testValidateCommand() {
        webServerTen.validateCommand();
    }

    @Test
    public void testEqualsObject() {
        assertTrue(webServerOne.equals(webServerOne));
        assertFalse(webServerOne.equals(null));
        assertFalse(webServerOne.equals(""));
        assertFalse(webServerOne.equals(webServerTwo));
        assertTrue(webServerOne.equals(webServerThree));
        assertFalse(webServerTwo.equals(webServerThree));
        assertFalse(webServerTwo.equals(webServerOne));
        assertTrue(webServerTwo.equals(webServerFour));
        assertFalse(webServerFive.equals(webServerFour));
        assertTrue(webServerFive.equals(webServerSix));
        assertFalse(webServerOne.equals(webServerFive));
        assertFalse(webServerTwo.equals(webServerSix));

        assertFalse(webServerSeven.equals(webServerSix));
        assertFalse(webServerSeven.equals(webServerEight));
        assertTrue(webServerSeven.equals(webServerNine));
        assertFalse(webServerEight.equals(webServerTen));

        assertFalse(webServerEleven.equals(webServerTen));
        assertTrue(webServerEleven.equals(webServerTwelve));
        assertFalse(webServerTen.equals(webServerThirteen));
    }

    @Test
    public void testToString() {
        assertEquals("CreateWebServerCommand {groupIds=[], host=host, name=otherName, port=10000, httpsPort=20000}",
                webServerTen.toString());
    }
}
