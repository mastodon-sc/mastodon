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
import net.trackmate.graph.traversal.GraphIteratorBuilder;
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

	private final GraphIteratorBuilder< V, E > builder;

	public ConnectedComponentsDynamicDefault( final ListenableGraph< V, E > graph )
	{
		super( graph );
		this.ccMap = new TIntObjectHashMap< RefSet< V > >();
		this.idProvider = new IDProvider();
		this.builder = GraphIteratorBuilder.createOn( graph ).undirected().unsorted().withTraversalListener( null );
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
					final RefSet< V > newCC = createVertexSet(2);
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
		 * Process edges removed now.
		 */

		if ( !event.getEdgeRemoved().isEmpty() )
		{
			final Iterator< E > it = event.getEdgeAdded().iterator();
			while ( it.hasNext() )
			{
				final E edge = it.next();
				source = edge.getSource( source );
				target = edge.getTarget( target );
				final int id = idOf( source );

				/*
				 * Iterate from the source vertex and see if we meet target.
				 */

				final Iterator< V > fromSourceIt = builder.breadthFirst( source ).build();
				final RefSet< V > candidateCC = createVertexSet();
				boolean sameCC = false;
				while ( fromSourceIt.hasNext() )
				{
					final V v = fromSourceIt.next();
					if ( v.equals( target ) )
					{
						sameCC = true;
						break;
					}
					candidateCC.add( v );
				}

				if ( sameCC )
				{
					/*
					 * They still belong to the same cc, even after edge
					 * removal. We don't do anything.
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

					final int sizeTargetSide = ccMap.get( id ).size() - sizeSourceSide;
					if ( sizeTargetSide > 1 )
					{
						ccMap.get( id ).removeAll( candidateCC );
					}
					else
					{
						// It became too small, remove it.
						ccMap.remove( id );
						idProvider.free( id );
					}
				}
			}
		}
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
