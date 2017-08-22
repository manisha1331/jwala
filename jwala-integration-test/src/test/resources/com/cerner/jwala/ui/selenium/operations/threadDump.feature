Feature: Thread Dump

  Scenario:Thread dump a started jvm
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92      |
      | mediaType       | JDK              |
      | archiveFilename | jdk1.8.0_92.zip  |
      | remoteDir       | media.remote.dir |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache-tomcat-7.0.55.zip |
      | remoteDir       | media.remote.dir         |
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | host1               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |

    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I created a web server resource with the following parameters:
      | group        | seleniumGroup              |
      | webServer    | seleniumWebserver          |
      | deployName   | httpd.conf                 |
      | deployPath   | httpd.resource.deploy.path |
      | templateName | httpdconf.tpl              |

    And I created a jvm resource and metadata with the following parameters:
      | group        | seleniumGroup  |
      | jvm          | seleniumJvm    |
      | deployName   | hello.xml      |
      | metaDataFile | hello.xml.json |
      | templateName | hello.xml.tpl  |

    And I created a jvm resource and metadata with the following parameters:
      | group        | seleniumGroup  |
      | jvm          | seleniumJvm    |
      | deployName   | hello.xml      |
      | metaDataFile | hello.xml.json |
      | templateName | hello.xml.tpl  |

    And I created a jvm resource and metadata with the following parameters:
      | group        | seleniumGroup   |
      | jvm          | seleniumJvm     |
      | deployName   | setenv.bat      |
      | metaDataFile | setenv.bat.json |
      | templateName | setenv.bat.tpl  |

    And I created a jvm resource and metadata with the following parameters:
      | group        | seleniumGroup   |
      | jvm          | seleniumJvm     |
      | deployName   | server.xml      |
      | metaDataFile | server.xml.json |
      | templateName | server.xml.tpl  |

    And I created a web app resource with the following parameters:
      | group        | seleniumGroup               |
      | webApp       | seleniumWebapp              |
      | deployName   | hello-world.war             |
      | deployPath   | webapp.resource.deploy.path |
      | templateName | hello-world.war             |

    And I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group
    And I click the generate web application button of "seleniumWebapp"
    And I see "seleniumWebapp" web application got deployed successfully
    And I generate the webserver "seleniumWebserver" of the group "seleniumGroup"
    And I check that the web server "seleniumWebserver" was successfully generated
    And I generate the jvm "seleniumJvm" of the group "seleniumGroup"
    And I see the individual JVM "seleniumJvm" was successfully generated
    And I started web server "seleniumWebserver" of group "seleniumGroup"
    And I click start on jvm "seleniumJvm" of the group "seleniumGroup"
    And I see the state of "seleniumWebserver" web server of group "seleniumGroup" is "STARTED"
    And I click thread dump of jvm "seleniumJvm" of the group "seleniumGroup"
    Then I see the thread dump page