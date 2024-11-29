/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.ui.util.RamerDouglasPeucker;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.OffsetAxes.OffsetAxesListener;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.OverlayRenderer;
import bdv.viewer.TransformListener;
import net.imglib2.RealLocalizable;

public class FreeformSelectionBehaviour implements DragBehaviour, OverlayRenderer, OffsetAxesListener, TransformListener< ScreenTransform >
{

	public static final String FREEFORM_SELECTION = "freeform selection";

	public static final String FREEFORM_SELECTION_ADD = "freeform add to selection";

	private static final String[] FREEFORM_SELECTION_KEYS = new String[] { "ctrl button1" };

	private static final String[] FREEFORM_SELECTION_ADD_KEYS = new String[] { "ctrl shift button1" };

	public static < V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > > void
			install( final Behaviours behaviours,
					final DataGraphLayout< ?, ? > layout,
					final DataGraph< ?, ? > graph,
					final SelectionModel< DataVertex, DataEdge > selection,
					final FocusModel< DataVertex > focus,
					final DataDisplayPanel< ?, ? > panel )
	{
		final FreeformSelectionBehaviour select = new FreeformSelectionBehaviour( layout, graph, selection, focus, panel, false );
		select.transformChanged( panel.getScreenTransform().get() );
		select.updateAxesSize( panel.getOffsetAxes().getWidth(), panel.getOffsetAxes().getHeight() );
		behaviours.behaviour( select, FREEFORM_SELECTION, FREEFORM_SELECTION_KEYS );

		final FreeformSelectionBehaviour selectAdd = new FreeformSelectionBehaviour( layout, graph, selection, focus, panel, true );
		selectAdd.transformChanged( panel.getScreenTransform().get() );
		selectAdd.updateAxesSize( panel.getOffsetAxes().getWidth(), panel.getOffsetAxes().getHeight() );
		behaviours.behaviour( selectAdd, FREEFORM_SELECTION_ADD, FREEFORM_SELECTION_ADD_KEYS );

		panel.getScreenTransform().listeners().add( select );
		panel.getScreenTransform().listeners().add( selectAdd );

		panel.getOffsetAxes().listeners().add( select );
		panel.getOffsetAxes().listeners().add( selectAdd );

		panel.getDisplay().overlays().add( select );
		panel.getDisplay().overlays().add( selectAdd );
	}

	private final boolean addToSelection;

	private final List< Point > polygon = new ArrayList<>();

	private boolean isDrawing = false;

	private final Component panel;

	private final SelectionModel< DataVertex, DataEdge > selection;

	private final DataGraphLayout< ?, ? > layout;

	private final DataGraph< ?, ? > graph;

	private final FocusModel< DataVertex > focus;

	private final ScreenTransform screenTransform;

	private int axesWidth;

	public FreeformSelectionBehaviour(
			final DataGraphLayout< ?, ? > layout,
			final DataGraph< ?, ? > graph,
			final SelectionModel< DataVertex, DataEdge > selection,
			final FocusModel< DataVertex > focus,
			final Component panel,
			final boolean addToSelection )
	{
		this.layout = layout;
		this.graph = graph;
		this.selection = selection;
		this.focus = focus;
		this.panel = panel;
		this.addToSelection = addToSelection;
		this.screenTransform = new ScreenTransform();
	}

	@Override
	public void init( final int x, final int y )
	{
		polygon.clear();
		polygon.add( new Point( x, y ) );
		isDrawing = true;
	}

	@Override
	public void drag( final int x, final int y )
	{
		if ( isDrawing )
		{
			final Point p = new Point( x, y );
			if ( RamerDouglasPeucker.shouldAddPoint( polygon, p, 0.1 ) )
				polygon.add( p );
			panel.repaint();
		}
	}

	@Override
	public void end( final int x, final int y )
	{
		isDrawing = false;
		polygon.add( new Point( x, y ) );
		select();
	}

	private void select()
	{
		selection.pauseListeners();

		if ( !addToSelection )
			selection.clearSelection();

		// Fetch data points in bounding-box.
		final double x1 = polygon.stream().mapToDouble( Point::getX ).min().getAsDouble();
		final double x2 = polygon.stream().mapToDouble( Point::getX ).max().getAsDouble();
		final double y1 = polygon.stream().mapToDouble( Point::getY ).min().getAsDouble();
		final double y2 = polygon.stream().mapToDouble( Point::getY ).max().getAsDouble();
		final RefSet< DataVertex > vs = layout.getDataVerticesWithin( x1, y1, x2, y2 );

		// Test if these points are in polygon.
		final DataVertex vertexRef = graph.vertexRef();
		for ( final DataVertex v : vs )
		{
			if ( isPointInsidePolygon( v ) )
			{
				selection.setSelected( v, true );
				for ( final DataEdge e : v.outgoingEdges() )
				{
					final DataVertex t = e.getTarget( vertexRef );
					if ( vs.contains( t ) )
						selection.setSelected( e, true );
				}
			}
		}
		graph.releaseRef( vertexRef );

		final Iterator< DataVertex > it = vs.iterator();
		if ( it.hasNext() )
			focus.focusVertex( it.next() );

		selection.resumeListeners();
	}

	private boolean isPointInsidePolygon( final RealLocalizable point )
	{
		final int n = polygon.size();
		boolean inside = false;

		final double xl = point.getDoublePosition( 0 );
		final double yl = point.getDoublePosition( 1 );
		final double xs = screenTransform.layoutToScreenX( xl ) + axesWidth;
		final double ys = screenTransform.layoutToScreenY( yl );

		for ( int i = 0, j = n - 1; i < n; j = i++ )
		{
			final Point pi = polygon.get( i );
			final Point pj = polygon.get( j );

			if ( ( pi.y > ys ) != ( pj.y > ys ) &&
					( xs < ( pj.x - pi.x ) * ( ys - pi.y ) / ( pj.y - pi.y ) + pi.x ) )
			{
				inside = !inside;
			}
		}
		return inside;
	}

	private final Path2D path = new Path2D.Double();

	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( !isDrawing )
			return;

		path.reset();
		path.moveTo( polygon.get( 0 ).x, polygon.get( 0 ).y );
		for ( int i = 1; i < polygon.size(); i++ )
			path.lineTo( polygon.get( i ).x, polygon.get( i ).y );
		path.closePath();

		g.setColor( Color.RED );
		final Graphics2D g2 = ( Graphics2D ) g;
		g2.draw( path );
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
	public void updateAxesSize( final int width, final int height )
	{
		axesWidth = width;
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.GRAPHER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( FREEFORM_SELECTION, FREEFORM_SELECTION_KEYS, "Freeform selection in the grapher." );
			descriptions.add( FREEFORM_SELECTION_ADD, FREEFORM_SELECTION_ADD_KEYS, "Freeform add to selection in the grapher." );
		}
	}
}
