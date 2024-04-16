/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.model;

import org.junit.Test;
import org.mastodon.mamut.model.Model;

/**
 * Tests {@link Model} to make sure there are no memory leaks.
 */
public class ModelGarbageCollectionTest
{
	/**
	 * Test if the {@link Model} can be properly garbage collection.
	 * <p>
	 * This test should fail, if there is a memory leak that prevents the
	 * {@link Model} or associated objects from being garbage collected.
	 * <p>
	 * Hint: The Eclipse Memory Analyzer is very useful for debugging
	 * memory leaks.
	 * <p>
	 * NB: At the point of writing this test. Only ~30 models would fit
	 * in the memory available for unit testing. So the test would
	 * fail to create more than 30 models, if there's GC problem.
	 * (There was for example a memory leak in mastodon-graph version
	 * 1.0.0-beta-23 that caused this test to fail.)
	 */
	@Test
	public void testGarbageCollection()
	{
		for ( int i = 0; i < 100; i++ )
			new Model();
	}

}
