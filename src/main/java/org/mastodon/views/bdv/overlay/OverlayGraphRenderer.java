/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.bdv.overlay;

import org.mastodon.collection.RefCollection;
import org.mastodon.spatial.SpatioTemporalIndex;

import bdv.viewer.OverlayRenderer;
import bdv.viewer.TimePointListener;
import bdv.viewer.TransformListener;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Renderer for a time-resliced graph overlay on a BDV.
 * <p>
 * In this class, spatial coordinates are stored in local variables named
 * <code>gPos</code> and <code>lPos</code> of type <code>double[]</code> with 3
 * elements:
 * <ul>
 * <li><code>gPos</code> are world coordinates. It is used to store coordinates
 * in the global referential, that is the one with absolute, physical
 * coordinates. It is used <i>e.g.</i> to store vertex coordinates:
 * <code>vertex.localize(gPos)</code>.</li>
 * <li><code>lPos</code> are viewer coordinates. It is used to store coordinates
 * in the local referential, currently rendered in the BDV under a certain
 * orientation, zoom, etc. Mouse coordinates are typically stored in this
 * variable.
 * </ul>
 *
 * <p>
 *
 * TODO: Review and revise.
 *
 * @param <V>
 *            the type of model vertex.
 * @param <E>
 *            the type of model edge.
 *
 * @author Tobias Pietzsch
 */
public interface OverlayGraphRenderer< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends OverlayRenderer, TransformListener< AffineTransform3D >, TimePointListener
{

	/**
	 * Returns the edge currently painted close to the specified location.
	 * <p>
	 * It is the responsibility of the caller to lock the graph it inspects for
	 * reading operations, prior to calling this method. A typical call from
	 * another method would happen like this:
	 *
	 * <pre>
	 * ReentrantReadWriteLock lock = graph.getLock();
	 * lock.readLock().lock();
	 * boolean found = false;
	 * try
	 * {
	 * 	E edge = renderer.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, ref )
	 * 	... // do something with the edge
	 * 	... // edge is guaranteed to stay valid while the lock is held
	 * }
	 * finally
	 * {
	 * 	lock.readLock().unlock();
	 * }
	 * </pre>
	 *
	 * @param x
	 *            the x location to search, in viewer coordinates (screen).
	 * @param y
	 *            the y location to search, in viewer coordinates (screen).
	 * @param tolerance
	 *            the distance tolerance to accept close edges.
	 * @param ref
	 *            an edge reference, that might be used to return the vertex
	 *            found.
	 * @return the closest edge within tolerance, or <code>null</code> if it
	 *         could not be found.
	 */
	public E getEdgeAt( final int x, final int y, final double tolerance, final E ref );

	/**
	 * Transform viewer coordinates to global (world) coordinates.
	 *
	 * @param x
	 *            viewer X coordinate
	 * @param y
	 *            viewer Y coordinate
	 * @param gPos
	 *            receives global coordinates corresponding to viewer
	 *            coordinates <em>(x, y, 0)</em>.
	 */
	public void getGlobalPosition( final int x, final int y, final double[] gPos );

	/**
	 * Transform global (world) coordinates to viewer coordinates.
	 *
	 * @param gPos
	 *            global coordinates to transform.
	 * @param vPos
	 *            receives the viewer coordinates.
	 */
	public void getViewerPosition( final double[] gPos, final double[] vPos );

	public int getCurrentTimepoint();

	/**
	 * Returns the vertex currently painted close to the specified location.
	 * <p>
	 * It is the responsibility of the caller to lock the graph it inspects for
	 * reading operations, prior to calling this method. A typical call from
	 * another method would happen like this:
	 *
	 * <pre>
	 * ReentrantReadWriteLock lock = graph.getLock();
	 * lock.readLock().lock();
	 * try
	 * {
	 * 	V vertex = renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, ref );
	 * 	... // do something with the vertex
	 * 	... // vertex is guaranteed to stay valid while the lock is held
	 * }
	 * finally
	 * {
	 * 	lock.readLock().unlock();
	 * }
	 * </pre>
	 *
	 * @param x
	 *            the x location to search, in viewer coordinates (screen).
	 * @param y
	 *            the y location to search, in viewer coordinates (screen).
	 * @param tolerance
	 *            the distance tolerance to accept close vertices.
	 * @param ref
	 *            a vertex reference, that might be used to return the vertex
	 *            found.
	 * @return the closest vertex within tolerance, or <code>null</code> if it
	 *         could not be found.
	 */
	public V getVertexAt( final int x, final int y, final double tolerance, final V ref );

	/**
	 * Get all vertices that would be visible with the current display settings
	 * and the specified {@code transform} and {@code timepoint}. This is used
	 * to compute {@link OverlayContext}.
	 * <p>
	 * Note, that it doesn't lock the {@link SpatioTemporalIndex}: we assumed,
	 * that this is already done by the caller.
	 * <p>
	 * TODO: The above means that the index is locked for longer than
	 * necessary.Revisit this and once it is clear how contexts are used in
	 * practice.
	 *
	 * @param transform
	 * @param timepoint
	 * @return vertices that would be visible with the current display settings
	 *         and the specified {@code transform} and {@code timepoint}.
	 */
	public RefCollection< V > getVisibleVertices( final AffineTransform3D transform, final int timepoint );

}
