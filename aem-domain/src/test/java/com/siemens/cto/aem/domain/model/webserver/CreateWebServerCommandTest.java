package com.siemens.cto.aem.domain.model.webserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;

import static org.junit.Assert.assertEquals;

public class CreateWebServerCommandTest {

    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final Path STATUS_PATH = new Path("/statusPath");
    private static final FileSystemPath HTTP_CONFIG_FILE = new FileSystemPath("d:/some-dir/httpd.conf");
    private static final Integer portNumber = 10000;
    private static final Integer httpsPort = 20000;

    final List<Identifier<Group>> groupIds = new ArrayList<>();

    final Collection<Identifier<Group>> groupIdsFour = new ArrayList<>();

    final CreateWebServerCommand webServer =
            new CreateWebServerCommand(groupIds, NAME, HOST, portNumber, httpsPort, STATUS_PATH, HTTP_CONFIG_FILE);
    final CreateWebServerCommand webServerTen =
            new CreateWebServerCommand(groupIdsFour, "otherName", HOST, portNumber, httpsPort, STATUS_PATH, HTTP_CONFIG_FILE);

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
    public void testGetStatusPath() {
        assertEquals(STATUS_PATH, webServer.getStatusPath());
    }

    @Test
    public void testValidateCommand() {
        webServerTen.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPath() {
        final CreateWebServerCommand invalidPath =
                new CreateWebServerCommand(groupIdsFour, "otherName", HOST, 0, 0, new Path("abc"), new FileSystemPath(""));
        invalidPath.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidFileSystemPath() {
        final CreateWebServerCommand invalidPath =
                new CreateWebServerCommand(groupIdsFour, "otherName", HOST, 0, 0, new Path("/abc"), new FileSystemPath("/some-dir/httpd.conf"));
        invalidPath.validateCommand();
    }
}
