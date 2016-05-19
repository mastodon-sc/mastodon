package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.spatial.SpatialIndex;
import net.trackmate.spatial.SpatioTemporalIndex;

public class SpatioTemporalIndexWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements SpatioTemporalIndex< OverlayVertexWrapper< V, E > >
{
	private final OverlayGraphWrapper< V, E > graphWrapper;

	private final SpatioTemporalIndex< V > wrappedIndex;

	public SpatioTemporalIndexWrapper( final OverlayGraphWrapper< V, E > graphWrapper, final SpatioTemporalIndex< V > index )
	{
		this.graphWrapper = graphWrapper;
		this.wrappedIndex = index;
	}

	@Override
	public Iterator< OverlayVertexWrapper< V, E > > iterator()
	{
		return new OverlayVertexIteratorWrapper< V, E >( graphWrapper, graphWrapper.vertexRef(), wrappedIndex.iterator() );
	}

	@Override
	public Lock readLock()
	{
		return wrappedIndex.readLock();
	}

	@Override
	public SpatialIndex< OverlayVertexWrapper< V, E > > getSpatialIndex( final int timepoint )
	{
		final SpatialIndex< V > index = wrappedIndex.getSpatialIndex( timepoint );
		if ( index == null )
			return null;
		else
			return new SpatialIndexWrapper< V, E >( graphWrapper, index );
	}

	@Override
	public SpatialIndex< OverlayVertexWrapper< V, E > > getSpatialIndex( final int fromTimepoint, final int toTimepoint )
	{
		final SpatialIndex< V > index = wrappedIndex.getSpatialIndex( fromTimepoint, toTimepoint );
		if ( index == null )
			return null;
		else
			return new SpatialIndexWrapper< V, E >( graphWrapper, index );
	}
}
