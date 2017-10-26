package org.mastodon.app;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.app.ui.ViewFrame;
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
import org.mastodon.util.Listeners;

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

	protected final Listeners.List< ViewListener > listeners = new Listeners.SynchronizedList<>();

	protected ViewFrame frame;

	public MastodonView(
			final M appModel,
			final VG viewGraph )
	{
		this.appModel = appModel;
		this.viewGraph = viewGraph;

		final RefBimap< MV, V > vertexMap = viewGraph.getVertexMap();
		final RefBimap< ME, E > edgeMap = viewGraph.getEdgeMap();

		groupHandle = appModel.getGroupManager().createGroupHandle();
		timepointModel = groupHandle.getModel( appModel.TIMEPOINT );
		highlightModel = new HighlightModelAdapter<>( appModel.getHighlightModel(), vertexMap, edgeMap );
		focusModel = new FocusModelAdapter<>( appModel.getFocusModel(), vertexMap, edgeMap );
		selectionModel = new SelectionModelAdapter<>( appModel.getSelectionModel(), vertexMap, edgeMap );
		final NavigationHandler< MV, ME > navigation = groupHandle.getModel( appModel.NAVIGATION );
		navigationHandler = new NavigationHandlerAdapter<>( navigation, vertexMap, edgeMap );
	}

	public Listeners< ViewListener > listeners()
	{
		return listeners;
	}
}
