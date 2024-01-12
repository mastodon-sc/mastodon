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
package org.mastodon.mamut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.IOException;

import org.junit.Test;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class CloseListenerTest
{

	/**
	 * Test if {@link CloseListener} is called, when a Mastodon project is
	 * closed.
	 */
	@Test
	public void testCloseListeners() throws IOException, SpimDataException
	{
		assumeFalse( "This test requires a display.", GraphicsEnvironment.isHeadless() );
		try (Context context = new Context())
		{
			// setup
			final ProjectModel appModel = openTinyProject( context );
			final int[] counter = new int[] { 0 };
			appModel.projectClosedListeners().add( () -> counter[ 0 ]++ );
			// process
			appModel.close();

			// test
			assertEquals( 1, counter[ 0 ] );
		}
	}

	private static ProjectModel openTinyProject( final Context context ) throws IOException, SpimDataException
	{
		final String tinyProjectFile = CloseListenerTest.class.getResource( "/org/mastodon/mamut/examples/tiny/tiny-project.mastodon" ).getFile();
		final MamutProject project = MamutProjectIO.load( tinyProjectFile );
		final ProjectModel appModel = ProjectLoader.open( project, context );
		return appModel;
	}
}
