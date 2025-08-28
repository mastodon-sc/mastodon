/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.app.ui.UIModel;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.spatial.HasTimepoint;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.KeymapManager;

/**
 * Data class that stores the data model and the application model of a Mastodon
 * application. The vertices are expected to have a timepoint.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 * @param <M>
 *            the type of the model used in the application.
 * @param <V>
 *            the type of vertices in the model graph.
 * @param <E>
 *            the type of edges in the model graph.
 */
public class AppModel<
		M extends MastodonModel< G, V, E >,
		G extends ListenableGraph< V, E >,
		V extends AbstractListenableVertex< V, E, ?, ? > & HasTimepoint,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{

	protected final M model;

	protected final UIModel uiModel;

	protected final Context context;

	/**
	 * Instantiate a new Mastodon-app model.
	 *
	 * @param numGroups
	 *            the number of groups to create in the group manager,
	 * @param model
	 *            the data model.
	 * @param keyPressedManager
	 *            the key-pressed manager.
	 * @param keymapManager
	 *            the keymap manager.
	 * @param plugins
	 *            the plugins.
	 * @param globalActions
	 *            the global actions.
	 * @param keyConfigContexts
	 *            keyconf contexts for appActions (actions that should be
	 *            available in all views)
	 */
	public AppModel(
			final Context context,
			final M model,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts,
			final int numGroups )
	{
		this.context = context;
		this.model = model;
		this.uiModel = new UIModel( numGroups, keyPressedManager, keymapManager, plugins, globalActions, keyConfigContexts );
	}

	public M dataModel()
	{
		return model;
	}

	public UIModel uiModel()
	{
		return uiModel;
	}

	public Context getContext()
	{
		return context;
	}

	public int getTimepointMin()
	{
		return minFromVertices( model.getGraph() );
	}

	public int getTimepointMax()
	{
		return maxFromVertices( model.getGraph() );
	}

	private int maxFromVertices( final G graph )
	{
		int max = Integer.MIN_VALUE;
		for ( final V v : graph.vertices() )
			if ( v.getTimepoint() > max )
				max = v.getTimepoint();
		return max == Integer.MIN_VALUE ? 10 : max;
	}

	private int minFromVertices( final G graph )
	{
		int min = Integer.MAX_VALUE;
		for ( final V v : graph.vertices() )
			if ( v.getTimepoint() < min )
				min = v.getTimepoint();
		return min == Integer.MAX_VALUE ? 0 : min;
	}
}
