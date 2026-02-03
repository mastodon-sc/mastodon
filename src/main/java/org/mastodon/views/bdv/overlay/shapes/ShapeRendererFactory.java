package org.mastodon.views.bdv.overlay.shapes;

import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.bdv.overlay.OverlayEdge;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayVertex;
import org.mastodon.views.bdv.overlay.RenderSettings;
import org.mastodon.views.bdv.overlay.Visibilities;

/**
 * Interface for factories that can create shape renderers for BDV overlays.
 * This is used in the {@link OverlayGraphRenderer} to draw custom shapes there.
 *
 * @author Jean-Yves Tinevez
 */
public interface ShapeRendererFactory
{
	/**
	 * Creates a new vertex shape renderer for the given graph and models.
	 *
	 * @param <V>
	 *            the overlay vertex type
	 * @param <E>
	 *            the overlay edge type
	 * @param <R>
	 *            the type of render settings used by the renderer
	 * @param graph
	 *            the overlay graph
	 * @param highlight
	 *            the highlight model
	 * @param focus
	 *            the focus model
	 * @param selection
	 *            the selection model
	 * @param coloring
	 *            the graph coloring generator
	 * @param visibilities
	 *            the visibilities
	 * @return a new vertex shape renderer
	 */
	< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V >, R extends RenderSettings< R > > VertexShapeRenderer< V, E, R > createRenderer(
			final OverlayGraph< V, E > graph,
			final HighlightModel< V, E > highlight,
			final FocusModel< V > focus,
			final SelectionModel< V, E > selection,
			final GraphColorGenerator< V, E > coloring,
			final Visibilities< V, E > visibilities
	);
}
