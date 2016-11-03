/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync.GithubEnterprise;

import com.attivio.releng.scmsync.SCMActivity.ScmActivity;
import com.attivio.releng.scmsync.ScmSyncException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Github Payload Data Access Object
 * Created by userb on 10/7/16.
 */
public class Payload {
  private String ref;
  private String before;
  private String after;
  private boolean created;
  private boolean deleted;
  private boolean forced;
  private String base_ref;

  private List<Commit> commits;
  private Repository repository;

  private Logger logger = LoggerFactory.getLogger(Payload.class);
  /**
   * Create from file containing json - mostly to unit tests
   * @param jsonFile file containing json
   * @return Payload object
   * @throws IOException on missing or inaccessible file
   */
  public static Payload createFromFile(File jsonFile) throws IOException {
    String content = new Scanner(jsonFile).useDelimiter("\\Z").next();
    return Payload.createFromString(content);
  }

  /**
   * Create from json string
   * @param json string
   * @return Payload object
   */
  public static Payload createFromString(String json) {
    Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDataAdapter()).create();
    return gson.fromJson(json, Payload.class);
  }

  /**
   * Obtain Branch path from ref
   * @return branch path
   * @throws ScmSyncException on failure to parse refs/heads/branchPATH
   */
  public String getBranchPath() throws ScmSyncException {
    String[] elements = getRef().split("refs/heads/");
    if (elements.length != 2) {
      throw new ScmSyncException("Cannot locate branch path from " + getRef() + " found: " + elements.length + " - " + elements[0]);
    }
    return elements[1];
  }

  /**
   * Convert github commit objects into scm Activity objects
   * @return list of ScmActivity Objects
   * @throws ScmSyncException on bad branch path
   */
  public List<ScmActivity> getScmActitivies() throws ScmSyncException {
    List<ScmActivity> activities = new ArrayList<>();

    for (Commit commit: getCommits()) {
      for (String issueKey: commit.getIssueKeys()) {
        logger.info("Process Changeset: " + commit.getId() + " " + issueKey);
        activities.add(new ScmActivity(issueKey, getBranchPath(), commit) );
      }
    }
    return activities;
  }

  public String toString() {
    // Extract Messages
    List<String> commitSummaries = getCommits().stream()
        .map(Commit::toString)
        .collect(Collectors.toList());

    return "Payload:\n\tRef " + ref + "\n\tBefore " + before + "\n\tAfter: " + after + "\n\tCommits ("
        + getCommits().size() + "):\n\t" + StringUtils.join(commitSummaries,"\n\t");
  }

  // Simple Getters and setters

  public List<Commit> getCommits() {
    return commits;
  }



  public String getRef() {
    return ref;
  }


  public void setRef(String ref) {
    this.ref = ref;
  }



  public void setBefore(String before) {
    this.before = before;
  }

  public void setAfter(String after) {
    this.after = after;
  }

  public boolean isCreated() {
    return created;
  }

  public void setCreated(boolean created) {
    this.created = created;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean isForced() {
    return forced;
  }

  public void setForced(boolean forced) {
    this.forced = forced;
  }

  public String getBase_ref() {
    return base_ref;
  }

  public void setBase_ref(String base_ref) {
    this.base_ref = base_ref;
  }
}
