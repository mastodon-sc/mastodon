package org.mastodon.app.ui;

import java.util.ArrayList;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;

/**
 * Base class for views of a {@link MastodonModel}, that display a view-graph
 * derived from the model's graph.
 *
 * @param <M>
 *            the type of the mastodon model.
 * @param <VG>
 *            the type of the view-graph.
 * @param <MV>
 *            the type of vertices in the mastodon model.
 * @param <ME>
 *            the type of edges in the mastodon model.
 * @param <V>
 *            the type of vertices in the view-graph.
 * @param <E>
 *            the type of edges in the view-graph.
 */
public class AbstractMastodonView2<
		M extends MastodonModel< ?, MV, ME >,
		VG extends ViewGraph< MV, ME, V, E >,
		MV extends Vertex< ME >,
		ME extends Edge< MV >,
		V extends Vertex< E >,
		E extends Edge< V > > implements MastodonView2< M, VG, MV, ME, V, E >
{

	protected final M dataModel;

	protected final UIModel< ? > uiModel;

	protected final VG viewGraph;

	protected final GroupHandle groupHandle;

	protected final TimepointModel timepointModel;

	protected final HighlightModel< V, E > highlightModel;

	protected final FocusModel< V > focusModel;

	protected final SelectionModel< V, E > selectionModel;

	protected final NavigationHandler< V, E > navigationHandler;

	protected final ArrayList< Runnable > runOnClose;

	public AbstractMastodonView2(
			final M dataModel,
			final UIModel< ? > uiModel,
			final VG viewGraph )
	{
		this.dataModel = dataModel;
		this.uiModel = uiModel;
		this.viewGraph = viewGraph;
		this.groupHandle = uiModel.getGroupManager().createGroupHandle();

		final RefBimap< MV, V > vertexMap = viewGraph.getVertexMap();
		final RefBimap< ME, E > edgeMap = viewGraph.getEdgeMap();
		final TimepointModelAdapter timepointModelAdapter = new TimepointModelAdapter( groupHandle.getModel( uiModel.TIMEPOINT ) );
		final HighlightModelAdapter< MV, ME, V, E > highlightModelAdapter = new HighlightModelAdapter<>( dataModel.getHighlightModel(), vertexMap, edgeMap );
		final FocusModelAdapter< MV, ME, V, E > focusModelAdapter = new FocusModelAdapter<>( dataModel.getFocusModel(), vertexMap, edgeMap );
		final SelectionModelAdapter< MV, ME, V, E > selectionModelAdapter = new SelectionModelAdapter<>( dataModel.getSelectionModel(), vertexMap, edgeMap );
		@SuppressWarnings( { "rawtypes", "unchecked" } )
		final NavigationHandlerAdapter< MV, ME, V, E > navigationHandlerAdapter = new NavigationHandlerAdapter( groupHandle.getModel( uiModel.NAVIGATION ), vertexMap, edgeMap );

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

	@Override
	public synchronized void onClose( final Runnable runnable )
	{
		runOnClose.add( runnable );
	}

	@Override
	public synchronized void close()
	{
		runOnClose.forEach( Runnable::run );
		runOnClose.clear();
	}

	@Override
	public GroupHandle getGroupHandle()
	{
		return groupHandle;
	}
}
