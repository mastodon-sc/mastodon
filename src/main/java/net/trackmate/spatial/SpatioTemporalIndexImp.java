package net.trackmate.spatial;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Ref;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;

/**
 * Maintain a spatio-temporal index of all vertices of a graph.
 *
 * TODO: figure out locking and locking API.
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class SpatioTemporalIndexImp<
		V extends Vertex< E > & Ref< V > & RealLocalizable & HasTimepoint,
		E extends Edge< V > >
	implements GraphListener< V, E >, SpatioTemporalIndex< V >
{
	/**
	 * Int value used to declare that the requested timepoint is not in a map.
	 * Timepoints are always >= 0, so -1 works...
	 */
	private final static int NO_ENTRY_KEY = -1;

	final TIntObjectHashMap< SpatialIndexImp< V > > timepointToSpatialIndex;

	private final ListenableGraph< V, E > graph;

	private final RefPool< V > vertexPool;

	private final Lock readLock;

    private final Lock writeLock;

	public SpatioTemporalIndexImp( final ListenableGraph< V, E > graph, final RefPool< V > vertexPool )
	{
		this.graph = graph;
		this.vertexPool = vertexPool;
		timepointToSpatialIndex = new TIntObjectHashMap< SpatialIndexImp< V > >( 10, 0.5f, NO_ENTRY_KEY );
		graph.addGraphListener( this );
		final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	    readLock = rwl.readLock();
	    writeLock = rwl.writeLock();
	    init();
	}

	private void init()
	{
		final TIntObjectHashMap< RefList< V > > timepointToVertices = new TIntObjectHashMap< RefList< V > >( 10, 0.5f, NO_ENTRY_KEY );
		for ( final V v : graph.vertices() )
		{
			RefList< V > vs = timepointToVertices.get( v.getTimepoint() );
			if ( vs == null )
			{
				vs = CollectionUtils.createVertexList( graph );
				timepointToVertices.put( v.getTimepoint(), vs );
			}
			vs.add( v );
		}

		final TIntObjectIterator< RefList< V > > i = timepointToVertices.iterator();
		while ( i.hasNext() )
		{
			i.advance();
			final int timepoint = i.key();
			final SpatialIndexImp< V > data = new SpatialIndexImp< V >( i.value(), vertexPool );
			timepointToSpatialIndex.put( timepoint, data );
		}
	}

	@Override
	public Lock readLock()
	{
		return readLock;
	}

	@Override
	public Iterator< V > iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpatialIndex< V > getSpatialIndex( final int timepoint )
	{
		return timepointToSpatialIndex.get( timepoint );
	}

	@Override
	public SpatialIndex< V > getSpatialIndex( final int fromTimepoint, final int toTimepoint )
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "not implemented yet" );
	}

	/**
	 * TODO: Rebuilding of SpatioTemporalIndex must be called periodically from somewhere!
	 *
	 * Rebuild one {@link SpatialIndexData} for which the
	 * {@link SpatialIndexData#modCount()} exceeds the specified maximum.
	 */
	void rebuildAny( final int maxModCount )
	{
		SpatialIndexImp< V > index = null;
		readLock.lock();
		try
		{
			final TIntObjectIterator< SpatialIndexImp< V > > i = timepointToSpatialIndex.iterator();
		    while( i.hasNext() )
		    {
		    	i.advance();
		    	index = i.value();
		    	if ( index.modCount() > maxModCount )
		    		break;
		    }
		}
		finally
		{
			readLock.unlock();
		}

		if ( index != null )
			index.rebuild();
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		writeLock.lock();
		try
		{
			final int tp = vertex.getTimepoint();
			SpatialIndexImp< V > index = timepointToSpatialIndex.get( tp );
			if ( index == null )
			{
				index = new SpatialIndexImp< V >( CollectionUtils.createVertexSet( graph ), vertexPool );
				timepointToSpatialIndex.put( tp, index );
			}
			index.add( vertex );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		writeLock.lock();
		try
		{
			final SpatialIndexImp< V > index = timepointToSpatialIndex.get( vertex.getTimepoint() );
			index.remove( vertex );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void edgeAdded( final E edge )
	{}

	@Override
	public void edgeRemoved( final E edge )
	{}

	@Override
	public void graphRebuilt()
	{
		writeLock.lock();
		try
		{
			init();
		}
		finally
		{
			writeLock.unlock();
		}
	}

//	@Override // TODO: should be implemented for some listener interface
	public void vertexPositionChanged( final V vertex )
	{
		vertexAdded( vertex );
	}
}
