package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Mother class for color generators that return a color for a vertex based on
 * feature defined for a branch vertex 'upward' or 'downward' in time.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertex in the core graph.
 * @param <E>
 *            the type of edge in the core graph.
 * @param <BV>
 *            the type of vertices in he branch graph.
 * @param <BE>
 *            the type of vertices in he branch graph.
 */
public abstract class AbstractBranchVertexColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
{

	protected final FeatureColorGenerator< BV > colorGenerator;

	protected final BranchGraph< BV, BE, V, E > branchGraph;

	public AbstractBranchVertexColorGenerator(
			final FeatureProjection< BV > featureProjection,
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		this.branchGraph = branchGraph;
		this.colorGenerator = new FeatureColorGenerator<>( featureProjection, colorMap, min, max );
	}

	protected final int downwardFromVertex( final V v )
	{
		final BV bvref = branchGraph.vertexRef();
		try
		{

			final BV bv = branchGraph.getBranchVertex( v, bvref );
			if ( bv != null )
				return colorGenerator.color( bv );

			final BE beref = branchGraph.edgeRef();
			try
			{
				final BE be = branchGraph.getBranchEdge( v, beref );
				if ( be == null )
					return 0;

				final BV target = be.getTarget( bvref );
				return colorGenerator.color( target );
			}
			finally
			{
				branchGraph.releaseRef( beref );
			}
		}
		finally
		{
			branchGraph.releaseRef( bvref );
		}
	}

	protected final int upwardFromVertex( final V v )
	{
		final BV bvref = branchGraph.vertexRef();
		try
		{

			final BV bv = branchGraph.getBranchVertex( v, bvref );
			if ( bv != null )
				return colorGenerator.color( bv );

			final BE beref = branchGraph.edgeRef();
			try
			{
				final BE be = branchGraph.getBranchEdge( v, beref );
				if ( be == null )
					return 0;

				final BV target = be.getSource( bvref );
				return colorGenerator.color( target );
			}
			finally
			{
				branchGraph.releaseRef( beref );
			}
		}
		finally
		{
			branchGraph.releaseRef( bvref );
		}
	}
}
