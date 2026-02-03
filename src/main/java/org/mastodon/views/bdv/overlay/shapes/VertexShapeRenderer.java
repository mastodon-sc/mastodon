/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2026 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.bdv.overlay.shapes;

import java.awt.Graphics2D;

import org.mastodon.collection.RefCollection;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.overlay.OverlayEdge;
import org.mastodon.views.bdv.overlay.OverlayVertex;
import org.mastodon.views.bdv.overlay.RenderSettings;

import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Renders vertices of a specific shape type in a BDV overlay.
 *
 * <p>
 * Implementations know how to:
 * <ul>
 * <li>Draw their specific shape (ellipsoid, polygon, etc.)</li>
 * <li>Determine if a screen point hits the shape (for picking/selection)</li>
 * <li>Provide the screen bounding box (for culling/intersection tests)</li>
 * </ul>
 *
 *
 * @param <V>
 *            the vertex type
 * @param <E>
 *            the edge type
 */
public interface VertexShapeRenderer< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V >, R extends RenderSettings< R > >
{

	/**
	 * Sets the current view transform. This converts vertex positions from
	 * world coordinates to screen coordinates.
	 *
	 * <p>
	 * This must be called at least once before any drawing or hit-testing
	 * operations. Typically called once per render pass and once per hit-test
	 * pass, before calling {@link #drawVertices(Graphics2D, OverlayVertex)} or
	 * {@link #containsScreenPoint(OverlayVertex, int, int, double)} multiple
	 * times.
	 *
	 * @param transform
	 *            the current view transform.
	 */
	void setTransform( AffineTransform3D transform );

	/**
	 * Sets the current shape rendering settings. This determines which
	 * visualization modes are active and how hit-testing is performed.
	 *
	 * <p>
	 * The shape settings fundamentally determine:
	 * <ul>
	 * <li>Which parts of the shape are rendered (e.g., projection,
	 * intersection, or point center)</li>
	 * <li>How fading/transparency is applied</li>
	 * <li>Which hit-test algorithms are used in
	 * {@link #containsScreenPoint(OverlayVertex, int, int, double)}</li>
	 * </ul>
	 *
	 * <p>
	 * This must be called at least once before any drawing or hit-testing
	 * operations. Typically called once per render pass and once per hit-test
	 * pass, with the same settings used for all subsequent operations until
	 * changed.
	 *
	 * @param renderSettings
	 *            the shape-specific rendering settings.
	 */
	void setShapeSettings( R renderSettings );

	/**
	 * Draw vertices with their shape.
	 *
	 * <p>
	 * The Graphics2D context is already configured by the caller with rendering
	 * hints (antialiasing, etc.).
	 *
	 * <p>
	 * The renderer's responsibility is to draw its shape using this graphics
	 * context. It reads shape-specific data from the vertex and uses the shape
	 * settings (previously set via
	 * {@link #setShapeSettings(ShapeRenderSettings)}) to decide which
	 * visualization modes to apply. A render settings may specify that some
	 * vertices that are passed to this method should not be drawn at all.
	 *
	 * @param graphics2D
	 *            the graphics context, already configured with stroke style and
	 *            rendering hints.
	 * @param vertices
	 *            the vertices to draw.
	 */
	void drawVertices( Graphics2D graphics2D, Iterable< V > vertices );

	/**
	 * Filters the specified vertices and return only those that would be
	 * visible with the current display settings and the specified
	 * {@code transform} and {@code timepoint}.
	 * <p>
	 * This method is meant to be called from the OverlayGraphRenderer, which
	 * can return the vertices in the current view.
	 *
	 * @param in
	 *            the vertices to filter.
	 * @param transform
	 *            the current view transform.
	 * @param timepoint
	 *            the current timepoint.
	 * @param width
	 *            the width of the overlay panel.
	 * @param height
	 *            the height of the overlay panel.
	 * @return vertices that would be visible with the current display settings
	 *         and the specified {@code transform} and {@code timepoint}.
	 */
	RefCollection< V > filterVisibleVertices(
			Iterable< V > in,
			final AffineTransform3D transform,
			final int timepoint,
			final int width,
			final int height );

	/**
	 * Returns the vertex currently painted close to the specified location.
	 * <p>
	 * This must be used only by the OverlayGraphRenderer, which is in charge of
	 * preparing the argument used here. The spatial index will be properly
	 * locked and unlocked there.
	 *
	 * @param x
	 *            the x coordinate in screen space.
	 * @param y
	 *            the y coordinate in screen space.
	 * @param tolerance
	 *            the tolerance in pixels for picking objects when they are
	 *            drawn as points.
	 * @param si
	 *            the spatial index of the current timepoint to search in.
	 * @param polytope
	 *            the polytope representing the current view frustum in global
	 *            coordinates
	 * @param maxRadiusSquared
	 *            the maximum radius of an object in the current view, squared.
	 *            This is used to limit the search for intersection hits.
	 * @return the vertex at the given position, or null if none found.
	 */
	V getVertexAt( int x, int y, double tolerance, SpatialIndex< V > si, ConvexPolytope polytope, double maxRadiusSquared, V ref );

}
