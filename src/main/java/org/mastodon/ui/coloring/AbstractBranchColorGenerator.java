package org.mastodon.ui.coloring;

import org.mastodon.adapter.RefBimap;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

/**
 * Mother class for color generators that return a color for a vertex based on
 * feature defined for a branch vertex 'upward' or 'downward' in time.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertex to color with this generator.
 * @param the
 *            type of the associated edge in the core graph.
 * @param <BV>
 *            the type of vertices in he branch graph.
 */
public abstract class AbstractBranchColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV >
{

	protected final FeatureColorGenerator< BV > colorGenerator;

	protected final RefBimap< V, BV > mapping;

	protected final ReadOnlyGraph< V, E > graph;

	public AbstractBranchColorGenerator(
			final FeatureProjection< BV > featureProjection,
			final RefBimap< V, BV > mapping,
			final ReadOnlyGraph< V, E > graph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		this.graph = graph;
		this.colorGenerator = new FeatureColorGenerator<>( featureProjection, colorMap, min, max );
		this.mapping = mapping;
	}

	protected final int downwardFromVertex( final V v )
	{
		final BV bvref = mapping.reusableRightRef();
		final E eref = graph.edgeRef();
		try
		{
			final BV o = mapping.getRight( v, bvref );
			if ( o == null )
			{
				// Iterate till we find a linked branch vertex.
				Edges< E > ie = v.outgoingEdges();
				while ( ie.size() == 1 )
				{
					final V source = ie.get( 0, eref ).getTarget( mapping.reusableLeftRef( bvref ) );
					final BV obj = mapping.getRight( source, bvref );
					if ( obj != null )
						return colorGenerator.color( obj );

					ie = source.outgoingEdges();
				}
				return 0;
			}
			return colorGenerator.color( o );
		}
		finally
		{
			mapping.releaseRef( bvref );
			graph.releaseRef( eref );
		}
	}

	protected final int upwardFromVertex( final V v )
	{
		final BV bvref = mapping.reusableRightRef();
		final E eref = graph.edgeRef();
		try
		{
			final BV o = mapping.getRight( v, bvref );
			if ( o == null )
			{
				// Iterate till we find a linked branch vertex.
				Edges< E > ie = v.incomingEdges();
				while ( ie.size() == 1 )
				{
					final V source = ie.get( 0, eref ).getSource( mapping.reusableLeftRef( bvref ) );
					final BV obj = mapping.getRight( source, bvref );
					if ( obj != null )
						return colorGenerator.color( obj );

					ie = source.incomingEdges();
				}
				return 0;
			}
			return colorGenerator.color( o );
		}
		finally
		{
			mapping.releaseRef( bvref );
			graph.releaseRef( eref );
		}
	}
}
