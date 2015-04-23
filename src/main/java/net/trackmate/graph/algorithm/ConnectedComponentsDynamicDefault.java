package net.trackmate.graph.algorithm;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;
import java.util.Set;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.listenable.GraphChangeEvent;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.graph.traversal.BreadthFirstSearch;
import net.trackmate.graph.traversal.SearchListener;
import net.trackmate.graph.util.TIntArrayDeque;

/**
 * No Holm & de Lichtenbertg, because our tracks will be trees 90% of the time.
 * Another class will do Holm & de Lichtenbertg.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the graph vertices iterated.
 * @param <E>
 *            the type of the graph edges iterated.
 */
public class ConnectedComponentsDynamicDefault< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E > implements GraphListener< V, E >
{
	private final TIntObjectHashMap< RefSet< V >> ccMap;

	private final IDProvider idProvider;

	private final BreadthFirstSearch< V, E > bfs;

	private final ConnectedComponentsDynamicDefault< V, E >.VertexFinderListener sl;

	public ConnectedComponentsDynamicDefault( final ListenableGraph< V, E > graph )
	{
		super( graph );
		this.ccMap = new TIntObjectHashMap< RefSet< V > >();
		this.idProvider = new IDProvider();
		this.bfs = new BreadthFirstSearch< V, E >( graph, false );
		this.sl = new VertexFinderListener();
		bfs.setTraversalListener( sl );
		graph.addGraphListener( this );
		init();
	}

	private void init()
	{
		final Set< RefSet< V >> ccs = new ConnectedComponents< V, E >( graph ).get();
		for ( final RefSet< V > cc : ccs )
		{
			ccMap.put( idProvider.next(), cc );
		}
	}

	@Override
	public void graphChanged( final GraphChangeEvent< V, E > event )
	{
		V source = vertexRef();
		V target = vertexRef();

		/*
		 * Process edges added first.
		 */

		if ( !event.getEdgeAdded().isEmpty() )
		{
			final Iterator< E > it = event.getEdgeAdded().iterator();
			while ( it.hasNext() )
			{
				final E edge = it.next();

				source = edge.getSource( source );
				target = edge.getTarget( target );
				final int sourceID = idOf( source );
				final int targetID = idOf( target );

				if ( sourceID < 0 && targetID < 0 )
				{
					// They build a new component.
					final RefSet< V > newCC = createVertexSet( 2 );
					newCC.add( source );
					newCC.add( target );
					ccMap.put( idProvider.next(), newCC );

				}
				else if ( sourceID >= 0 && targetID < 0 )
				{
					/*
					 * Source belong to a know cc, target not. We add target to
					 * the cc of source.
					 */
					ccMap.get( sourceID ).add( target );

				}
				else if ( sourceID < 0 && targetID >= 0 )
				{
					/*
					 * Target belong to a know cc, source not. We add source to
					 * the cc of target.
					 */
					ccMap.get( targetID ).add( source );

				}
				else if ( sourceID == targetID )
				{
					/*
					 * They both belong to the same cc already. We don't do
					 * anything since this does not change the CCs.
					 */

				}
				else
				{
					/*
					 * They do not belong to the same cc. The large cc eats the
					 * little one, which disappear.
					 */

					final int sourceSize = size( sourceID );
					final int targetSize = size( targetID );
					final int toDelete;
					final int toGrow;
					if ( sourceSize > targetSize )
					{
						toDelete = targetID;
						toGrow = sourceID;
					}
					else
					{
						toDelete = sourceID;
						toGrow = targetID;
					}

					final RefSet< V > removed = ccMap.remove( toDelete );
					ccMap.get( toGrow ).addAll( removed );
					idProvider.free( toDelete );
				}
			}
		}

		/*
		 * Process vertices removed.
		 */
		if ( !event.getVertexRemoved().isEmpty() )
		{
			final Iterator< V > it = event.getVertexRemoved().iterator();
			while ( it.hasNext() )
			{
				final V v = it.next();
				final int id = idOf( v );
				ccMap.get( id ).remove( v );
			}
		}

		/*
		 * Process edges removed now.
		 */

		if ( !event.getEdgeRemoved().isEmpty() )
		{
			final Iterator< E > it = event.getEdgeRemoved().iterator();
			while ( it.hasNext() )
			{
				final E edge = it.next();
				source = event.getPreviousEdgeSource( edge, source );
				target = event.getPreviousEdgeTarget( edge, target );

				final int sourceID = idOf( source );
				final int targetID = idOf( target );

				if ( sourceID >= 0 && targetID >= 0 )
				{
					/*
					 * Both source and target vertices still exist in the graph,
					 * so we can look around to see if we have 2 components. For
					 * instance, iterate from the source vertex and see if we
					 * meet target.
					 */

					final RefSet< V > candidateCC = createVertexSet();
					sl.setCandidateCC( candidateCC );

					sl.setTarget( target );
					bfs.start( source );

					if ( bfs.wasAborted() )
					{
						/*
						 * The target was found during search. So they still
						 * belong to the same cc, even after edge removal. We
						 * don't do anything.
						 */
						continue;
					}
					else
					{
						final int sizeSourceSide = candidateCC.size();
						if ( sizeSourceSide > 1 )
						{
							// Only add it if it is large enough
							ccMap.put( idProvider.next(), candidateCC );
						}

						final int sizeTargetSide = ccMap.get( sourceID ).size() - sizeSourceSide;
						if ( sizeTargetSide > 1 )
						{
							ccMap.get( sourceID ).removeAll( candidateCC );
						}
						else
						{
							// It became too small, remove it.
							ccMap.remove( sourceID );
							idProvider.free( sourceID );
						}
					}
				}
				else if ( sourceID < 0 && targetID >= 0 )
				{

					/*
					 * The source CC is not present in the graph anymore. We
					 * just have to create a new CC with what radiates from
					 * target, and remove what we found from its previous CC.
					 */
					final RefSet< V > candidateCC = createVertexSet();
					sl.setCandidateCC( candidateCC );

					sl.setTarget( null );
					bfs.start( target );

					final int size = candidateCC.size();
					if ( size > 1 )
					{
						// Only add it if it is large enough
						ccMap.put( idProvider.next(), candidateCC );
					}

					final int sizeOldCC = ccMap.get( targetID ).size() - size;
					if ( sizeOldCC > 1 )
					{
						ccMap.get( targetID ).removeAll( candidateCC );
					}
					else
					{
						// It became too small, remove it.
						ccMap.remove( targetID );
						idProvider.free( targetID );
					}
				}
				else
				{

					/*
					 * The target CC is not present in the graph anymore. We
					 * just have to create a new CC with what radiates from
					 * target, and remove what we found from its previous CC.
					 */
					final RefSet< V > candidateCC = createVertexSet();
					sl.setCandidateCC( candidateCC );

					sl.setTarget( null );
					bfs.start( source );

					final int size = candidateCC.size();
					if ( size > 1 )
					{
						// Only add it if it is large enough
						ccMap.put( idProvider.next(), candidateCC );
					}

					final int sizeOldCC = ccMap.get( sourceID ).size() - size;
					if ( sizeOldCC > 1 )
					{
						ccMap.get( sourceID ).removeAll( candidateCC );
					}
					else
					{
						// It became too small, remove it.
						ccMap.remove( sourceID );
						idProvider.free( sourceID );
					}
				}
			}
		}

		releaseRef( source );
		releaseRef( target );
	}

