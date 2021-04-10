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
package org.mastodon.views.bdv;

import bdv.viewer.TransformListener;
import bdv.viewer.ViewerPanel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.views.bdv.overlay.OverlayContext;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.wrap.OverlayContextWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextListener;
import org.mastodon.views.context.ContextProvider;
import org.scijava.listeners.Listeners;

import net.imglib2.realtransform.AffineTransform3D;

/**
 * A model vertices {@link ContextProvider}. It provides a
 * {@link OverlayContextWrapper} (always the same) which wraps a
 * {@link OverlayContext} (always the same). It must be registered to
 * {@link ViewerPanel#renderTransformListeners()} to be updated with the
 * viewer transform.
 *
 * @param <V>
 *            the type of vertices in the model.
 * @param <E>
 *            the type of edges in the model.
 * @author Tobias Pietzsch
 */
public class BdvContextProvider< V extends Vertex< E >, E extends Edge< V > >
		implements ContextProvider< V >, TransformListener< AffineTransform3D >
{
	private final String name;

	private final Listeners.List< ContextListener< V > > listeners;

	private final OverlayContext< OverlayVertexWrapper< V, E > > overlayContext;

	private Context< V > context;

	private final OverlayContextWrapper< V, E > overlayContextWrapper;

	public BdvContextProvider( final String name,
			final OverlayGraph< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > overlayGraph,
			final OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > renderer )
	{
		this.name = name;
		listeners = new Listeners.SynchronizedList<>( l -> l.contextChanged( context ) );
		overlayContext = new OverlayContext<>( overlayGraph, renderer );
		overlayContextWrapper = new OverlayContextWrapper<>( overlayContext, this::contextChanged );
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Listeners< ContextListener< V > > listeners()
	{
		return listeners;
	}

	@Override
	public void transformChanged( final AffineTransform3D transform )
	{
		overlayContext.transformChanged( transform );
	}

	/**
	 * Expose the {@link #overlayContextWrapper}.
	 * 
	 * @return {@link OverlayContextWrapper}
	 */
	public OverlayContextWrapper< V, E > getOverlayContextWrapper()
	{
		return overlayContextWrapper;
	}

	/**
	 * Trigger a contextChanged "manually". (Normally this happens via
	 * {@link #transformChanged(AffineTransform3D)}.)
	 */
	public synchronized void notifyContextChanged()
	{
		for ( final ContextListener< V > l : listeners.list )
			l.contextChanged( context );
	}

	private synchronized void contextChanged( final Context< V > context )
	{
		this.context = context;
		for ( final ContextListener< V > l : listeners.list )
			l.contextChanged( context );
	}
}
