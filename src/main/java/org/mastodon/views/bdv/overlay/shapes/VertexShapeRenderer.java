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
import org.mastodon.views.bdv.overlay.OverlayEdge;
import org.mastodon.views.bdv.overlay.OverlayVertex;
import org.mastodon.views.bdv.overlay.RenderSettings;

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
 * <p>
 * The renderer is stateful and operates on four pieces of context:
 * <ul>
 * <li><b>View transform:</b> Converts world coordinates to screen coordinates.
 * Set via {@link #setTransform(AffineTransform3D)}. Required for all
 * operations.</li>
 * <li><b>Shape rendering settings:</b> Determines which visualization modes are
 * active (e.g., draw projection, intersection, or point for ellipsoids). Set
 * via {@link #setShapeSettings(ShapeRenderSettings)}. Required for all
 * operations.</li>
 * <li><b>Current timepoint:</b> Used to compute time-based fading and coloring.
 * Set via {@link #setCurrentTimepoint(int)}. Required for all operations.</li>
 * </ul>
 *
 * <p>
 * <b>Usage Pattern:</b>
 *
 * <pre>
 * // Setup phase (once per render/pick pass)
 * renderer.setTransform(viewTransform);
 * renderer.setShapeSettings(ellipsoidSettings);
 * renderer.setCurrentTimepoint(currentTimepoint);
 *
 * // Drawing phase
 * for (V vertex : visibleVertices) {
 *     renderer.drawVertex(graphics, vertex);
 * }
 *
 * // Hit-testing phase (reuses same renderer state)
 * if (renderer.containsScreenPoint(vertex, clickX, clickY, tolerance)) {
 *     // Vertex was hit
 * }
 * </pre>
 *
 * <p>
 * The renderer uses the constructor-injected models (coloring, selection,
 * highlight, focus) to compute final display colors, taking into account:
 * <ul>
 * <li>Base color from the graph coloring scheme</li>
 * <li>Selection/highlight/focus state</li>
 * <li>Fading based on distance from view plane and time distance</li>
 * </ul>
 *
 * The shape settings fundamentally determine both what gets rendered and how
 * hit-testing is performed. For example, an ellipsoid renderer with
 * {@code drawProjection=true, drawIntersection=false} will:
 * <ul>
 * <li>Render only the projection of the ellipsoid onto the view plane</li>
 * <li>Test hits only against the projection (not the intersection)</li>
 * </ul>
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
	 * pass, before calling {@link #drawVertex(Graphics2D, OverlayVertex)} or
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
	 * Sets the current timepoint. Used to compute time-based fading and
	 * coloring.
	 *
	 * <p>
	 * Vertices at different timepoints than the current one are faded based on
	 * the time distance.
	 *
	 * <p>
	 * This must be called at least once before any drawing or hit-testing
	 * operations.
	 *
	 * @param timepoint
	 *            the current timepoint.
	 */
	void setCurrentTimepoint( int timepoint );

	/**
	 * Draw a single vertex with its shape at its current screen position.
	 *
	 * <p>
	 * The Graphics2D context is already configured by the caller with:
	 * <ul>
	 * <li>Color (stroke and fill)</li>
	 * <li>Stroke style (width, dashing if needed)</li>
	 * <li>Rendering hints (antialiasing, etc.)</li>
	 * </ul>
	 *
	 * <p>
	 * The renderer's responsibility is to draw its shape using this graphics
	 * context. It reads shape-specific data from the vertex and uses the shape
	 * settings (previously set via
	 * {@link #setShapeSettings(ShapeRenderSettings)}) to decide which
	 * visualization modes to apply.
	 *
	 * <p>
	 * The vertex position is computed internally from the vertex and the
	 * current transform (previously set via
	 * {@link #setTransform(AffineTransform3D)}).
	 *
	 * <p>
	 * The renderer internally computes the final color based on:
	 * <ul>
	 * <li>Base color from the graph coloring scheme</li>
	 * <li>Selection/highlight/focus state of the vertex</li>
	 * <li>Fading based on depth and time distance</li>
	 * <li>Fade parameters from the shape settings</li>
	 * </ul>
	 *
	 * The Graphics2D color parameter is used as the stroke/fill color, but the
	 * renderer may compute a modified color (with fading/transparency) for
	 * drawing.
	 *
	 * @param graphics2D
	 *            the graphics context, already configured with stroke style and
	 *            rendering hints.
	 * @param vertex
	 *            the vertex to draw.
	 */
	void drawVertex( Graphics2D graphics2D, V vertex );

	/**
	 * Determine if a screen point is inside (or very close to) the drawn shape
	 * of this vertex.
	 *
	 * <p>
	 * Used for picking/selection: when the user clicks at (screenX, screenY),
	 * which vertices did they click on?
	 *
	 * <p>
	 * The hit-test algorithm depends on the shape settings (set via
	 * {@link #setShapeSettings(ShapeRenderSettings)}). For example, an
	 * ellipsoid renderer will test against the projection, intersection, or
	 * point center depending on which modes are enabled in the settings.
	 *
	 * <p>
	 * The tolerance parameter allows for "fuzzy" clicking (e.g., click within 5
	 * pixels of the shape edge).
	 *
	 * @param vertex
	 *            the vertex to test.
	 * @param screenX
	 *            X coordinate of the click in viewer (screen) space.
	 * @param screenY
	 *            Y coordinate of the click in viewer (screen) space.
	 * @param tolerance
	 *            how far from the shape boundary to still count as a hit, in
	 *            pixels.
	 * @return {@code true} if the point (screenX, screenY) is inside the
	 *         vertex's drawn shape (± tolerance), considering the current shape
	 *         settings.
	 */
	boolean containsScreenPoint( V vertex, int screenX, int screenY, double tolerance );

	/**
	 * Get all vertices that would be visible with the current display settings
	 * and the specified {@code transform} and {@code timepoint}.
	 *
	 * @param transform
	 *            the current view transform.
	 * @param timepoint
	 *            the current timepoint.
	 * @return vertices that would be visible with the current display settings
	 *         and the specified {@code transform} and {@code timepoint}.
	 */
	RefCollection< V > getVisibleVertices( final AffineTransform3D transform, final int timepoint );

}