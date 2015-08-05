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
import net.trackmate.graph.collection.RefSet;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeVertex;

class ContextTrackScheme< V extends OverlayVertex< V, E > & HasTrackSchemeVertex, E extends OverlayEdge< E, V > >
{
	public static final int DEFAULT_CONTEXT_WINDOW = 10;

	private final OverlayGraph< V, E > graph;

	private final ShowTrackScheme trackscheme;

	private final PoolObjectList< TrackSchemeVertex > roots;

	private int contextWindow = DEFAULT_CONTEXT_WINDOW;

	private double focusRange = TracksOverlaySpotCovariance.DEFAULT_LIMIT_FOCUS_RANGE;

	private boolean useCrop;

	public ContextTrackScheme(
			final OverlayGraph< V, E > graph,
			final ShowTrackScheme trackScheme )
	{
		this.graph = graph;
		this.trackscheme = trackScheme;
		roots = trackScheme.getGraph().createVertexList();
	}

	public void buildContext(
			final int timepoint,
			final AffineTransform3D viewerTransform,
			final int width,
			final int height )
	{
		final int minTimepoint = timepoint - contextWindow;
		final int maxTimepoint = timepoint + contextWindow;

		// mark vertices in crop region with timestamp and find roots.
		final int mark = trackscheme.getNewLayoutTimestamp();
		roots.clear();
		if ( useCrop )
		{
			final ConvexPolytope crop = new ConvexPolytope(
					new HyperPlane( 0, 0, 1, -focusRange ),
					new HyperPlane( 0, 0, -1, -focusRange ),
					new HyperPlane( 1, 0, 0, 0 ),
					new HyperPlane( -1, 0, 0, -width ),
					new HyperPlane( 0, 1, 0, 0 ),
					new HyperPlane( 0, -1, 0, -height ) );
			final ConvexPolytope tcrop = ConvexPolytope.transform( crop, viewerTransform.inverse() );
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
		}
		else
		{
			for ( int t = minTimepoint; t <= maxTimepoint; ++t )
			{
				final RefSet< V > spots = graph.getSpots( t );
				if ( spots != null )
				{
					for ( final V v : spots )
					{
						final TrackSchemeVertex tv = v.getTrackSchemeVertex();
						tv.setLayoutTimestamp( mark );
						if ( t == minTimepoint || tv.incomingEdges().isEmpty() )
							roots.add( tv );
					}
				}
			}
		}

		roots.getIndexCollection().sort(); // TODO sort roots by something
											// meaningful...

		// layout and repaint
		trackscheme.relayout( roots, mark );
	}

	public void setContextWindow( final int contextWindow )
	{
		this.contextWindow = contextWindow;
	}

	public void setFocusRange( final double focusRange )
	{
		this.focusRange = focusRange;
	}

	public void setUseCrop( final boolean useCrop )
	{
		this.useCrop = useCrop;
	}

	public static < V extends OverlayVertex< V, E > & HasTrackSchemeVertex, E extends OverlayEdge< E, V > >
			ContextTrackScheme< V, E > create(
					final OverlayGraph< V, E > graph,
					final ShowTrackScheme trackScheme )
	{
		return new ContextTrackScheme< V, E >( graph, trackScheme );
	}


}