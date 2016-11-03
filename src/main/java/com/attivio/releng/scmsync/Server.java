/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync;

import com.attivio.releng.scmsync.GithubEnterprise.Payload;
import com.attivio.releng.scmsync.SCMActivity.ScmActivity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Server to receive json from github, convert and push to jira's plugin
 * Created by userb on 10/7/16.
 */
public class Server {
  static final String homeDirProperty = "scmsync.home.dir";
  static final String propertiesFileName = "scmsync.properties";


  // Jira Config
  private String jiraRestUrl;
  private String jiraUser;
  private String jiraPassword;

  private int port = 10000;
  private List<InetAddress> allowedHosts = new ArrayList<>();
  private File homeDir;
  private Logger logger = LoggerFactory.getLogger(Server.class);

  public Server() throws ScmSyncException, IOException {
    setHomeDir();
    configure();
  }

  private void configure() throws ScmSyncException, IOException {
    // Load Configuration
    File config = getConfigurationFile();
    Properties props = loadProperties(config);

    // Set Server Configuration
    port = Integer.parseInt(props.getProperty("port"));
    loadAllowedHosts(props);

    // Set Jira Config
    jiraRestUrl = props.getProperty("jira.url") +  "/rest/scmactivity/1.0/changeset/activity";
    if (null == props.getProperty("jira.url")) { throw new ScmSyncException("jira.url property missing from " + config.getAbsolutePath()); }
    jiraUser = props.getProperty("jira.user");
    if (null == jiraUser) { throw new ScmSyncException("jira.user property missing from " + config.getAbsolutePath()); }
    jiraPassword = props.getProperty("jira.password");
    if (null == jiraPassword) { throw new ScmSyncException("jira.password property missing from " + config.getAbsolutePath()); }
  }

  /**
   * Load List of allowed client hosts
   * @param props properties loaded from file
   * @throws ScmSyncException  cannot locate property
   * @throws UnknownHostException cannot locate host ip
   */
  private void loadAllowedHosts(Properties props) throws ScmSyncException, UnknownHostException {
    String allowed = props.getProperty("allowed.hosts");
    if (null == allowed) {
      throw new ScmSyncException("Cannot locate allowed.hosts property in scmsync properties");
    }

    String [] elements = allowed.split(",");
    for(String e: elements) {
      InetAddress address = InetAddress.getByName(e);
      allowedHosts.add(address);
    }
  }

  /**
   * find configuration file
   * @return File object for scmsync.properties
   * @throws ScmSyncException on failure to locate file
   */
  private File getConfigurationFile() throws ScmSyncException {
    File config = new File(homeDir, propertiesFileName);
    if (! config.exists() || ! config.isFile()) {
      throw new ScmSyncException("Cannot locate configuration file " + config.getAbsolutePath());
    }
    return config;
  }

  /**
   * Load configuration properties
   * @param config config file
   * @return properties from file
   * @throws IOException on failure to read
   */
  private Properties loadProperties(File config) throws IOException {
    Properties props = new Properties();
    try (InputStream input = new FileInputStream(config)) {
      props.load(input);
    }
    return props;
  }

  /**
   * Set Home dir from system property
   * @throws ScmSyncException on lack of property
   */
  private void setHomeDir() throws ScmSyncException {
    String path = System.getProperty(homeDirProperty);
    if (null == path) {
      throw new ScmSyncException("Cannot locate " + homeDirProperty + " system property");
    }
    setHomeDir(path);
  }

  /**
   * Set homedir from path
   * @param path path to home dir
   * @throws ScmSyncException on dir not exist.
   */
  private void setHomeDir(String path) throws ScmSyncException {
    homeDir = new File(path);
    if (! homeDir.exists() || ! homeDir.isDirectory()) {
      throw new ScmSyncException("Cannot locate directory for " + homeDirProperty + ": " + path);
    }
  }

  /**
   * Post Access URL
   * @return url for post access for use in Github webhook config
   */
  private String getAccessUrl() {
    String host = "localhost";
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
     logger.error("Cannot determine hostname - using locahost");
    }
    return "http://" + host + ":" + port + "/payload";
  }

  /**
   * Start Server
   */
  public void start() {
    logger.info("Starting... " + getAccessUrl() );

    port(this.port);

    // SparkJava lamba for /payload post receiver
    post("/payload",(req, res) -> {
      if (! hostAllowed(req.ip())) {
        String message= "Unauthorized Host Denied: " + req.ip();
        logger.error(message);
        return message;
      }
      String result = processCommits(req.body());
      return result;
    });

  }

  /**
   *  Process commits in payload
   * @param json json payload
   * @return status message
   */
  private String processCommits(String json) {
    Payload payload = Payload.createFromString(json);
    logger.debug("Payload Acquired:\n\t" + payload.toString());
    List<ScmActivity> activities = null;

    // Generate Activities
    try {
      activities = payload.getScmActitivies();
    } catch (ScmSyncException e) {
      String message = "Cannot Process payload because " + e.getMessage()
          + "\nPayload:\n" + payload.toString();
      logger.error(message);
      return message;
    }

    // Process Activities
    for (ScmActivity activity : activities) {
      try {
        jirasend(activity);
      } catch (ScmSyncException e) {
        logger.error(e.getMessage());
        return e.getMessage();
      }
    }
    return "Processed Payload\n" + payload.toString();
  }

  /**
   * Sends an Activity to SCMActivity Plugin's REST api
   * @param activity ScmActivity object corresponding to commit found in push payload
   * @throws ScmSyncException on post failure
   */
  private void jirasend(ScmActivity activity) throws ScmSyncException {

    // Setup Client
    Client client = Client.create();
    WebResource webResource = client.resource(jiraRestUrl);
    client.addFilter(new HTTPBasicAuthFilter(jiraUser, jiraPassword));

    // Post to SCMActivity
    ClientResponse clientResponse = webResource.type("application/json").post(ClientResponse.class, activity.toJson());

    String result = clientResponse.getStatus() + " " + clientResponse.getEntity(String.class);
    logger.debug("Jira Send: \n\t" + activity.toString() + "\n\tResponse: " + result );

    if (clientResponse.getStatus() == 201 ) {
      logger.info("OK " + activity.getChangeId());
    } else {
      throw new ScmSyncException("Jira Rejected Activity because " + result  + " " + activity.toString());
    }
  }

  /**
   * Check if host allowed
   * @param hostIp ip or dns name of host
   * @return true if allowed
   */
  boolean hostAllowed(String hostIp) {
    InetAddress address;
    try {
      address = InetAddress.getByName(hostIp);
    } catch (UnknownHostException e) {
      logger.error("Cannot lookup host: " + hostIp + " because " + e.getMessage());
      return false;
    }

    for (InetAddress allowed : allowedHosts) {
      if (allowed.equals(address)) {
        return true;
      }
    }
    return false;
  }

  // Mainly to support unit tests
  int getPort() {
    return port;
  }
  String getJiraRestUrl() {  return jiraRestUrl; }
  String getJiraUser() {     return jiraUser; }
  String getJiraPassword() { return jiraPassword; }

  /**
   * Main Class for startup via app assembler
   * @param args unused at present
   * @throws Exception on error
   */
  public static void main(String[] args) throws Exception {
    Server server = new Server();
    server.start();
  }

}
