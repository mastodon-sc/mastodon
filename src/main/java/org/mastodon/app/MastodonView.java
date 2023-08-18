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
package org.mastodon.app;

import java.util.ArrayList;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.AbstractSpot;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;

/**
 * Mother class for generic mastodon views. Offer facilities to manage model
 * objects (selection, highlight, focus, time-point) and navigation (navigation,
 * group-handle).
 *
 * @param <M>
 *            the type of the mastodon-app model.
 * @param <VG>
 *            the type of the view-graph displayed in this view.
 * @param <MV>
 *            model vertex type.
 * @param <ME>
 *            model edge type.
 * @param <V>
 *            view vertex type.
 * @param <E>
 *            view edge type.
 *
 * @author Tobias Pietzsch
 */
public class MastodonView<
		M extends MastodonAppModel< ?, MV, ME >,
		VG extends ViewGraph< MV, ME, V, E >,
		MV extends AbstractSpot< MV, ME, ?, ?, ? >,
		ME extends AbstractListenableEdge< ME, MV, ?, ? >,
		V extends Vertex< E >,
		E extends Edge< V > >
{
	protected final M appModel;

	protected VG viewGraph;

	protected final GroupHandle groupHandle;

	protected final TimepointModel timepointModel;

	protected final HighlightModel< V, E > highlightModel;

	protected final FocusModel< V, E > focusModel;

	protected final SelectionModel< V, E > selectionModel;

	protected final NavigationHandler< V, E > navigationHandler;

	protected final ArrayList< Runnable > runOnClose;

	public MastodonView(
			final M appModel,
			final VG viewGraph )
	{
		this.appModel = appModel;
		this.viewGraph = viewGraph;

		final RefBimap< MV, V > vertexMap = viewGraph.getVertexMap();
		final RefBimap< ME, E > edgeMap = viewGraph.getEdgeMap();

		groupHandle = appModel.getGroupManager().createGroupHandle();

		final TimepointModelAdapter timepointModelAdapter =
				new TimepointModelAdapter( groupHandle.getModel( appModel.TIMEPOINT ) );
		final HighlightModelAdapter< MV, ME, V, E > highlightModelAdapter =
				new HighlightModelAdapter<>( appModel.getHighlightModel(), vertexMap, edgeMap );
		final FocusModelAdapter< MV, ME, V, E > focusModelAdapter =
				new FocusModelAdapter<>( appModel.getFocusModel(), vertexMap, edgeMap );
		final SelectionModelAdapter< MV, ME, V, E > selectionModelAdapter =
				new SelectionModelAdapter<>( appModel.getSelectionModel(), vertexMap, edgeMap );
		final NavigationHandlerAdapter< MV, ME, V, E > navigationHandlerAdapter =
				new NavigationHandlerAdapter<>( groupHandle.getModel( appModel.NAVIGATION ), vertexMap, edgeMap );

		timepointModel = timepointModelAdapter;
		highlightModel = highlightModelAdapter;
		focusModel = focusModelAdapter;
		selectionModel = selectionModelAdapter;
		navigationHandler = navigationHandlerAdapter;

		runOnClose = new ArrayList<>();
		runOnClose.add( () -> {
			timepointModelAdapter.listeners().removeAll();
			highlightModelAdapter.listeners().removeAll();
			focusModelAdapter.listeners().removeAll();
			selectionModelAdapter.listeners().removeAll();
			navigationHandlerAdapter.listeners().removeAll();
		} );
	}

	/**
	 * Adds the specified {@link Runnable} to the list of runnables to execute
	 * when this view is closed.
	 *
	 * @param runnable
	 *            the {@link Runnable} to add.
	 */
	public synchronized void onClose( final Runnable runnable )
	{
		runOnClose.add( runnable );
	}

	protected synchronized void close()
	{
		runOnClose.forEach( Runnable::run );
		runOnClose.clear();
	}

	/**
	 * Exposes the {@link GroupHandle} of this view.
	 * 
	 * @return the {@link GroupHandle} of this view.
	 */
	public GroupHandle getGroupHandle()
	{
		return groupHandle;
	}
}
