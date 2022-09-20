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
package org.mastodon.views.bdv.overlay;

import bdv.viewer.TransformListener;
import java.util.concurrent.locks.Lock;

import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextListener;

import net.imglib2.realtransform.AffineTransform3D;

public class OverlayContext< V extends OverlayVertex< V, ? > > implements
		Context< V >,
		TransformListener< AffineTransform3D >
{
	private final OverlayGraph< V, ? > graph;

	private final SpatioTemporalIndex< V > index;

	private final OverlayGraphRenderer< V, ? > renderer;

	private ContextListener< V > contextListener = null;

	public OverlayContext(
			final OverlayGraph< V, ? > overlayGraph,
			final OverlayGraphRenderer< V, ? > renderer )
	{
		this.graph = overlayGraph;
		this.index = graph.getIndex();
		this.renderer = renderer;
	}

	@Override
	public Lock readLock()
	{
		return index.readLock();
	}

	@Override
	public Iterable< V > getInsideVertices( final int timepoint )
	{
//		final ConvexPolytope visiblePolytope = renderer.getVisiblePolytopeGlobal( transform, timepoint );
//		final ClipConvexPolytope< V > ccp = index.getSpatialIndex( timepoint ).getClipConvexPolytope();
//		ccp.clip( visiblePolytope );
//		return ccp.getInsideValues();
		return renderer.getVisibleVertices( transform, timepoint );
	}

	@Override
	public int getTimepoint()
	{
		return renderer.getCurrentTimepoint();
	}

	private final AffineTransform3D transform = new AffineTransform3D();

	@Override
	public void transformChanged( final AffineTransform3D t )
	{
		transform.set( t );
		if ( contextListener != null )
			contextListener.contextChanged( this );
	}

	public void setContextListener( final ContextListener< V > contextListener )
	{
		this.contextListener = contextListener;
	}
}
