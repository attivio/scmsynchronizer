/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync.SCMActivity;

/**
 * SCmActivity Data Access Object
 * Created by userb on 10/11/16.
 */
public class ChangedFile {
  private String fileName = null;
  private String fileAction = null;

  public ChangedFile(String fileName, String fileAction) {
    this.fileName = fileName;
    this.fileAction = fileAction;
  }

  @Override
  public int hashCode() {
    return fileAction.hashCode() + fileName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (null == obj ||
        ! (obj instanceof ChangedFile) ) { return false; }

    ChangedFile cObj = (ChangedFile) obj;

    return cObj.getFileAction().equals(this.getFileAction()) &&
        cObj.getFileName().equals(this.getFileName());
  }

  public String toString() {
    return fileName + " :: " + fileAction;
  }

  private String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileAction() {
    return fileAction;
  }

  public void setFileAction(String fileAction) {
    this.fileAction = fileAction;
  }
}
