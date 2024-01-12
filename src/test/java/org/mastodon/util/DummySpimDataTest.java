/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import org.junit.Test;

/**
 * Tests {@link DummySpimData}.
 */
public class DummySpimDataTest
{

	@Test
	public void testTryCreate()
	{
		String path = "/test/some/path/x=10 y=20 z=30 t=40.dummy";
		AbstractSpimData< ? > spimData = DummySpimData.tryCreate( path );
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		BasicViewSetup setup = seq.getViewSetupsOrdered().get( 0 );
		long[] size = setup.getSize().dimensionsAsLongArray();
		assertArrayEquals( new long[] { 10, 20, 30 }, size );
		int numberOfTimepoints = seq.getTimePoints().size();
		assertEquals( 40, numberOfTimepoints );
	}

	@Test
	public void testFromSpimDataXml() throws SpimDataException
	{
		String xml = this.getClass().getResource( "/org/mastodon/mamut/examples/tiny-unknown-url/remote-dataset.xml" )
				.getPath();
		AbstractSpimData< ? > data = DummySpimData.fromSpimDataXml( xml );
		long[] size = data.getSequenceDescription().getViewSetups().get( 0 ).getSize().dimensionsAsLongArray();
		assertArrayEquals( new long[] { 2169, 2048, 988 }, size );
	}
}
