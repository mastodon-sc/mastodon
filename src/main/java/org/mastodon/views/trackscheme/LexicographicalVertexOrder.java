/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.trackscheme;

import java.util.ArrayList;
import java.util.Collections;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.views.trackscheme.util.AlphanumCompare;

import gnu.trove.iterator.TIntIterator;
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
			keys.add( VertexKey.build( v, graph ) );
		Collections.sort( keys );

		final RefArrayList< TrackSchemeVertex > sorted =
				new RefArrayList<>( graph.vertices().getRefPool(), vertices.size() );
		final TIntArrayList indices = sorted.getIndexCollection();
		for ( final VertexKey k : keys )
			indices.add( k.getVertexInternalPoolIndex() );

		return sorted;
	}

	private static class VertexKey implements Comparable< VertexKey >
	{
		public static VertexKey build(
				final TrackSchemeVertex v,
				final TrackSchemeGraph< ?, ? > graph )
		{
			final VertexKey token;
			if ( v.incomingEdges().isEmpty() )
				token = new VertexKey( v.getRootLabel() );
			else
			{
				final TrackSchemeVertex ref = graph.vertexRef();

				final TrackSchemeEdge parentEdge = v.incomingEdges().iterator().next();
				final TrackSchemeVertex parent = parentEdge.getSource( ref );
				token = build( parent, graph );

				int i = 0;
				for ( final TrackSchemeEdge e : parent.outgoingEdges() )
					if ( e.equals( parentEdge ) )
						break;
					else
						++i;
				token.append( i );

				graph.releaseRef( ref );
			}
			token.vertexInternalPoolIndex = v.getInternalPoolIndex();
			return token;
		}

		private final String rootName;

		private final TIntList edgeSequence;

		private int vertexInternalPoolIndex;

		private VertexKey( final String rootName )
		{
			this.rootName = rootName;
			edgeSequence = new TIntArrayList();
		}

		private void append( final int i )
		{
			edgeSequence.add( i );
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
				final TIntIterator it = edgeSequence.iterator();
				final TIntIterator oit = o.edgeSequence.iterator();
				while ( it.hasNext() )
				{
					if ( !oit.hasNext() )
						return 1;
					final int e = it.next() - oit.next();
					if ( e != 0 )
						return e;
				}
				return oit.hasNext() ? -1 : 0;
			}
			else
				return rc;
		}

		@Override
		public String toString()
		{
			final StringBuilder b = new StringBuilder( "[" + rootName );
			final TIntIterator it = edgeSequence.iterator();
			while ( it.hasNext() )
			{
				b.append( "," );
				b.append( it.next() );
			}
			b.append( "]" );
			return b.toString();
		}
	}
}
