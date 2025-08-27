package org.mastodon.model;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.spatial.SpatioTemporalIndex;

import net.imglib2.RealLocalizable;

public interface MastodonModel<
		G extends ListenableGraph< V, E > ,
		V extends AbstractListenableVertex< V, E, ?, ? > & HasTimepoint & RealLocalizable,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{

	/**
	 * Exposes the key to the navigation model in the GroupHandle of this model.
	 *
	 * @return the navigation model key.
	 */
	GroupableModelFactory< NavigationHandler< V, E > > getNavigationGroupKey();

	/**
	 * Exposes the key to the time-point model in the GroupHandle of this model.
	 *
	 * @return the time-point model key.
	 */
	GroupableModelFactory< TimepointModel > getTimepointGroupKey();

	/**
	 * Exposes the graph managed by this model.
	 *
	 * @return the graph.
	 */
	G getGraph();

	/**
	 * Exposes the bidirectional map between vertices and their id, and between
	 * edges and their id.
	 *
	 * @return the bidirectional id map.
	 */
	GraphIdBimap< V, E > getGraphIdBimap();

	/**
	 * Exposes the tag-set model that can be used to tag vertices and edges of
	 * the graph managed in this model.
	 *
	 * @return the tag-set model.
	 */
	TagSetModel< V, E > getTagSetModel();

	/**
	 * Exposes the spatio-temporal index of this model.
	 *
	 * @return the spatio-temporal index.
	 */
	SpatioTemporalIndex< V > getSpatioTemporalIndex();

	/**
	 * Exposes the highlight of this model.
	 *
	 * @return the highlight model.
	 */
	HighlightModel< V, E > getHighlightModel();

	/**
	 * Exposes the focus of this model.
	 *
	 * @return the focus model.
	 */
	FocusModel< V > getFocusModel();

	/**
	 * Exposes the selection of this model.
	 *
	 * @return the selection model.
	 */
	SelectionModel< V, E > getSelectionModel();

	/**
	 * Exposes the group manager of this model.
	 *
	 * @return the group manager.
	 */
	GroupManager getGroupManager();

}
