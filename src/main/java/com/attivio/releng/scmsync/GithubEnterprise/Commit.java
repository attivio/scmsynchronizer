/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync.GithubEnterprise;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Git hub data access object for commit - gson/json
 * Created by userb on 10/7/16.
 */
public class Commit {
  private String id;
  private String tree_id;
  private boolean distinct;
  private String message;
  private Date timestamp;
  private String url;
  private Author author;
  private List<String> added = new ArrayList<>();
  private List<String> removed = new ArrayList<>();
  private List<String> modified = new ArrayList<>();
  private final Pattern issueKeyPattern = Pattern.compile("[A-Z]+-[0-9]+");

  public String toString() {
    return "commit " + getShortId()
        + " files: [a: " + added.size() + " d: " + removed.size()  + " m: " + modified.size()
        + " @" + author.getUsername()
        + " msg: " + message;
  }

  public Author getAuthor() {
    return author;
  }

  /**
   * Git Commit Id Truncated
   * @return short id - first 7 chars
   */
  public String getShortId() { return id.substring(0, Math.min(id.length(), 7)); }

  /**
   * Extract Issues Keys from Message
   * @return list of keys
   */
  public List<String> getIssueKeys() {
    Set<String> keys = new HashSet<>();
    Matcher matcher = issueKeyPattern.matcher(getMessage());
    while (matcher.find()) {
      keys.add(matcher.group());
    }

    return new ArrayList<>(keys);
  }

  // Standard Getters and Setters - mostly for testing

  public String getId() {
    return id;
  }

  public String getTree_id() {
    return tree_id;
  }

  public String getMessage() {
    return message;
  }

  public String getUrl() {
    return url;
  }

  public List<String> getAdded() {
    return added;
  }

  public List<String> getRemoved() {
    return removed;
  }

  public List<String> getModified() {
    return modified;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  void setMessage(String message) {
    this.message = message;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setTree_id(String tree_id) {
    this.tree_id = tree_id;
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }
}
