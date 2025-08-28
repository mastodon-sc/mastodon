package org.mastodon.app.ui;

import java.util.ArrayList;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.app.AppModel;
import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.spatial.HasTimepoint;

public class MastodonView2<
		AM extends AppModel< ?, ?, MV, ME >,
		M extends MastodonModel< ?, MV, ME >,
		VG extends ViewGraph< MV, ME, V, E >,
		MV extends AbstractListenableVertex< MV, ME, ?, ? > & HasTimepoint,
		ME extends AbstractListenableEdge< ME, MV, ?, ? >,
		V extends Vertex< E >,
		E extends Edge< V > >
{

	protected final AM model;

	protected final VG viewGraph;

	protected final GroupHandle groupHandle;

	protected final TimepointModel timepointModel;

	protected final HighlightModel< V, E > highlightModel;

	protected final FocusModel< V > focusModel;

	protected final SelectionModel< V, E > selectionModel;

	protected final NavigationHandler< V, E > navigationHandler;

	protected final ArrayList< Runnable > runOnClose;

	public MastodonView2( final AM model, final VG viewGraph )
	{
		this.model = model;
		this.viewGraph = viewGraph;

		this.groupHandle = model.uiModel().getGroupManager().createGroupHandle();

		final RefBimap< MV, V > vertexMap = viewGraph.getVertexMap();
		final RefBimap< ME, E > edgeMap = viewGraph.getEdgeMap();
		final TimepointModelAdapter timepointModelAdapter = new TimepointModelAdapter( groupHandle.getModel( model.uiModel().TIMEPOINT ) );
		final HighlightModelAdapter< MV, ME, V, E > highlightModelAdapter = new HighlightModelAdapter<>( model.dataModel().getHighlightModel(), vertexMap, edgeMap );
		final FocusModelAdapter< MV, ME, V, E > focusModelAdapter = new FocusModelAdapter<>( model.dataModel().getFocusModel(), vertexMap, edgeMap );
		final SelectionModelAdapter< MV, ME, V, E > selectionModelAdapter = new SelectionModelAdapter<>( model.dataModel().getSelectionModel(), vertexMap, edgeMap );
		@SuppressWarnings( { "rawtypes", "unchecked" } )
		final NavigationHandlerAdapter< MV, ME, V, E > navigationHandlerAdapter = new NavigationHandlerAdapter( groupHandle.getModel( model.uiModel().NAVIGATION ), vertexMap, edgeMap );

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
