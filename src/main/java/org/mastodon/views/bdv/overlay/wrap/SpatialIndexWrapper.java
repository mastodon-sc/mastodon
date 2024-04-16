/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.bdv.overlay.wrap;

import java.util.Iterator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.kdtree.ClipConvexPolytope;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.spatial.SpatialIndex;

import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.neighborsearch.NearestNeighborSearch;

public class SpatialIndexWrapper< V extends Vertex< E >, E extends Edge< V > >
		implements SpatialIndex< OverlayVertexWrapper< V, E > >
{
	private final OverlayGraphWrapper< V, E > graphWrapper;

	private final SpatialIndex< V > wrappedIndex;

	public SpatialIndexWrapper( final OverlayGraphWrapper< V, E > graphWrapper, final SpatialIndex< V > index )
	{
		this.graphWrapper = graphWrapper;
		this.wrappedIndex = index;
	}

	@Override
	public Iterator< OverlayVertexWrapper< V, E > > iterator()
	{
		return new OverlayVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(), wrappedIndex.iterator() );
	}

	@Override
	public NearestNeighborSearch< OverlayVertexWrapper< V, E > > getNearestNeighborSearch()
	{
		return new NNS();
	}

	@Override
	public IncrementalNearestNeighborSearch< OverlayVertexWrapper< V, E > > getIncrementalNearestNeighborSearch()
	{
		return new INNS();
	}

	@Override
	public ClipConvexPolytope< OverlayVertexWrapper< V, E > > getClipConvexPolytope()
	{
		return new CCP();
	}

	@Override
	public int size()
	{
		return wrappedIndex.size();
	}

	@Override
	public boolean isEmpty()
	{
		return wrappedIndex.isEmpty();
	}

	class NNS implements NearestNeighborSearch< OverlayVertexWrapper< V, E > >, Sampler< OverlayVertexWrapper< V, E > >
	{
		private final NearestNeighborSearch< V > wrappedNNS;

		private final OverlayVertexWrapper< V, E > v;

		public NNS()
		{
			this.wrappedNNS = wrappedIndex.getNearestNeighborSearch();
			this.v = graphWrapper.vertexRef();
		}

		@Override
		public OverlayVertexWrapper< V, E > get()
		{
			final V nnv = wrappedNNS.getSampler().get();
			if ( nnv == null )
				return null;

			final int id = graphWrapper.idmap.getVertexId( nnv );
			v.wv = graphWrapper.idmap.getVertex( id, v.ref );
			return v;
		}

		@Override
		public int numDimensions()
		{
			return wrappedNNS.numDimensions();
		}

		@Override
		public void search( final RealLocalizable pos )
		{
			wrappedNNS.search( pos );
		}

		@Override
		public Sampler< OverlayVertexWrapper< V, E > > getSampler()
		{
			return this;
		}

		@Override
		public RealLocalizable getPosition()
		{
			return wrappedNNS.getPosition();
		}

		@Override
		public double getSquareDistance()
		{
			return wrappedNNS.getSquareDistance();
		}

		@Override
		public double getDistance()
		{
			return wrappedNNS.getDistance();
		}

		private NNS( final NNS other )
		{
			this.wrappedNNS = other.wrappedNNS.copy();
			this.v = graphWrapper.vertexRef();
		}

		@Override
		public NNS copy()
		{
			return new NNS( this );
		}
	}

	class INNS implements IncrementalNearestNeighborSearch< OverlayVertexWrapper< V, E > >
	{
		private final IncrementalNearestNeighborSearch< V > wrappedINNS;

		private final OverlayVertexWrapper< V, E > v;

		public INNS()
		{
			this.wrappedINNS = wrappedIndex.getIncrementalNearestNeighborSearch();
			this.v = graphWrapper.vertexRef();
		}

		@Override
		public void localize( final float[] position )
		{
			wrappedINNS.localize( position );
		}

		@Override
		public void localize( final double[] position )
		{
			wrappedINNS.localize( position );
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return wrappedINNS.getFloatPosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return wrappedINNS.getDoublePosition( d );
		}

		@Override
		public int numDimensions()
		{
			return wrappedINNS.numDimensions();
		}

		@Override
		public void jumpFwd( final long steps )
		{
			wrappedINNS.jumpFwd( steps );
		}

		@Override
		public void fwd()
		{
			wrappedINNS.fwd();
		}

		@Override
		public void reset()
		{
			wrappedINNS.reset();
		}

		@Override
		public boolean hasNext()
		{
			return wrappedINNS.hasNext();
		}

		@Override
		public OverlayVertexWrapper< V, E > get()
		{
			final V nnv = wrappedINNS.get();
			if ( nnv == null )
				return null;

			final int id = graphWrapper.idmap.getVertexId( nnv );
			v.wv = graphWrapper.idmap.getVertex( id, v.ref );
			return v;
		}

		@Override
		public OverlayVertexWrapper< V, E > next()
		{
			fwd();
			return get();
		}

		@Override
		public double getSquareDistance()
		{
			return wrappedINNS.getSquareDistance();
		}

		@Override
		public double getDistance()
		{
			return wrappedINNS.getDistance();
		}

		@Override
		public void search( final RealLocalizable reference )
		{
			wrappedINNS.search( reference );
		}

		private INNS( final INNS other )
		{
			this.wrappedINNS = other.wrappedINNS.copy();
			this.v = graphWrapper.vertexRef();
		}

		@Override
		public INNS copy()
		{
			return new INNS( this );
		}

		@Override
		public INNS copyCursor()
		{
			return copy();
		}
	}

	class CCP implements ClipConvexPolytope< OverlayVertexWrapper< V, E > >
	{
		private final ClipConvexPolytope< V > wrappedCCP;

		public CCP()
		{
			this.wrappedCCP = wrappedIndex.getClipConvexPolytope();
		}

		@Override
		public int numDimensions()
		{
			return wrappedCCP.numDimensions();
		}

		@Override
		public void clip( final ConvexPolytope polytope )
		{
			wrappedCCP.clip( polytope );
		}

		@Override
		public void clip( final double[][] planes )
		{
			wrappedCCP.clip( planes );
		}

		@Override
		public Iterable< OverlayVertexWrapper< V, E > > getInsideValues()
		{
			final Iterable< V > iterable = wrappedCCP.getInsideValues();
			return new Iterable< OverlayVertexWrapper< V, E > >()
			{
				@Override
				public Iterator< OverlayVertexWrapper< V, E > > iterator()
				{
					return new OverlayVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(),
							iterable.iterator() );
				}
			};
		}

		@Override
		public Iterable< OverlayVertexWrapper< V, E > > getOutsideValues()
		{
			final Iterable< V > iterable = wrappedCCP.getOutsideValues();
			return new Iterable< OverlayVertexWrapper< V, E > >()
			{
				@Override
				public Iterator< OverlayVertexWrapper< V, E > > iterator()
				{
					return new OverlayVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(),
							iterable.iterator() );
				}
			};
		}
	}
}
