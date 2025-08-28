package org.mastodon.model;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.spatial.SpatioTemporalIndex;

public interface MastodonModel<
		G extends ListenableGraph< V, E > ,
		V extends AbstractListenableVertex< V, E, ?, ? >,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{

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

}
