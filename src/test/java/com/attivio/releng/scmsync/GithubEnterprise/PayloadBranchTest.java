/**
 * Copyright 2016 Attivio Inc.,
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.attivio.releng.scmsync.GithubEnterprise;

import com.attivio.releng.scmsync.ScmSyncException;
import com.attivio.releng.scmsync.TestBase;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Tests for Branch Path Parse
 * Created by userb on 10/12/16.
 */
public class PayloadBranchTest extends TestBase {


  @Test
  public void happyPathBranchName() throws ScmSyncException {
    List<String> branchNames = new ArrayList<>(Arrays.asList("feature/PLAT-20394", "develop"));

    for (String branchName: branchNames) {
      Payload payload = new Payload();
      payload.setRef("refs/heads/" + branchName); // set ref
      assertEquals(branchName, payload.getBranchPath()); // check branch name
    }
  }

  @Test
  public void corruptRef() throws ScmSyncException {
    Payload payload = new Payload();
    payload.setRef("refs/badName"); // set bad ref

    // Set for Fail
    expectedException.expect(ScmSyncException.class);
    expectedException.expectMessage(new StringContains("Cannot locate branch path from refs/badName found: 1 - refs/badName"));

    payload.getBranchPath();

  }
}
