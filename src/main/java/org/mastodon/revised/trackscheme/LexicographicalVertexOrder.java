package org.mastodon.revised.trackscheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.graph.Edges;
import org.mastodon.revised.trackscheme.util.AlphanumCompare;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Lexicographically order {@link TrackSchemeVertex} for root sorting in
 * TrackScheme. Each TrackSchemeVertex is assigned a key recursively as follows:
 * <ol>
 * <li>Roots have key <em>[label]</em> consisting of their label.
 * <li>Non-roots have key <em>[parentkey|i]</em>, where <em>parentkey</em> is
 * the key of the nodes parent and this node is the <em>i</em>th child of the
 * parent.
 * </ol>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class LexicographicalVertexOrder
{
	public static RefList< TrackSchemeVertex > sort(
			final TrackSchemeGraph< ?, ? > graph,
			final RefCollection< TrackSchemeVertex > vertices )
	{
		final ArrayList< VertexKey > keys = new ArrayList<>( vertices.size() );
		for ( final TrackSchemeVertex v : vertices )
			keys.add( new VertexKey( v, graph ) );
		Collections.sort( keys );

		final RefArrayList< TrackSchemeVertex > sorted = new RefArrayList<>( graph.vertices().getRefPool(), vertices.size() );
		final TIntArrayList indices = sorted.getIndexCollection();
		for ( final VertexKey k : keys )
			indices.add( k.getVertexInternalPoolIndex() );

		return sorted;
	}

	public static Comparator< TrackSchemeVertex > comparator( final TrackSchemeGraph< ?, ? > graph )
	{
		return new Comparator< TrackSchemeVertex >()
		{
			@Override
			public int compare( final TrackSchemeVertex v1, final TrackSchemeVertex v2 )
			{
				return new VertexKey( v1, graph ).compareTo( new VertexKey( v2, graph ) );
			}
		};
	}

	public static Comparator< TrackSchemeVertex > comparator2( final TrackSchemeGraph< ?, ? > graph )
	{
		return new Comparator< TrackSchemeVertex >()
		{
			@Override
			public int compare( final TrackSchemeVertex v1, final TrackSchemeVertex v2 )
			{

				return 0;
			}
		};
	}

	private static class VertexKey implements Comparable< VertexKey >
	{
		private final String rootName;

		private final TIntList edgeSequence;

		private final int vertexInternalPoolIndex;

		public VertexKey(
				final TrackSchemeVertex v,
				final TrackSchemeGraph< ?, ? > graph )
		{
			edgeSequence = new TIntArrayList();
			vertexInternalPoolIndex = v.getInternalPoolIndex();
			final TrackSchemeVertex ref = graph.vertexRef();
			ref.refTo( v );
			Edges< TrackSchemeEdge > edges = v.incomingEdges();
			while ( !edges.isEmpty() )
			{
				final TrackSchemeEdge parentEdge = edges.get( 0 );
				final TrackSchemeVertex parent = parentEdge.getSource( ref );
				int i = 0;
				for ( final TrackSchemeEdge e : parent.outgoingEdges() )
					if ( e.equals( parentEdge ) )
						break;
					else
						++i;
				edgeSequence.add( i );
				edges = parent.incomingEdges();
			}
			rootName = ref.getLabel();
			graph.releaseRef( ref );
		}

		public int getVertexInternalPoolIndex()
		{
			return vertexInternalPoolIndex;
		}

		@Override
		public int compareTo( final VertexKey o )
		{
			final int rc = AlphanumCompare.compare( rootName, o.rootName );
			if ( rc == 0 )
			{
				int i = edgeSequence.size() - 1;
				int oi = o.edgeSequence.size() - 1;
				while ( i >= 0 )
				{
					if ( oi < 0 )
						return 1;
					final int e = edgeSequence.get( i-- ) - o.edgeSequence.get( oi-- );
					if ( e != 0 )
						return e;
				}
				return ( oi >= 0 ) ? -1 : 0;
			}
			else
				return rc;
		}

		@Override
		public String toString()
		{
			final StringBuilder b = new StringBuilder( "[" + rootName );
			int i = edgeSequence.size() - 1;
			while ( i >= 0 )
			{
				b.append( "," );
				b.append( edgeSequence.get( i-- ) );
			}
			b.append( "]" );
			return b.toString();
		}
	}
}
