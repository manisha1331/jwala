package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.zip.ZipDirectory;
import com.cerner.jwala.service.zip.impl.ZipDirectoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);

    private static final String BINARY_LOCATION_PROPERTY_KEY = "jwala.binary.dir";
    private static final String UNZIPEXE = "unzip.exe";
    private static final String APACHE_EXCLUDE = "ReadMe.txt *--";

    private final ZipDirectory zipDirectory = new ZipDirectoryImpl();
    private final BinaryDistributionControlService binaryDistributionControlService;

    @Autowired
    public BinaryDistributionServiceImpl(BinaryDistributionControlService binaryDistributionControlService) {
        this.binaryDistributionControlService = binaryDistributionControlService;
    }

    @Override
    public void distributeJdk(final String hostname) {
        File javaHome = new File(ApplicationProperties.get("stp.java.home"));
        String jdkDir = javaHome.getName();
        String binaryDeployDir = javaHome.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        if (jdkDir != null && !jdkDir.isEmpty()) {
            distributeBinary(hostname, jdkDir, binaryDeployDir, "");
        } else {
            LOGGER.warn("JDK dir location is null or empty {}", jdkDir);
        }
    }

    @Override
    public void distributeTomcat(final String hostname) {
        File tomcat = new File(ApplicationProperties.get("remote.paths.tomcat.core"));
        String tomcatDir = tomcat.getParentFile().getName();
        String binaryDeployDir = tomcat.getParentFile().getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        if (tomcatDir != null && !tomcatDir.isEmpty()) {
            distributeBinary(hostname, tomcatDir, binaryDeployDir, "");
        } else {
            LOGGER.warn("Tomcat dir location is null or empty {}", tomcatDir);
        }
    }

    @Override
    public void distributeWebServer(final String hostname) {
        File apache = new File(ApplicationProperties.get("remote.paths.apache.httpd"));
        String webServerDir = apache.getName();
        String binaryDeployDir = apache.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        if (webServerDir != null && !webServerDir.isEmpty()) {
            distributeBinary(hostname, webServerDir, binaryDeployDir, APACHE_EXCLUDE);
        } else {
            LOGGER.warn("WebServer dir location is null or empty {}", webServerDir);
        }
    }

    private void distributeBinary(final String hostname, final String binaryName, final String binaryDeployDir, final String exclude) {
        String binaryDir = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY);
        if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
            if (!remoteFileCheck(hostname, binaryDeployDir + "/" + binaryName)) {
                LOGGER.info("Couldn't find {} on host {}. Trying to deploy it", binaryName, hostname);
                if (binaryDir != null && !binaryDir.isEmpty()) {
                    String zipFile = binaryDir + "/" + binaryName + ".zip";
                    String destinationZipFile = binaryDeployDir + "/" + binaryName + ".zip";
                    if (!new File(zipFile).exists()) {
                        LOGGER.debug("binary zip does not exists, create zip");
                        zipFile = zipBinary(binaryDir + "/" + binaryName);
                    }

                    remoteCreateDirectory(hostname, binaryDeployDir);

                    remoteSecureCopyFile(hostname, zipFile, destinationZipFile);

                    remoteUnzipBinary(hostname, AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue() + "/" + UNZIPEXE, destinationZipFile, binaryDeployDir, exclude);

                    remoteDeleteBinary(hostname, destinationZipFile);

                } else {
                    LOGGER.warn("Cannot find the binary directory location in jwala, value is {}", binaryDir);
                }
            } else {
                LOGGER.info("Found {} at on host {}", binaryName, hostname);
            }

        } else {
            LOGGER.warn("Binary deploy location not provided value is {}", binaryDeployDir);
        }
    }

    public void changeFileMode(final String hostname, final String mode, final String targetDir, final String target) {
        try {
            if (binaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target).getReturnCode().wasSuccessful()) {
                LOGGER.info("change file mode " + mode + " at targetDir " + targetDir);
            } else {
                String message = "Failed to change the file permissions in " + targetDir + "/" + UNZIPEXE;
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in change file mode at host: " + hostname + " mode: " + mode + " target: " + target;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    public void remoteDeleteBinary(final String hostname, final String destination) {
        try {
            if (binaryDistributionControlService.deleteBinary(hostname, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully delete the binary {}", destination);
            } else {
                final String message = "error in deleting file " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in delete remote binary at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    public void remoteUnzipBinary(final String hostname, final String zipPath, final String binaryLocation, final String destination, final String exclude) {
        try {
            if (binaryDistributionControlService.unzipBinary(hostname, zipPath, binaryLocation, destination, exclude).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully unzipped the binary {}", binaryLocation);
            } else {
                final String message = "cannot unzip from " + binaryLocation + " to " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in remote unzip binary at host: " + hostname + " binaryLocation: " + binaryLocation + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    public void remoteSecureCopyFile(final String hostname, final String source, final String destination) {
        try {
            if (binaryDistributionControlService.secureCopyFile(hostname, source, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully copied the binary {} over to {}", source, destination);
            } else {
                final String message = "error with scp of binary " + source + " to destination " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in remote secure copy at host: " + hostname + " source: " + source + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    public void remoteCreateDirectory(final String hostname, final String destination) {
        try {
            if (binaryDistributionControlService.createDirectory(hostname, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully created directories {}", destination);
            } else {
                final String message = "user does not have permission to create the directory " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in create remote directory at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    public boolean remoteFileCheck(final String hostname, final String destination) {
        //TODO: it will never catch here
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname, destination).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in check remote File at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        return result;
    }

    @Override
    public String zipBinary(final String location) {
        String destination = null;
        if (location != null && !location.isEmpty() && new File(location).exists()) {
            destination = location + ".zip";
            LOGGER.info("{} found, zipping to {}", location, destination);
            zipDirectory.zip(location, destination);
        } else {
            LOGGER.warn("Could not find the location {}", location);
        }
        return destination;
    }

    @Override
    public void prepareUnzip(String hostname) {
        final String tocScriptsPath = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();
        if (remoteFileCheck(hostname, tocScriptsPath)) {
            LOGGER.info(tocScriptsPath + " exists at " + hostname);
        } else {
            remoteCreateDirectory(hostname, tocScriptsPath);
        }
        final String unzipFileDestination = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();
        if (remoteFileCheck(hostname, unzipFileDestination + "/" + UNZIPEXE)) {
            LOGGER.info(unzipFileDestination + "/" + UNZIPEXE + " exists at " + hostname);
        } else {
            final String unzipFileSource = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY) + "/" + UNZIPEXE;
            LOGGER.info("unzipFileSource: " + unzipFileSource);
            remoteSecureCopyFile(hostname, unzipFileSource, unzipFileDestination);
            changeFileMode(hostname, "a+x", tocScriptsPath, UNZIPEXE);
        }
    }
}