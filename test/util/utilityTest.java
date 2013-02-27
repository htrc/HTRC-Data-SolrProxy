/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: solr
# File:  utilityTest.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package util;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class utilityTest {

	@Test
	public void testEscape() {
		String id = "uc2.ark:/13960/t3dz03v4c";
		
		assertEquals("uc2.ark\\:/13960/t3dz03v4c", utility.escape(id));
	}

	@Ignore
	@Test
	public void testDelete() {
		// not used for now since the result is streamed back to user instead of
		// creating a zip file on server side and transferrig it back to client
		fail("Not yet implemented");
	}
	@Ignore
	@Test
	public void testZipDir() {
		// not used for now since the result is streamed back to user instead of
		// creating a zip file on server side and transferrig it back to client
		fail("Not yet implemented");
	}
	@Ignore
	@Test
	public void testUnzip() {
		// not used for now since the result is streamed back to user instead of
		// creating a zip file on server side and transferrig it back to client
		fail("Not yet implemented");
	}

}
