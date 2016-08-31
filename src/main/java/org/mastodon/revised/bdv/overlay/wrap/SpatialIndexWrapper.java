package org.mastodon.revised.bdv.overlay.wrap;

import java.util.Iterator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.kdtree.ClipConvexPolytope;
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

	class NNS implements NearestNeighborSearch< OverlayVertexWrapper< V, E > >,	Sampler< OverlayVertexWrapper< V, E > >
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
					return new OverlayVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(), iterable.iterator() );
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
					return new OverlayVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(), iterable.iterator() );
				}
			};
		}
	}
}
