/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync.GithubEnterprise;

import com.attivio.releng.scmsync.TestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Finding keys in commit comments
 * Created by userb on 10/12/16.
 */
public class CommentMatcherTest extends TestBase {
  private Commit commit = new Commit();


  @Test
  public void matchSingleKey() {
    // Message
    String jiraKey = "JIRAKEY-1495";
    commit.setMessage("some text  " + jiraKey + " some other text");

    // Find Keys
    List<String> foundKeys = commit.getIssueKeys();

    // Verify
    assertEquals("Number of Keys", 1, foundKeys.size());
    assertEquals("Value of Key", jiraKey, foundKeys.get(0));
  }


  @Test
  public void matchMultiKeys() {
    // Message
    String jiraKey1 = "JIRAKEY-1495";
    String jiraKey2 = "JIRAKEY-1496";
    String jiraKey3 = "JIRAKEY-1497";
    List<String> expectedKeys = new ArrayList<>(Arrays.asList(jiraKey1, jiraKey2, jiraKey3));
    commit.setMessage("some text  " + jiraKey1 + " some other"+ jiraKey2 + " text " + jiraKey3);

    // Find Keys
    List<String> foundKeys = commit.getIssueKeys();

    // Verify
    assertEquals("Number of Keys", expectedKeys.size(), foundKeys.size());
    for (String key : expectedKeys) {
      assertTrue("Expected to find " + key + " in " + foundKeys.toString(),
          foundKeys.contains(key));
    }
  }

  @Test
  public void duplicateKey() {
    // Message
    String jiraKey1 = "JIRAKEY-1495";
    String jiraKey2 = "JIRAKEY-1496";
    String jiraKey3 = "JIRAKEY-1496";  // duplicate
    List<String> expectedKeys = new ArrayList<>(Arrays.asList(jiraKey1, jiraKey2, jiraKey3));
    commit.setMessage("some text  " + jiraKey1 + " some other"+ jiraKey2 + " text " + jiraKey3);

    // Find Keys
    List<String> foundKeys = commit.getIssueKeys();

    // Verify
    assertEquals("Number of Keys", 2, foundKeys.size());

    for (String key : new ArrayList<>(Arrays.asList(jiraKey1, jiraKey2)) ) {
      assertTrue("Expected to find " + key + " in " + foundKeys.toString(),
          foundKeys.contains(key));
    }

  }
}
