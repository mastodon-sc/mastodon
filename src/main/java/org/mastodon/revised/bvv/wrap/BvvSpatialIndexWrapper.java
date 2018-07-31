package org.mastodon.revised.bvv.wrap;

import java.util.Iterator;
import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.kdtree.ClipConvexPolytope;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.spatial.SpatialIndex;

public class BvvSpatialIndexWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements SpatialIndex< BvvVertexWrapper< V, E > >
{
	private final BvvGraphWrapper< V, E > graphWrapper;

	private final SpatialIndex< V > wrappedIndex;

	public BvvSpatialIndexWrapper( final BvvGraphWrapper< V, E > graphWrapper, final SpatialIndex< V > index )
	{
		this.graphWrapper = graphWrapper;
		this.wrappedIndex = index;
	}

	@Override
	public Iterator< BvvVertexWrapper< V, E > > iterator()
	{
		return new BvvVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(), wrappedIndex.iterator() );
	}

	@Override
	public NearestNeighborSearch< BvvVertexWrapper< V, E > > getNearestNeighborSearch()
	{
		return new NNS();
	}

	@Override
	public IncrementalNearestNeighborSearch< BvvVertexWrapper< V, E > > getIncrementalNearestNeighborSearch()
	{
		return new INNS();
	}

	@Override
	public ClipConvexPolytope< BvvVertexWrapper< V, E > > getClipConvexPolytope()
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

	class NNS implements NearestNeighborSearch< BvvVertexWrapper< V, E > >,	Sampler< BvvVertexWrapper< V, E > >
	{
		private final NearestNeighborSearch< V > wrappedNNS;

		private final BvvVertexWrapper< V, E > v;

		public NNS()
		{
			this.wrappedNNS = wrappedIndex.getNearestNeighborSearch();
			this.v = graphWrapper.vertexRef();
		}

		@Override
		public BvvVertexWrapper< V, E > get()
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
		public Sampler< BvvVertexWrapper< V, E > > getSampler()
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

	class INNS implements IncrementalNearestNeighborSearch< BvvVertexWrapper< V, E > >
	{
		private final IncrementalNearestNeighborSearch< V > wrappedINNS;

		private final BvvVertexWrapper< V, E > v;

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
		public BvvVertexWrapper< V, E > get()
		{
			final V nnv = wrappedINNS.get();
			if ( nnv == null )
				return null;

			final int id = graphWrapper.idmap.getVertexId( nnv );
			v.wv = graphWrapper.idmap.getVertex( id, v.ref );
			return v;
		}

		@Override
		public BvvVertexWrapper< V, E > next()
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

	class CCP implements ClipConvexPolytope< BvvVertexWrapper< V, E > >
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
		public Iterable< BvvVertexWrapper< V, E > > getInsideValues()
		{
			return () -> new BvvVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(), wrappedCCP.getInsideValues().iterator() );
		}

		@Override
		public Iterable< BvvVertexWrapper< V, E > > getOutsideValues()
		{
			return () -> new BvvVertexIteratorWrapper<>( graphWrapper, graphWrapper.vertexRef(), wrappedCCP.getOutsideValues().iterator() );
		}
	}
}
