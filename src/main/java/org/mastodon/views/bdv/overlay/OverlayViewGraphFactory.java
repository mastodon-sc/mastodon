package org.mastodon.views.bdv.overlay;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.views.bdv.overlay.shapes.BdvOverlayProperties;

/**
 * Factory interface for creating OverlayViewGraph instances.
 *
 * @author Jean-Yves Tinevez
 *
 */
public interface OverlayViewGraphFactory<
	M extends MastodonModel< ?, V, E >,
	P extends BdvOverlayProperties< V, E >,
	OV extends OverlayVertex< OV, OE >,
	OE extends OverlayEdge< OE, OV >,
	V extends Vertex< E >,
	E extends Edge< V > >
{

	/**
	 * Creates a view graph suitable to be used in a BDV overlay.
	 *
	 * @param model
	 *            the data model to base the view graph on.
	 * @param properties
	 *            the overlay properties that maps the data model to the overlay
	 *            graph.
	 * @return a new overlay graph.
	 */
	OverlayGraph< OV, OE, V, E > createViewGraph( M model, P properties );
}
