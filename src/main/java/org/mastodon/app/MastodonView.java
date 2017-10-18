package org.mastodon.app;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
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
 * @param <MV> model vertex type
 * @param <ME> model edge type
 * @param <V> view vertex type
 * @param <E> view edge type
 */
public class MastodonView<
		M extends MastodonAppModel< ?, MV, ME >,
		MV extends AbstractSpot< MV, ME, ?, ?, ? >,
		ME extends AbstractListenableEdge< ME, MV, ?, ? >,
		V extends Vertex< E >,
		E extends Edge< V > >
{
	// TODO: make all public fields protected

	public final M appModel;

	public final RefBimap< MV, V > vertexMap;

	public final RefBimap< ME, E > edgeMap;

	public final GroupHandle groupHandle;

	public final TimepointModel timepointModel;

	public final HighlightModel< V, E > highlightModel;

	public final FocusModel< V, E > focusModel;

	public final SelectionModel< V, E > selectionModel;

	public final NavigationHandler< V, E > navigationHandler;

	public MastodonView(
			final M appModel,
			final RefBimap< MV, V > vertexMap,
			final RefBimap< ME, E > edgeMap )
	{
		this.appModel = appModel;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;

		groupHandle = appModel.getGroupManager().createGroupHandle();
		timepointModel = groupHandle.getModel( appModel.TIMEPOINT );
		highlightModel = new HighlightModelAdapter<>( appModel.getHighlightModel(), vertexMap, edgeMap );
		focusModel = new FocusModelAdapter<>( appModel.getFocusModel(), vertexMap, edgeMap );
		selectionModel = new SelectionModelAdapter<>( appModel.getSelectionModel(), vertexMap, edgeMap );
		final NavigationHandler< MV, ME > navigation = groupHandle.getModel( appModel.NAVIGATION );
		navigationHandler = new NavigationHandlerAdapter<>( navigation, vertexMap, edgeMap );
	}
}
