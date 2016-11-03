/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync;

import com.attivio.releng.scmsync.GithubEnterprise.Commit;
import com.attivio.releng.scmsync.GithubEnterprise.Payload;
import com.attivio.releng.scmsync.SCMActivity.Actions;
import com.attivio.releng.scmsync.SCMActivity.ChangedFile;
import com.attivio.releng.scmsync.SCMActivity.ScmActivity;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Payload parse and data access
 * Created by userb on 10/11/16.
 */
public class PayloadParseTest extends TestBase {
  private File testDir;

  @Before
  public void setup() {
    testDir = createEmptyTestDir();
  }


  @Test
  public void testScmActivityJsonHappyPath() throws Exception {
    // Setup Payload
    Payload payload = parsePayloadFromTestFile("githubHook-Commits.json");

    List<Commit> commits = payload.getCommits();
    Commit commit = commits.get(0);

    // Convert
    ScmActivity scmActivity = new ScmActivity("R-1", payload.getBranchPath(), commit);

    // reload
    String text = scmActivity.toJson();
    ScmActivity reloaded = ScmActivity.createFromString(text);

    // Verify
    assertEquals(scmActivity.getIssueKey(), reloaded.getIssueKey());
    assertEquals(scmActivity.getChangeAuthor(), reloaded.getChangeAuthor());
    for (ChangedFile c: scmActivity.getChangeFiles()) {
      assertTrue("Expected " + c.toString() + " in\n" + reloaded.getChangeFiles(), reloaded.getChangeFiles().contains(c));
    }

  }
  @Test
  public void testCreateSCMActivityFromPlayloadCommit() throws IOException, ScmSyncException {
    // Expected Results
    List<ChangedFile> expected = new ArrayList<>();
    expected.add(new ChangedFile("modules/foo/src/org/githuborg/apack/NewClass.java", Actions.added));
    expected.add(new ChangedFile("modules/foo/src/org/githuborg/dpack/Defunct.java", Actions.deleted));
    expected.add(new ChangedFile("modules/foo/src/org/githuborg/mpack/Utils.java", Actions.modified));
    expected.add(new ChangedFile("modules/foo/src/org/githuborg/mpack/config/Client.java", Actions.modified));

    // Setup Payload
    Payload payload = parsePayloadFromTestFile("githubHook-Commits.json");
    List<Commit> commits = payload.getCommits();
    Commit commit = commits.get(0);

    // Convert
    ScmActivity scmActivity = new ScmActivity("R-1", payload.getBranchPath(), commit);

    // Verify
    List<ChangedFile> changes = scmActivity.getChangeFiles();
    assertEquals(changes.toString(), 4, changes.size());
    for (ChangedFile c: expected) {
      assertTrue("Expected missing from found changed files: " + c.toString() + "\nFound:\n"+ changes, changes.contains(c));
    }
  }

  @Test
  public void testBranchCatchupMergeFromMasterTwoCommits() throws Exception {
    Payload payload = parsePayloadFromTestFile("githubHook-branchCatchupWithMaster2Commits.json");
    assertEquals("refs/heads/feature/catchup", payload.getRef());
    List<Commit> commits =  payload.getCommits();
    assertEquals(2, commits.size());

    // Extract Messages
    List<String> messages = commits.stream()
        .map(Commit::getMessage)
        .collect(Collectors.toList());

    // Verify these are the commits we expect
    assertTrue(messages.contains("RELENG-1 commit1"));
    assertTrue(messages.contains("RELENG-1 commit2"));

  }


  @Test
  public void testMergeTwoCommitsFromPublishedBranch() throws Exception {
    Payload payload = parsePayloadFromTestFile("githubHook-multimergeMerged.json");
    assertEquals("refs/heads/master", payload.getRef());
    List<Commit> commits =  payload.getCommits();
    assertEquals(2, commits.size());

    // Extract Messages
    List<String> messages = commits.stream()
        .map(Commit::getMessage)
        .collect(Collectors.toList());

    // Verify these are the commits we expect
    assertTrue(messages.contains("RELENG-1 commit1"));
    assertTrue(messages.contains("RELENG-1 commit2"));

  }

  @Test
  public void testNewBranchNoCommits() throws Exception {
    Payload payload = parsePayloadFromTestFile("githubHook-NewBranch.json");
    assertEquals("refs/heads/feature/newbranch", payload.getRef());
    assertEquals(0, payload.getCommits().size());
  }


  @Test
  public void testSingleCommitWithTwoTickets() throws IOException, ScmSyncException {
    Payload payload = parsePayloadFromTestFile("githubHook-Commits.json");

    assertEquals("refs/heads/develop", payload.getRef());

    // Check Commit
    List<Commit> commits = payload.getCommits();
    assertEquals(1, commits.size());

    Commit commit = commits.get(0);

    assertEquals("auser", commit.getAuthor().getName());
    assertEquals("auser", commit.getAuthor().getUsername());
    assertEquals("auser@ourgroup.org", commit.getAuthor().getEmail());

    // Check Activities
    List<ScmActivity> activities = payload.getScmActitivies();
    assertEquals(2, activities.size());

    ScmActivity activity1 = activities.get(0);
    ScmActivity activity2 = activities.get(1);

    compareActivityToCommit(payload.getBranchPath(), commit, activity1);
    compareActivityToCommit(payload.getBranchPath(), commit, activity2);
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
