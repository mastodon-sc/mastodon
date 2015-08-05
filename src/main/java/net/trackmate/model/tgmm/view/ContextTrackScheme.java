package net.trackmate.model.tgmm.view;

import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.bdv.wrapper.HasTrackSchemeVertex;
import net.trackmate.bdv.wrapper.OverlayEdge;
import net.trackmate.bdv.wrapper.OverlayGraph;
import net.trackmate.bdv.wrapper.OverlayVertex;
import net.trackmate.bdv.wrapper.SpatialSearch;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeVertex;

class ContextTrackScheme< V extends OverlayVertex< V, E > & HasTrackSchemeVertex, E extends OverlayEdge< E, V > >
{
	private final OverlayGraph< V, E > graph;

	private final ShowTrackScheme trackScheme;

	private final PoolObjectList< TrackSchemeVertex > roots;

	public ContextTrackScheme(
			final OverlayGraph< V, E > graph,
			final ShowTrackScheme trackScheme )
	{
		this.graph = graph;
		this.trackScheme = trackScheme;
		roots = trackScheme.getGraph().createVertexList();
	}

	public void buildContext(
			final int timepoint,
			final AffineTransform3D viewerTransform,
			final int width,
			final int height )
	{
		final int depth = 200;
		final int minTimepoint = timepoint - 2;
		final int maxTimepoint = timepoint + 2;

		final ConvexPolytope crop = new ConvexPolytope(
				new HyperPlane( 0, 0, 1, -depth ),
				new HyperPlane( 0, 0, -1, -depth ),
				new HyperPlane( 1, 0, 0, 0 ),
				new HyperPlane( -1, 0, 0, -width ),
				new HyperPlane( 0, 1, 0, 0 ),
				new HyperPlane( 0, -1, 0, -height ) );
		final ConvexPolytope tcrop = ConvexPolytope.transform( crop, viewerTransform.inverse() );

		final int mark = trackScheme.getNewLayoutTimestamp();

		// mark vertices in crop region with timestamp and find roots.
		roots.clear();
		for ( int t = minTimepoint; t <= maxTimepoint; ++t )
		{
			final SpatialSearch< V > search = graph.getSpatialSearch( t );
			if ( search != null )
			{
				search.clip( tcrop );
				for ( final V v : search.getInsideVertices() )
				{
					final TrackSchemeVertex tv = v.getTrackSchemeVertex();
					tv.setLayoutTimestamp( mark );
					if ( t == minTimepoint || tv.incomingEdges().isEmpty() )
						roots.add( tv );
				}
			}
		}
		roots.getIndexCollection().sort(); // TODO sort roots by something
											// meaningful...

		// layout and repaint
		trackScheme.relayout( roots, mark );
	}

	public static < V extends OverlayVertex< V, E > & HasTrackSchemeVertex, E extends OverlayEdge< E, V > >
			ContextTrackScheme< V, E > create(
					final OverlayGraph< V, E > graph,
					final ShowTrackScheme trackScheme )
	{
		return new ContextTrackScheme< V, E >( graph, trackScheme );
	}
}