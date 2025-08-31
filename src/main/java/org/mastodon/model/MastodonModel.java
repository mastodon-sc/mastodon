package org.mastodon.model;

import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.spatial.SpatioTemporalIndex;

public interface MastodonModel<
		G extends ReadOnlyGraph< V, E >,
		V extends Vertex< E >, 
		E extends Edge< V > >
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

	/**
	 * Exposes the feature model that can be used to manage features computed on
	 * the graph managed in this model.
	 * 
	 * @return the feature model.
	 */
	FeatureModel getFeatureModel();

}
