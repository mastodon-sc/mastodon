package net.trackmate.spatial;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.Ref;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.mempool.DoubleMappedElement;
import net.trackmate.kdtree.ClipConvexPolytopeKDTree;
import net.trackmate.kdtree.KDTree;
import net.trackmate.kdtree.KDTreeNode;
import net.trackmate.kdtree.KDTreeValidIterator;
import net.trackmate.kdtree.NearestValidNeighborSearchOnKDTree;

/**
 * Spatial index of {@link RealLocalizable} objects.
 * <p>
 * When the index is {@link #SpatialIndexData(Collection, RefPool) constructed},
 * a KDTree of objects is built. The index can be modified by adding, changing,
 * and removing objects. These changes do not trigger a rebuild of the KDTree.
 * Instead, affected nodes in the KDTree are marked as invalid and the modified
 * objects are maintained in a separate set.
 * <p>
 * The idea is that a new {@link SpatialIndexData} is built after a certain
 * number of modifications.
 * <p>
 * This class is not threadsafe!
 *
 *
 *
 * TODO: the added set should not simply store refs to the original objects. It should have refs and copies of their locations, similar to KDTreeNode.
 *
 *
 *
 * @param <O>
 *            type of indexed {@link RealLocalizable} objects.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
class SpatialIndexData< O extends Ref< O > & RealLocalizable >
		implements Iterable< O >
{
	private final RefPool< O > objPool;

	/**
	 * KDTree of objects that were provided at construction. When changes are
	 * made to the set of objects (i.e. {@link #added(Ref)},
	 * {@link #remove(Ref)}, {@link #changePosition(Ref)}), the
	 * corresponding nodes in the KDTree are marked as invalid.
	 */
	private final KDTree< O, DoubleMappedElement > kdtree;

	/**
	 * Objects that were modified ({@link #added(Ref)},
	 * {@link #changePosition(Ref)}) since construction. These override
	 * invalid objects in the KDTree.
	 */
	private final RefSet< O > added;

	/**
	 * maps objects to corresponding nodes in the KDTree.
	 */
	private final RefRefMap< O, KDTreeNode< O, DoubleMappedElement > > nodeMap;

	/**
	 * temporary ref.
	 */
	private final KDTreeNode< O, DoubleMappedElement > node;

	/**
	 * Keeps track of the number of (valid) objects maintained in this index.
	 */
	private int size;

	/**
	 * Construct index from a {@link Collection} of objects.
	 *
	 * @param objs
	 *            {@link RealLocalizable} objects to index.
	 * @param objPool
	 *            pool for creating refs, collections, etc.
	 */
	SpatialIndexData( final Collection< O > objs, final RefPool< O > objPool )
	{
		this.objPool = objPool;
		kdtree = KDTree.kdtree( objs, objPool );
		nodeMap = KDTree.createRefToKDTreeNodeMap( kdtree );
		added = new PoolObjectSet< O >( objPool );
		node = kdtree.createRef();
	    size = kdtree.size();
	}

	/**
	 * Construct index from another {@link SpatialIndexData}.
	 * Puts all the objects from the other index into the KDTree.
	 */
	SpatialIndexData( final SpatialIndexData< O > si )
	{
		objPool = si.objPool;
		final Collection< O > collection = new AbstractCollection< O >()
		{
			@Override
			public Iterator< O > iterator()
			{
				return si.iterator();
			}

			@Override
			public int size()
			{
				return si.size;
			}
		};
		kdtree = KDTree.kdtree( collection, objPool );
		nodeMap = KDTree.createRefToKDTreeNodeMap( kdtree );
		added = new PoolObjectSet< O >( objPool );
		node = kdtree.createRef();
	    size = kdtree.size();
	}

	int modCount()
	{
		final int invalid = kdtree.size() + added.size() - size;
		final int modCount = added.size() + invalid;
		return modCount;
	}

	@Override
	public Iterator< O > iterator()
	{
		final Iterator< O > kdtreeIter = KDTreeValidIterator.create( kdtree, objPool );
		final Iterator< O > addedIter = added.iterator();
		return new Iter< O >( objPool, kdtreeIter, addedIter );
	}

	public NearestNeighborSearch< O > getNearestNeighborSearch()
	{
		return new NNS();
	}

	public ClipConvexPolytope< O > getClipConvexPolytope()
	{
		return new CCP();
	}

	/**
	 * Add a new object to the index. Also use this to indicate that an existing
	 * object was moved.
	 *
	 * @param obj
	 *            object to add.
	 * @return {@code true} if this index did not already contain the specified
	 *         object.
	 */
	public boolean add( final O obj )
	{
		final KDTreeNode< O, DoubleMappedElement > n = nodeMap.get( obj, node );
		if ( n != null )
		{
			if ( n.isValid() )
			{
				n.setValid( false );
				--size;
			}
		}

		if ( added.add( obj ) )
		{
			++size;
			return true;
		}

		return false;
	}

	/**
	 * Remove an object from the index.
	 *
	 * @param obj object to remove.
	 * @return {@code true} if this index contained the specified object.
	 */
	public boolean remove( final O obj )
	{
		final KDTreeNode< O, DoubleMappedElement > n = nodeMap.get( obj, node );
		if ( n != null )
		{
			if ( n.isValid() )
			{
				n.setValid( false );
				--size;
				return true;
			}
		}
		else if ( added.remove( obj ) )
		{
			--size;
			return true;
		}

		return false;
	}

	static class Iter< O extends Ref< O > > implements Iterator< O >
	{
		private final O ref;

		private final O nextref;

		private final Iterator< O > kdtreeIter;

		private final Iterator< O > addedIter;

		private boolean hasNext;

		public Iter(
				final RefPool< O > objPool,
				final Iterator< O > kdtreeIter,
				final Iterator< O > addedIter )
		{
			ref = objPool.createRef();
			nextref = objPool.createRef();
			this.kdtreeIter = kdtreeIter;
			this.addedIter = addedIter;
			hasNext = prepareNext();
		}

		private boolean prepareNext()
		{
			if ( kdtreeIter.hasNext() )
			{
				nextref.refTo( kdtreeIter.next() );
				return true;
			}
			if ( addedIter.hasNext() )
			{
				nextref.refTo( addedIter.next() );
				return true;
			}
			return false;
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public O next()
		{
			if ( hasNext )
			{
				ref.refTo( nextref );
				hasNext = prepareNext();
			}
			return ref;
		}
	}

	class NNS implements NearestNeighborSearch< O >, Sampler< O >
	{
		private final NearestValidNeighborSearchOnKDTree< O, DoubleMappedElement > search;

		private double bestSquDistance;

		private int bestVertexIndex;

		private final O bestVertex;

		private final int n;

		public NNS()
		{
			search = new NearestValidNeighborSearchOnKDTree< O, DoubleMappedElement >( kdtree );
			bestVertexIndex = -1;
			bestVertex = objPool.createRef();
			n = search.numDimensions();
		}

		@Override
		public int numDimensions()
		{
			return n;
		}

		@Override
		public void search( final RealLocalizable pos )
		{
			bestSquDistance = Double.MAX_VALUE;
			bestVertexIndex = -1;

			search.search( pos );
			if ( search.get() != null )
			{
				bestSquDistance = search.getSquareDistance();
				bestVertexIndex = search.get().getInternalPoolIndex();
			}

			final double[] p = new double[ n ];
			for ( final O v : added )
			{
				double sum = 0;
				for ( int d = 0; d < n; ++d )
				{
					final double diff = v.getDoublePosition( d ) - p[ d ];
					sum += diff * diff;
				}
				if ( sum < bestSquDistance )
				{
					bestSquDistance = sum;
					bestVertexIndex = v.getInternalPoolIndex();
				}
			}

			if ( bestVertexIndex >= 0 )
				objPool.getByInternalPoolIndex( bestVertexIndex, bestVertex );
		}

		@Override
		public Sampler< O > getSampler()
		{
			return this;
		}

		@Override
		public RealLocalizable getPosition()
		{
			if ( bestVertexIndex == -1 )
				return null;

			return bestVertex;
		}

		@Override
		public double getSquareDistance()
		{
			return bestSquDistance;
		}

		@Override
		public double getDistance()
		{
			return Math.sqrt( bestSquDistance );
		}

		@Override
		public NNS copy()
		{
			final NNS copy = new NNS();
			copy.bestSquDistance = bestSquDistance;
			copy.bestVertexIndex = bestVertexIndex;
			if ( bestVertexIndex != -1 )
				copy.bestVertex.refTo( bestVertex );
			return copy;
		}

		@Override
		public O get()
		{
			if ( bestVertexIndex == -1 )
				return null;

			return bestVertex;
		}
	}

	class CCP implements ClipConvexPolytope< O >
	{
		private final ClipConvexPolytopeKDTree< O, DoubleMappedElement > clip;

		private final RefList< O > inside;

		private final RefList< O > outside;

		private final int n;

		public CCP()
		{
			clip = new ClipConvexPolytopeKDTree<>( kdtree );
			inside = new PoolObjectList< O >( objPool );
			outside = new PoolObjectList< O >( objPool );
			n = clip.numDimensions();
		}

		@Override
		public int numDimensions()
		{
			return n;
		}

		@Override
		public void clip( final ConvexPolytope polytope )
		{
			clip.clip( polytope );
			clipAdded( polytope );
		}

		@Override
		public void clip( final double[][] planes )
		{
			clip.clip( planes );
			clipAdded( planes );
		}

		@Override
		public Iterable< O > getInsideValues()
		{
			return new Iterable< O >()
			{
				@Override
				public Iterator< O > iterator()
				{
					final Iterator< O > kdtreeIter = clip.getValidInsideValues().iterator();
					final Iterator< O > addedIter = inside.iterator();
					return new Iter< O >( objPool, kdtreeIter, addedIter );
				}
			};
		}

		@Override
		public Iterable< O > getOutsideValues()
		{
			return new Iterable< O >()
			{
				@Override
				public Iterator< O > iterator()
				{
					final Iterator< O > kdtreeIter = clip.getValidOutsideValues().iterator();
					final Iterator< O > addedIter = outside.iterator();
					return new Iter< O >( objPool, kdtreeIter, addedIter );
				}
			};
		}

		private void clipAdded( final ConvexPolytope polytope )
		{
			final Collection< ? extends HyperPlane > hyperplanes = polytope.getHyperplanes();
			final double[][] planes = new double[ hyperplanes.size() ][];
			int i = 0;
			for ( final HyperPlane hyperplane : hyperplanes )
			{
				final double[] plane = new double[ n + 1 ];
				System.arraycopy( hyperplane.getNormal(), 0, plane, 0, n );
				plane[ n ] = hyperplane.getDistance();
				planes[ i++ ] = plane;
			}
			clipAdded( planes );
		}

		private void clipAdded( final double[][] planes )
		{
			final int nPlanes = planes.length;
			inside.clear();
			outside.clear();
			A: for ( final O p : added )
			{
				for ( int i = 0; i < nPlanes; ++i )
				{
					final double[] plane = planes[ i ];
					double dot = 0;
					for ( int d = 0; d < n; ++d )
						dot += p.getDoublePosition( d ) * plane[ d ];
					if ( dot < plane[ n ] )
					{
						outside.add( p );
						continue A;
					}
				}
				inside.add( p );
			}
		}

	}
}
