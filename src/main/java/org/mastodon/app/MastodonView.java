package org.mastodon.app;

import static org.mastodon.app.ui.MastodonViewStateSerialization.VIEW_TYPE_KEY;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.app.ui.MastodonViewStateSerialization;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.revised.model.AbstractSpot;

/**
 *
 * @param <M>
 * @param <VG>
 * @param <MV>
 *            model vertex type
 * @param <ME>
 *            model edge type
 * @param <V>
 *            view vertex type
 * @param <E>
 *            view edge type
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

		final TimepointModelAdapter timepointModelAdapter = new TimepointModelAdapter( groupHandle.getModel( appModel.TIMEPOINT ) );
		final HighlightModelAdapter< MV, ME, V, E > highlightModelAdapter = new HighlightModelAdapter<>( appModel.getHighlightModel(), vertexMap, edgeMap );
		final FocusModelAdapter< MV, ME, V, E > focusModelAdapter = new FocusModelAdapter<>( appModel.getFocusModel(), vertexMap, edgeMap );
		final SelectionModelAdapter< MV, ME, V, E > selectionModelAdapter = new SelectionModelAdapter<>( appModel.getSelectionModel(), vertexMap, edgeMap );
		final NavigationHandlerAdapter< MV, ME, V, E > navigationHandlerAdapter = new NavigationHandlerAdapter<>( groupHandle.getModel( appModel.NAVIGATION ), vertexMap, edgeMap );

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
		});
	}

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
	 * Returns the state of this view (position, size, etc.).
	 * 
	 * @return a new {@link MastodonViewStateSerialization} instance.
	 */
	public Map< String, Object > getGUIState()
	{
		final Map< String, Object > state = new LinkedHashMap<>();
		state.put( VIEW_TYPE_KEY, getClass().getSimpleName() );
		return state;
	}

	public void setGUIState( final Map< String, Object > guiState )
	{
		// Nothing to do, we have no state.
	}
}
