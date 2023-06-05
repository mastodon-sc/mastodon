/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.ref.WeakReference;

import org.junit.Test;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.util.GarbageCollectionUtils;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

/**
 * Test is Mastodon is properly garbage collected after it is closed.
 * <p>
 * Mastodon not being garbage collected is a problem for users.
 * As opening many Mastodon projects in Fiji in a row will eventually
 * lead to an OutOfMemoryError.
 */
public class GarbageCollectionTest
{
	static String exampleProject = GarbageCollectionDemo.class.getResource( "examples/tiny/tiny-project.mastodon" ).getFile();

	/**
	 * Test if Mastodon can be garbage collected.
	 * <p>
	 * The test will fail if there is a memory leak in Mastodon that prevents
	 * the Mastodon data structures from being garbage collected.
	 * There are many possible reasons for this:
	 * <ul>
	 *     <li>A thread never stops and keeps a reference to Mastodon data structures.</li>
	 *     <li>A JFrame that's not disposed and keeps a reference to Mastodon data structures.</li>
	 *     <li>A static reference / a singleton object that keeps a reference to Mastodon data structures.</li>
	 *     <li>...</li>
	 * </ul>
	 * <p>
	 * Memory leaks are hard to debug without proper tools.
	 * But using Eclipse Memory Analyzer to analyze a heap dump of the
	 * {@link GarbageCollectionDemo} should reveal the problem.
	 */
	@Test
	public void testIfMastodonIsGarbageCollectable()
	{
		assumeFalse( "Skip test for memory leaks in the Mastodon UI. (running in headless mode)",
				GraphicsEnvironment.isHeadless() );

		try( Context context = new Context() ) {
			WeakReference< ModelGraph > modelGraph = openAndCloseMastodon( context );
			GarbageCollectionUtils.triggerGarbageCollection();
			assertNull( "The garbage collection failed to clean ModelGraph.", modelGraph.get() );
		}
	}

	/**
	 * Open a Mastodon project with all different windows and close it.
	 * Return a weak reference to the ModelGraph.
	 */
	static WeakReference< ModelGraph > openAndCloseMastodon( Context context )
	{
		try
		{
			WindowManager windowManager = new WindowManager( context );
			MamutProject project = new MamutProjectIO().load( exampleProject );
			windowManager.getProjectManager().open( project, false, true );
			ModelGraph modelGraph = windowManager.getAppModel().getModel().getGraph();
			MainWindow mainWindow = new MainWindow( windowManager );
			mainWindow.setVisible( true );
			windowManager.createTrackScheme();
			windowManager.createBranchTrackScheme();
			windowManager.createHierarchyTrackScheme();
			windowManager.createBigDataViewer();
			windowManager.createBranchBigDataViewer();
			windowManager.createGrapher();
			windowManager.createTable( false );
			windowManager.createTable( true );
			windowManager.editTagSets();
			mainWindow.close( windowManager, windowManager.getGlobalAppActions().getActionMap().get( ProjectManager.SAVE_PROJECT ), null );
			return new WeakReference<>( modelGraph );
		}
		catch ( IOException | SpimDataException e )
		{
			throw new RuntimeException( e );
		}
	}
}