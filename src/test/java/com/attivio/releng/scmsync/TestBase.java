/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Base Class for tests
 * Created by userb on 10/9/2016.
 */
public class TestBase {
  Date now = new Date();
  private String hostName;

  @Rule
  public TestName name = new TestName();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  public TestBase() {
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostName = "UnknownHost";
    }
  }

  /**
   *
   * @param fileName beneath resources
   * @return file object for test file
   */
  File getTestFile(String fileName) {
    return new File("src/test/resources/" + fileName);
  }

  /**
   * Create empty dir for test (target/testdata/class-method
   * @return File for testdir
   */
  File createEmptyTestDir() {
    return createEmptyTestDir(getClass().getName() + "-" + name.getMethodName());
  }

  /**
   * Empty test directory
   * @param testName to use for testdir name
   * @return File for testdir
   */
  File createEmptyTestDir(String testName) {
    File testDir = new File("target/testdata/" + testName);
    if (testDir.exists()) {
      testDir.delete();
    }
    testDir.mkdirs();
    return testDir;
  }

}
