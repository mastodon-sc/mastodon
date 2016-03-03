package net.trackmate.revised.trackscheme;

import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.context.Context;

public class ContextLayout
{
	private final TrackSchemeGraph< ?, ? > graph;

	/**
	 * layout the {@link TrackSchemeGraph} into layout coordinates.
	 */
	private final LineageTreeLayout layout;

	private int previousMinTimepoint;

	private int previousMaxTimepoint;

	public ContextLayout(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout )
	{
		this.graph = graph;
		this.layout = layout;
		this.previousMinTimepoint = -1;
		this.previousMaxTimepoint = -1;
	}

	/**
	 * Layout part of the graph covered by {@code context}.
	 * <p>
	 * The {@link ScreenTransform#getMinY()} and
	 * {@link ScreenTransform#getMaxY()} of {@code transform} determines the
	 * timepoint range to cover. If the timepoint range is the same as in the
	 * previous call nothing is updated, unless {@code forceUpdate == true}.
	 *
	 * @param context
	 * @param transform
	 * @param forceUpdate
	 * @return {@code true} if the layout was updated.
	 */
	public boolean buildContext(
			final Context< TrackSchemeVertex > context,
			final ScreenTransform transform,
			final boolean forceUpdate )
	{
		final int minTimepoint = ( int ) transform.getMinY();
		final int maxTimepoint = ( int ) transform.getMaxY() + 1;
		if ( minTimepoint == previousMinTimepoint && maxTimepoint == previousMaxTimepoint && !forceUpdate )
			return false;

		previousMinTimepoint = minTimepoint;
		previousMaxTimepoint = maxTimepoint;

		final int ghostmark = layout.nextLayoutTimestamp();
		final int mark = layout.nextLayoutTimestamp();
		final RefList< TrackSchemeVertex > roots = CollectionUtils.createVertexList( graph );

		context.readLock().lock();
		try
		{
			for ( int t = minTimepoint; t <= maxTimepoint; ++t )
			{
				for ( final TrackSchemeVertex tv : context.getInsideVertices( t ) )
				{
					tv.setLayoutTimestamp( mark );
					if ( t == minTimepoint )
						roots.add( tv );
					else
						buildContextTraceParents( tv, ghostmark, minTimepoint, roots );
				}
			}
		}
		finally
		{
			context.readLock().unlock();
		}

		layout.layout( LexicographicalVertexOrder.sort( graph, roots ), mark );

		return true;
	}

	/**
	 * Follow backwards along incoming edges until
	 * <ul>
	 * <li>(A) a vertex is reached that is already marked with ghostmark or
	 * mark, or
	 * <li>(B) vertex is reached that has timepoint <= minTimepoint.
	 * </ul>
	 *
	 * Mark all recursively visited vertices as ghosts. In case (B), add the
	 * final vertex to set of roots.
	 */
	private void buildContextTraceParents( final TrackSchemeVertex tv, final int ghostmark, final int minTimepoint, final RefList< TrackSchemeVertex > roots )
	{
		if( tv.incomingEdges().isEmpty() )
			roots.add( tv );
		else
		{
			final TrackSchemeVertex ref = graph.vertexRef();
			for ( final TrackSchemeEdge te : tv.incomingEdges() )
			{
				final TrackSchemeVertex parent = te.getSource( ref );
				if ( parent.getLayoutTimestamp() < ghostmark )
				{
					parent.setLayoutTimestamp( ghostmark );
					if ( parent.getTimepoint() <= minTimepoint )
						roots.add( parent );
					else
						buildContextTraceParents( parent, ghostmark, minTimepoint, roots );
				}
			}
			graph.releaseRef( ref );
		}
	}
}
