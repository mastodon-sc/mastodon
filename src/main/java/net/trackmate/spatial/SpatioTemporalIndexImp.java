package net.trackmate.spatial;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.imglib2.RealLocalizable;
import net.trackmate.Ref;
import net.trackmate.RefPool;
import net.trackmate.collection.RefList;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.graph.zzgraphinterfaces.CollectionUtils;
import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.Vertex;

/**
 * Maintain a spatio-temporal index of all the vertices of a graph.
 * <p>
 * This class specializes for vertices that are {@link RealLocalizable} for
 * spatial searches and partitioning. The temporal information is fetched
 * directly from the vertices themselves, which should therefore implement the
 * {@link HasTimepoint} interface. Finally, the vertices are pool objects and
 * implement the {@link Ref} interface.
 * <p>
 * TODO: figure out locking and locking API.
 *
 * @param <V>
 *            the type of the vertices in the graph.
 * @param <E>
 *            the type of the edges in the graph.
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

	/**
	 * Creates a new spatio-temporal index for the specified graph, using the
	 * specified vertex pool. The temporal information is fetched directly from
	 * the vertices.
	 * <p>
	 * At construction, this instance registers as a listener of the specified
	 * graph and updates itself following changes in the graph. When this
	 * constructor returns, the spatio-temporal index can be immediately used.
	 *
	 * @param graph
	 *            the graph to build the spatio-temporal index for.
	 * @param vertexPool
	 *            the {@link RefPool} of the vertices of the graph.
	 */
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
		return getSpatialIndexImp( timepoint );
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
			getSpatialIndexImp( vertex.getTimepoint() ).add( vertex );
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

	private SpatialIndexImp< V > getSpatialIndexImp( final int timepoint )
	{
		SpatialIndexImp< V > index = timepointToSpatialIndex.get( timepoint );
		if ( index == null )
		{
			index = new SpatialIndexImp< V >( CollectionUtils.createVertexSet( graph ), vertexPool );
			timepointToSpatialIndex.put( timepoint, index );
		}
		return index;
	}
}
