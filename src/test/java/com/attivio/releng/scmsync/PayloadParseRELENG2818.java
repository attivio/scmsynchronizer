/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync;

import com.attivio.releng.scmsync.GithubEnterprise.Commit;
import com.attivio.releng.scmsync.GithubEnterprise.Payload;
import com.attivio.releng.scmsync.SCMActivity.ScmActivity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * RELENG-2818 test
 * Created by userb on 10/11/16.
 */
public class PayloadParseRELENG2818 extends TestBase {
  private File testDir;

  @Before
  public void setup() {
    testDir = createEmptyTestDir();
  }

  @Test
  public void testScmActivityDateConversion() throws Exception {
    // Setup Payload
    Payload payload = parsePayloadFromTestFile("RELENG-2818.payload.json");

    List<Commit> commits = payload.getCommits();
    Commit commit = commits.get(0);


    // Convert
    ScmActivity scmActivity = new ScmActivity("R-1", payload.getBranchPath(), commit);

    Date commitDate = commit.getTimestamp();
    Date scmActivityDate = scmActivity.getChangeDate();

    assertEquals("Check Converted timezone in UTC", commit.getTimestamp().toString(),"Tue Oct 25 20:13:06 EDT 2016");
    assertEquals("Expect scmActivity Date Matches Commit", commitDate.toString(), scmActivityDate.toString());

  }


  private Payload parsePayloadFromTestFile(String fileName) throws IOException {
    File json = getTestFile(fileName);
    return Payload.createFromFile(json);
  }

  private void compareActivityToCommit(String branch, Commit c, ScmActivity a) {
    assertTrue("Issue Key " + a.getIssueKey() + " in " + c.getIssueKeys(), c.getIssueKeys().contains(a.getIssueKey()));
    assertEquals("Message", c.getMessage(),a.getChangeMessage());
    assertEquals("Id", c.getShortId() + " @ " + branch, a.getChangeId());
    assertEquals("Author", c.getAuthor().getUsername(), a.getChangeAuthor());
  }
}
