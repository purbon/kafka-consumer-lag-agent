package com.purbon.kafka.lag.agent.integration;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.ClassRule;
import org.testcontainers.containers.DockerComposeContainer;

public class BaseIntegrationTest {

  private static final String DEFAULT_JAVA_HOME_PATH = "/bin/java";
  private static final String DEFAULT_JAVA_PATH = "java";

  @ClassRule
  public static DockerComposeContainer environment =
      new DockerComposeContainer(new File(System.getProperty("user.dir")+"/docker/docker-compose-test.yml"))
          .withExposedService("kafka", 9092)
          .withLocalCompose(true);

  protected void stopInternalApp(Process app) throws IOException, InterruptedException {
    try {
      app.getOutputStream().write('\n');
      try {
        app.getOutputStream().flush();
      } catch (IOException ignored) {
      }
    } finally {
      final int exitcode = app.waitFor();
      // Log any errors printed
      int len;
      byte[] buffer = new byte[100];
      while ((len = app.getErrorStream().read(buffer)) != -1) {
        System.out.write(buffer, 0, len);
      }

      assertThat("Application did not exit cleanly", exitcode == 0);
    }
  }

  protected Process buildAndStartInternalTestAppProcess() throws IOException {
    // If not starting the testcase via Maven, set the buildDirectory and finalName system properties manually.
    final String buildDirectory = (String) System.getProperties().get("buildDirectory");
    final String finalName = (String) System.getProperties().get("finalName")+"-jar-with-dependencies";
    final int port = 9999; //Integer.parseInt((String) System.getProperties().get("it.port"));
    final String config = "test.yml"; //resolveRelativePathToResource("test.yml");
    final String javaagent = "-javaagent:" + buildDirectory + "/" + finalName + ".jar=" + port + ":" + config;
    final String java = buildJavaPath(System.getenv("JAVA_HOME"));

    ProcessBuilder pb = new ProcessBuilder("java",
        "-Dcom.sun.management.jmxremote",
        "-Djava.rmi.server.hostname=0.0.0.0",
        "-Dcom.sun.management.jmxremote.port=9999",
        "-Dcom.sun.management.jmxremote.local.only=false",
        "-Dcom.sun.management.jmxremote.authenticate=false",
        "-Dcom.sun.management.jmxremote.ssl=false",
        javaagent,
        "-cp", buildClasspath(),
        "com.purbon.kafka.lag.agent.TestApp");
    final Process app = pb.start();
    return app;
  }

  private String buildJavaPath(String javaHome) {
    if (!(javaHome == null || javaHome.isEmpty())) {
      return javaHome + DEFAULT_JAVA_HOME_PATH;
    }
    return DEFAULT_JAVA_PATH;
  }

  //trying to avoid the occurrence of any : in the windows path
  private String resolveRelativePathToResource(String resource) {
    final String configwk = new File(getClass().getClassLoader().getResource(resource).getFile()).getAbsolutePath();
    final File workingDir = new File(new File(".").getAbsolutePath());
    return "." + configwk.replace(workingDir.getParentFile().getAbsolutePath(), "");
  }


  private String buildClasspath() {
    return "target/lag-exporter-jar-with-dependencies.jar";
  }

  private String[] buildArgs() {
    List<String> jmxOpts = buildJMXOptions();
    String[] args = jmxOpts.toArray(new String[jmxOpts.size()]);
    return args;
  }

  private String buildArgsAsString() {
    StringBuilder sb = new StringBuilder();
    for(String option : buildJMXOptions()) {
      sb.append(option);
      sb.append(" ");
    }
    return sb.toString();
  }

  private List<String> buildJMXOptions() {
    List<String> args = new ArrayList<>();

    args.add("-Dcom.sun.management.jmxremote");
    args.add("-Dcom.sun.management.jmxremote.port=9998");
    args.add("-Dcom.sun.management.jmxremote.rmi.port=9998");
    args.add("-Dcom.sun.management.jmxremote.authenticate=false");
    args.add("-Dcom.sun.management.jmxremote.ssl=false");

    /*args.add("-Dcom.sun.management.jmxremote");
    args.add("-Dcom.sun.management.jmxremote.port=9998");
    args.add("-Dcom.sun.management.jmxremote.rmi.port=9998");
    args.add("-Djava.rmi.server.hostname=0.0.0.0");
    args.add("-Dcom.sun.management.jmxremote=true");
    args.add("-Dcom.sun.management.jmxremote.authenticate=false");
    args.add("-Dcom.sun.management.jmxremote.ssl=false");
     */
    return args;
  }
}

