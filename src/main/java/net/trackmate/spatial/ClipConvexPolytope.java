package net.trackmate.spatial;

import net.imglib2.EuclideanSpace;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.kdtree.ConvexPolytope;

/**
 * Partition points in an Euclidean space into sets that are inside and outside
 * a given convex polytope, respectively.
 *
 * @param <T>
 *            type of points (usually {@link RealLocalizable}).
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface ClipConvexPolytope< T > extends EuclideanSpace
{
	/**
	 * Partition points into inside and outside of the given
	 * {@link ConvexPolytope}.
	 *
	 * @param polytope
	 *            polytope to clip with.
	 */
	public void clip( final ConvexPolytope polytope );

	/**
	 * Partition points into inside and outside of a convex polytope. The
	 * polytope is specified by a set of planes, such that the polytope
	 * comprises points that are in the positive half-space of all planes.
	 *
	 * @param planes
	 *            array of planes specifying the polytope to clip with. Each
	 *            plane <em>x·n=m</em> is given as a {@code double} array
	 *            <em>[n<sub>1</sub>, ..., n<sub>N</sub>, m]</em>
	 */
	public void clip( final double[][] planes );

	/**
	 * Get points inside the convex polytope specified in the last
	 * {@link #clip(ConvexPolytope)} operation.
	 *
	 * @return points inside the convex polytope.
	 */
	public Iterable< T > getInsideValues();

	/**
	 * Get points outside the convex polytope specified in the last
	 * {@link #clip(ConvexPolytope)} operation.
	 *
	 * @return points outside the convex polytope.
	 */
	public Iterable< T > getOutsideValues();
}
