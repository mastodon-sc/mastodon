package org.mastodon.views.bdv;

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
import net.imglib2.ui.TransformListener;

/**
 * A model vertices {@link ContextProvider}. It provides a
 * {@link OverlayContextWrapper} (always the same) which wraps a
 * {@link OverlayContext} (always the same). It must be registered to
 * {@link bdv.viewer.ViewerPanel#addRenderTransformListener(TransformListener)}
 * to be updated with the viewer transform.
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

	public BdvContextProvider( final String name,
			final OverlayGraph< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > overlayGraph,
			final OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > renderer )
	{
		this.name = name;
		listeners = new Listeners.SynchronizedList<>( l -> l.contextChanged( context ) );
		overlayContext = new OverlayContext<>( overlayGraph, renderer );
		new OverlayContextWrapper<>( overlayContext, this::contextChanged );
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