	private String toString( final TIntObjectHashMap< RefSet< V >> map )
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "{\n" );
		for ( final int key : map.keys() )
		{
			sb.append( "  " + key + " -> " + map.get( key ) + "\n" );
		}
		sb.append( "}" );
		return sb.toString();
	}

	/**
	 * Returns a new array containing the ids of the connected components of the
	 * graph.
	 *
	 * @return a new <code>int</code> array.
	 */
	public int[] ids()
	{
		return ccMap.keys();
	}

	/**
	 * Returns the number of connected components in the graph.
	 *
	 * @return the number of connected components in the graph.
	 */
	public int nComponents()
	{
		return ccMap.size();
	}

	/**
	 * Returns the connected component with the specified id, or
	 * <code>null</code> if the specified id does not exist.
	 *
	 * @param id
	 *            the id of the connected component.
	 * @return the connected component.
	 */
	public RefSet< V > get( final int id )
	{
		return ccMap.get( id );
	}

	/**
	 * Returns the size of the connected component with the specified id, or -1
	 * if the id does not exist.
	 *
	 * @param id
	 *            the id of the connected components.
	 * @return the connected component size.
	 */
	public int size( final int id )
	{
		final RefSet< V > cc = ccMap.get( id );
		if ( null == cc ) { return -1; }
		return cc.size();
	}

	/**
	 * Returns the id of the connected component the specified vertex belongs
	 * to, or -1 if the vertex does not belong to any connected component.
	 *
	 * @param v
	 *            the vertex.
	 * @return the id of its connected component.
	 */
	public int idOf( final V v )
	{
		for ( final int id : ccMap.keys() )
		{
			if ( ccMap.get( id ).contains( v ) ) { return id; }
		}
		return -1;
	}

	/*
	 * PRIVATE CLASSES
	 */

	private class VertexFinderListener implements SearchListener< V, E, BreadthFirstSearch< V, E > >
	{

		private V target;

		private RefSet< V > candidateCC;

		private void setTarget( final V target )
		{
			this.target = target;
		}

		private void setCandidateCC( final RefSet< V > candidateCC )
		{
			this.candidateCC = candidateCC;
		}

		@Override
		public void processVertexLate( final V vertex, final BreadthFirstSearch< V, E > search )
		{}

		@Override
		public void processVertexEarly( final V vertex, final BreadthFirstSearch< V, E > search )
		{
			if ( vertex.equals( target ) )
			{
				search.abort();
			}
			else
			{
				candidateCC.add( vertex );
			}
		}

		@Override
		public void processEdge( final E edge, final V from, final V to, final BreadthFirstSearch< V, E > search )
		{}

	}

	private static class IDProvider
	{
		private final TIntArrayDeque availableIDs = new TIntArrayDeque();

		private int id = 0;

		public int next()
		{
			if ( availableIDs.isEmpty() )
			{
				if ( id == Integer.MAX_VALUE ) { throw new RuntimeException( "Cannot have more than " + Integer.MAX_VALUE + " connected components." ); }
				return id++;
			}
			return availableIDs.pollLast();
		}

		public void free( final int id )
		{
			availableIDs.add( id );
		}
	}
}
