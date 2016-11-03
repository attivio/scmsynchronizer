/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test Server Configuration
 * Created by userb on 10/7/16.
 */
public class ServerStartupTests extends TestBase {
  private File testDir;
  @Before
  public void setup() throws IOException {
    testDir = createEmptyTestDir();
  }

  @After
  public void tearDown(){
  }

  @Test
  public void happyPathConfiguration() throws Exception {
    Properties props = new Properties();
    props.setProperty("port","31");
    props.setProperty("allowed.hosts", "127.0.0.1,192.168.1.1");
    props.setProperty("jira.url", "http://jira");
    props.setProperty("jira.user", "user");
    props.setProperty("jira.password", "password");
    File propFile = writePropertiesFile(props, Server.propertiesFileName);

    // Set runtime environment
    System.setProperty(Server.homeDirProperty, testDir.getAbsolutePath());

    Server server = new Server();
    assertEquals(31, server.getPort());
    assertTrue(server.hostAllowed("127.0.0.1"));
    assertFalse(server.hostAllowed("10.0.0.1"));
    assertEquals("user", server.getJiraUser());
    assertEquals("password", server.getJiraPassword());
    assertEquals("http://jira/rest/scmactivity/1.0/changeset/activity", server.getJiraRestUrl());
  }

  @Test
  public void missingJiraUrl() throws IOException, ScmSyncException {
    Properties props = new Properties();
    props.setProperty("port","31");
    props.setProperty("allowed.hosts", "127.0.0.1,192.168.1.1");
    props.setProperty("jira.user", "user");
    props.setProperty("jira.password", "password");

    writePropertiesFile(props, Server.propertiesFileName);

    // Set runtime environment
    System.setProperty(Server.homeDirProperty, testDir.getAbsolutePath());

    expectedException.expect(ScmSyncException.class);
    expectedException.expectMessage(new StringContains("jira.url property missing from"));

    new Server();

  }

  @Test
  public void missingAllowedList() throws Exception {
    // Configure Partial Properties File
    Properties props = new Properties();
    props.setProperty("port","31");
    File propFile = writePropertiesFile(props, Server.propertiesFileName);

    // Set runtime environment
    System.setProperty(Server.homeDirProperty, testDir.getAbsolutePath());

    // configure expected error
    expectedException.expect(ScmSyncException.class);
    expectedException.expectMessage(new StringContains("Cannot locate allowed.hosts property in scmsync properties"));

    new Server();

  }
  private File writePropertiesFile(Properties props, String fileName) throws IOException {
    File propFile = new File(testDir,fileName);
    OutputStream out = new FileOutputStream(propFile);
    props.store(out, "missing allowed hosts");
    return propFile;
  }

  @Test
  public void badHomeVar() throws Exception {
    // Set Bad Dir
    String dnePath = new File(testDir, "doesNOTexist").getAbsolutePath();
    System.setProperty(Server.homeDirProperty, dnePath);
    assertEquals(dnePath, System.getProperty(Server.homeDirProperty));

    // configure expected error
    expectedException.expect(ScmSyncException.class);
    expectedException.expectMessage(new StringContains("Cannot locate directory for scmsync.home.dir: " + dnePath));


    // Cause failure
    new Server();
  }
  @Test
  public void missingHomeVar() throws Exception {
    // configure expected error
    expectedException.expect(ScmSyncException.class);
    expectedException.expectMessage(new StringContains("Cannot locate scmsync.home.dir system property"));

    // Cause failure
    new Server();

  }
}
