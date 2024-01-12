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

import java.io.IOException;

import javax.swing.WindowConstants;

import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.tag.TagSetModel;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MastodonUtils
{

	private static final boolean LOG_STACK_TRACE = false;

	private MastodonUtils()
	{
		// prevent from instantiation
	}

	static Model openMastodonModel( final Context context, final String projectPath )
	{
		try
		{
			final MamutProject project = MamutProjectIO.load( projectPath );
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			final boolean isNewProject = project.getProjectRoot() == null;
			if ( !isNewProject )
			{
				try (final MamutProject.ProjectReader reader = project.openForReading())
				{
					final RawGraphIO.FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
					// Load features.
					MamutRawFeatureModelIO.deserialize( context, model, idmap, reader );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			return model;
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static WindowManager showGui( final String projectPath )
	{
		try
		{
			final ProjectModel appModel = ProjectLoader.open( MamutProjectIO.load( projectPath ), new Context() );
			final MainWindow mainWindow = new MainWindow( appModel );
			mainWindow.setVisible( true );
			mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
			return appModel.getWindowManager();
		}
		catch ( IOException | SpimDataException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static void logMastodonEvents( final ProjectModel appModel )
	{
		final GroupHandle groupHandle = appModel.getGroupManager().createGroupHandle();
		groupHandle.setGroupId( 0 );
		logNavigationHandle( groupHandle.getModel( appModel.NAVIGATION ) );
		logTimePointModel( groupHandle.getModel( appModel.TIMEPOINT ) );
		logFocusModel( appModel );
		logTagSetModel( appModel );
	}

	private static void logFocusModel( final ProjectModel appModel )
	{
		final FocusModel< Spot > focusModel = appModel.getFocusModel();
		final ModelGraph graph = appModel.getModel().getGraph();
		focusModel.listeners().add( () -> {
			final Spot ref = graph.vertexRef();
			final Spot focusedSpot = focusModel.getFocusedVertex( ref );
			log( "FocusModel: focused vertex: " + focusedSpot );
			graph.releaseRef( ref );
		} );
	}

	private static void logNavigationHandle( final NavigationHandler< Spot, Link > navigationHandler )
	{
		navigationHandler.listeners().add( new NavigationListener< Spot, Link >()
		{
			@Override
			public void navigateToVertex( final Spot vertex )
			{
				log( "NavigationHandler: navigate to vertex " + vertex );
			}

			@Override
			public void navigateToEdge( final Link edge )
			{
				log( "NavigationHandler: navigate to edge " + edge );
			}
		} );
	}

	private static void logTimePointModel( final TimepointModel model )
	{
		model.listeners().add( () -> log( "Time point changed: (to " + model.getTimepoint() + ")" ) );
	}

	private static void logTagSetModel( final ProjectModel appModel )
	{
		// TODO
		final Model model = appModel.getModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		tagSetModel.listeners().add( () -> log( "tag set changed" ) );
	}

	private static void log( final String text )
	{
		System.out.println( text + " " + Thread.currentThread().getName() );
		if ( LOG_STACK_TRACE )
			for ( final StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace() )
				System.out.println( "   " + stackTraceElement );
	}
}
