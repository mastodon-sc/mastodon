package org.mastodon.views.bvv.wrap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;

public class BvvSpatioTemporalIndexWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements SpatioTemporalIndex< BvvVertexWrapper< V, E > >
{
	private final BvvGraphWrapper< V, E > graphWrapper;

	private final SpatioTemporalIndex< V > wrappedIndex;

	public BvvSpatioTemporalIndexWrapper( final BvvGraphWrapper< V, E > graphWrapper, final SpatioTemporalIndex< V > index )
	{
		this.graphWrapper = graphWrapper;
		this.wrappedIndex = index;
	}

	@Override
	public Iterator< BvvVertexWrapper< V, E > > iterator()
	{
		return new BvvVertexIteratorWrapper< V, E >( graphWrapper, graphWrapper.vertexRef(), wrappedIndex.iterator() );
	}

	@Override
	public Lock readLock()
	{
		return wrappedIndex.readLock();
	}

	@Override
	public SpatialIndex< BvvVertexWrapper< V, E > > getSpatialIndex( final int timepoint )
	{
		final SpatialIndex< V > index = wrappedIndex.getSpatialIndex( timepoint );
		if ( index == null )
			return null;
		else
			return new BvvSpatialIndexWrapper< V, E >( graphWrapper, index );
	}

	@Override
	public SpatialIndex< BvvVertexWrapper< V, E > > getSpatialIndex( final int fromTimepoint, final int toTimepoint )
	{
		final SpatialIndex< V > index = wrappedIndex.getSpatialIndex( fromTimepoint, toTimepoint );
		if ( index == null )
			return null;
		else
			return new BvvSpatialIndexWrapper< V, E >( graphWrapper, index );
	}
}
