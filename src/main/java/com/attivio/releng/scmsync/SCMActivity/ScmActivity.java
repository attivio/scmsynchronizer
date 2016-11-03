/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync.SCMActivity;

import com.attivio.releng.scmsync.GithubEnterprise.Commit;
import com.attivio.releng.scmsync.ScmSyncException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SCM Activity Data Access Object for publish
 * Also handles conversion
 * (see: Post SCM Activity on https://github.com/scmenthusiast/jira-scm-activity-plugin/wiki/JIRA-SCM-Activity-Plugin-REST-API)
 * Created by userb on 10/11/16.
 */
public class ScmActivity {
  private String issueKey = null;
  private String changeId = null;
  private String changeType = null;

  private String changeAuthor = null;
  private Date changeDate = null;

  private String changeBranch = null;
  private String changeTag = null;
  private String changeStatus = null;
  private String changeLink = null;

  private String changeMessage = null;
  private List<ChangedFile> changeFiles = new ArrayList<>();

  private boolean changeUpdate;

  private boolean notifyEmail;
  private String notifyAs = null;

  /**
   *
   * @param issueKey jira key
   * @param branchPath ref minus refs/heads/
   * @param commit Commit object from payload
   * @throws ScmSyncException
   */
  public ScmActivity(String issueKey, String branchPath, Commit commit) throws ScmSyncException {
    this.issueKey = issueKey;
    changeId = commit.getShortId() + " @ " + branchPath;

    changeAuthor = commit.getAuthor().getUsername();
    changeDate = commit.getTimestamp();

    changeType = "git"; // hardcoded???
    //changeBranch = branchPath;  // skipping because the ID already contains branch
    //changeTag ??

    changeLink = commit.getUrl();

    changeMessage = commit.getMessage();

    addedFiles(commit.getAdded());
    deletedFiles(commit.getRemoved());
    modifiedFiles(commit.getModified());
    notifyAs = "relengauto";
    notifyEmail = false;

  }

  public String toString() {
    return "ScmActivity: "
        + issueKey + " "
        + changeId + " "
        + " files: [" + changeFiles.size() + "] "
        + "@" + changeAuthor
        + " msg: " + changeMessage;
  }

  private void addedFiles(List<String> collection) {
    for(String file: collection) {
      changeFiles.add(new ChangedFile(file, Actions.added ));
    }
  }
  private void deletedFiles(List<String> collection) {
    for(String file: collection) {
      changeFiles.add(new ChangedFile(file, Actions.deleted ));
    }
  }
  private void modifiedFiles(List<String> collection) {
    for(String file: collection) {
      changeFiles.add(new ChangedFile(file, Actions.modified ));
    }
  }

  /**
   * Convert to ScmActivity Json (see: https://github.com/scmenthusiast/jira-scm-activity-plugin/wiki/JIRA-SCM-Activity-Plugin-REST-API )
   * @return
   */
  public String toJson() {
    Gson gson = getGson();
    return gson.toJson(this);
  }

  private static Gson getGson() {
    return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
  }

  /**
   * Create from json string
   * @param json string
   * @return Payload object
   */
  public static ScmActivity createFromString(String json) {
    Gson gson = getGson();
    return gson.fromJson(json, ScmActivity.class);
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getChangeId() {
    return changeId;
  }

  public String getChangeType() {
    return changeType;
  }

  public String getChangeAuthor() {
    return changeAuthor;
  }

  public Date getChangeDate() {
    return changeDate;
  }

  public String getChangeBranch() {
    return changeBranch;
  }

  public String getChangeTag() {
    return changeTag;
  }

  public String getChangeStatus() {
    return changeStatus;
  }

  public String getChangeLink() {
    return changeLink;
  }

  public String getChangeMessage() {
    return changeMessage;
  }

  public boolean isChangeUpdate() {
    return changeUpdate;
  }

  public boolean isNotifyEmail() {
    return notifyEmail;
  }

  public String getNotifyAs() {
    return notifyAs;
  }
  public List<ChangedFile> getChangeFiles() {
    return changeFiles;
  }

  public void setChangeUpdate(boolean changeUpdate) {
    this.changeUpdate = changeUpdate;
  }
}
