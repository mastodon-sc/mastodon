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
package org.mastodon.views.grapher.display;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.OffsetAxes.OffsetAxesListener;
import org.mastodon.views.trackscheme.display.TrackSchemeZoom;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.OverlayRenderer;
import bdv.viewer.TransformListener;

/**
 * Drag behaviour that implements a zoom rectangle in a grapher view.
 *
 * @author Jean-Yves Tinevez
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class DataDisplayZoom< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
		extends AbstractNamedBehaviour
		implements DragBehaviour, OffsetAxesListener, TransformListener< ScreenTransform >
{
	private static final String TOGGLE_ZOOM = "box zoom";

	private static final String[] TOGGLE_ZOOM_KEYS = new String[] { "Z" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.GRAPHER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( TOGGLE_ZOOM, TOGGLE_ZOOM_KEYS, "Zoom to area specified by dragging a box." );
		}
	}

	public static < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > void install( final Behaviours behaviours, final DataDisplayPanel panel )
	{
		final DataDisplayZoom< V, E > zoom = new DataDisplayZoom<>( panel );

		// Create and register overlay.
		zoom.transformChanged( panel.getScreenTransform().get() );
		zoom.updateAxesSize( panel.getOffsetAxes().getWidth(), panel.getOffsetAxes().getHeight() );
		// put the overlay first, so that is below the graph rendering.
		panel.getDisplay().overlays().add( zoom.overlay );
		panel.getScreenTransform().listeners().add( zoom );
		panel.getOffsetAxes().listeners().add( zoom );

		behaviours.namedBehaviour( zoom, TOGGLE_ZOOM_KEYS );
	}

	private static final ImageIcon ZOOM_ICON = new ImageIcon( TrackSchemeZoom.class.getResource( "zoom.png" ) );

	public static final Color ZOOM_GRAPH_OVERLAY_COLOR = Color.BLUE.darker();

	private final DataDisplayPanel panel;

	private final InertialScreenTransformEventHandler transformEventHandler;

	private boolean dragging;

	private int axesWidth;

	private int axesHeight;

	private final ScreenTransform screenTransform;

	private final ZoomOverlay overlay;

	private DataDisplayZoom( final DataDisplayPanel panel )
	{
		super( TOGGLE_ZOOM );
		this.panel = panel;
		this.transformEventHandler = panel.getTransformEventHandler();

		dragging = false;
		screenTransform = new ScreenTransform();
		overlay = new ZoomOverlay();
	}

	@Override
	public void updateAxesSize( final int width, final int height )
	{
		axesWidth = width;
		axesHeight = height;
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized ( screenTransform )
		{
			screenTransform.set( transform );
		}
	}

	@Override
	public void init( final int x, final int y )
	{
		overlay.ox = x;
		overlay.oy = y;
		overlay.ex = x;
		overlay.ey = y;
		dragging = true;
		overlay.paint = true;
	}

	@Override
	public void drag( final int x, final int y )
	{
		if ( dragging )
		{
			overlay.ex = x;
			overlay.ey = y;
			panel.repaint();
		}
	}

	@Override
	public void end( final int x, final int y )
	{
		if ( dragging )
		{
			dragging = false;
			overlay.paint = false;

			final int x1 = Math.min( overlay.ox, overlay.ex ) - axesWidth;
			final int x2 = Math.max( overlay.ox, overlay.ex ) - axesWidth;
			final int y1 = Math.min( overlay.oy, overlay.ey ) - axesHeight;
			final int y2 = Math.max( overlay.oy, overlay.ey ) - axesHeight;
			final double[] screen1 = new double[] { x1, y1 };
			final double[] screen2 = new double[] { x2, y2 };
			final double[] layout1 = new double[ 2 ];
			final double[] layout2 = new double[ 2 ];

			screenTransform.applyInverse( layout1, screen1 );
			screenTransform.applyInverse( layout2, screen2 );
			transformEventHandler.zoomTo(
					layout1[ 0 ],
					layout2[ 0 ],
					layout1[ 1 ],
					layout2[ 1 ] );
		}
	}

	private class ZoomOverlay implements OverlayRenderer
	{

		public int ey;

		public int ex;

		public int oy;

		public int ox;

		private boolean paint;

		public ZoomOverlay()
		{
			paint = false;
		}

		@Override
		public void drawOverlays( final Graphics g )
		{
			if ( !paint )
				return;

			final int x1 = Math.min( ox, ex );
			final int x2 = Math.max( ox, ex );
			final int y1 = Math.min( oy, ey );
			final int y2 = Math.max( oy, ey );

			g.setColor( ZOOM_GRAPH_OVERLAY_COLOR );
			g.drawRect( x1, y1, x2 - x1, y2 - y1 );
			g.drawRect( x1 + 1, y1 + 1, x2 - x1 - 2, y2 - y1 - 2 );
			g.drawImage( ZOOM_ICON.getImage(), x1 + 3, y1 + 3, null );
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}
}
