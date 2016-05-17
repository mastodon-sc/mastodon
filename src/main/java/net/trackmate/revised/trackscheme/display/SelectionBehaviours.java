package net.trackmate.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;

import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour.NamedBehaviourAdder;

import bdv.viewer.TriggerBehaviourBindings;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.trackmate.collection.RefSet;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;

/**
 * TODO: Merge with TrackSchemeNavigator into one class that does everything related to focus and selection?
 *
 * TODO: javadoc
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class SelectionBehaviours implements TransformListener< ScreenTransform >, OffsetHeadersListener
{
	public static final String FOCUS_VERTEX_NAME = "ts click focus vertex";
	public static final String NAVIGATE_TO_VERTEX_NAME = "ts click navigate to vertex";
	public static final String SELECT_NAME = "ts click select";
	public static final String ADD_SELECT_NAME = "ts click add to selection";
	public static final String BOX_SELECT_NAME = "ts box selection";
	public static final String BOX_ADD_SELECT_NAME = "ts box add to selection";

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeNavigation navigation;

	private final TrackSchemeFocus focus;

	private final TrackSchemeSelection selection;

	private final BehaviourMap behaviourMap;

	private final ScreenTransform screenTransform;

	private final BoxSelectionBehaviour boxSelect;

	private final BoxSelectionBehaviour boxSelectAdd;

	/**
	 * current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * current height of horizontal header.
	 */
	private int headerHeight;

	public SelectionBehaviours(
			final InteractiveDisplayCanvasComponent< ScreenTransform > display,
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final AbstractTrackSchemeOverlay graphOverlay,
			final TrackSchemeFocus focus,
			final TrackSchemeNavigation navigation,
			final TrackSchemeSelection selection )
	{
		this.display = display;
		this.graph = graph;
		this.layout = layout;
		this.graphOverlay = graphOverlay;
		this.focus = focus;
		this.navigation = navigation;
		this.selection = selection;

		behaviourMap = new BehaviourMap();
		final NamedBehaviourAdder adder = new NamedBehaviourAdder( behaviourMap );
		adder.put( new ClickFocusBehaviour() );
		adder.put( new ClickNavigateBehaviour() );
		adder.put( new ClickSelectionBehaviour( SELECT_NAME, false ) );
		adder.put( new ClickSelectionBehaviour( ADD_SELECT_NAME, true ) );
		boxSelect = new BoxSelectionBehaviour( BOX_SELECT_NAME, false );
		adder.put( boxSelect );
		boxSelectAdd = new BoxSelectionBehaviour( BOX_ADD_SELECT_NAME, true );
		adder.put( boxSelectAdd );

		screenTransform = new ScreenTransform();
	}

	public void installBehaviourBindings( final TriggerBehaviourBindings triggerbindings, final InputTriggerAdder.Factory keyConfig )
	{
		final InputTriggerMap inputMap = new InputTriggerMap();
		final InputTriggerAdder adder = keyConfig.inputTriggerAdder( inputMap, "ts" );

		adder.put( FOCUS_VERTEX_NAME, "button1", "shift button1" );
		adder.put( NAVIGATE_TO_VERTEX_NAME, "double-click button1", "shift double-click button1" );
		adder.put( SELECT_NAME, "button1" );
		adder.put( ADD_SELECT_NAME, "shift button1" );
		adder.put( BOX_SELECT_NAME, "button1" );
		adder.put( BOX_ADD_SELECT_NAME, "shift button1" );

		triggerbindings.addBehaviourMap( "selection", behaviourMap );
		triggerbindings.addInputTriggerMap( "selection", inputMap );
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
	public void updateHeadersVisibility( final boolean isVisibleX, final int width, final boolean isVisibleY, final int height )
	{
		headerWidth = isVisibleX ? width : 0;
		headerHeight = isVisibleY ? height : 0;
	}

	/*
	 * PRIVATE METHODS
	 */

	private void selectWithin( final int x1, final int y1, final int x2, final int y2, final boolean addToSelection )
	{
		selection.pauseListeners();

		if ( !addToSelection )
			selection.clearSelection();

		final double lx1, ly1, lx2, ly2;
		synchronized ( screenTransform )
		{
			lx1 = screenTransform.screenToLayoutX( x1 );
			ly1 = screenTransform.screenToLayoutY( y1 );
			lx2 = screenTransform.screenToLayoutX( x2 );
			ly2 = screenTransform.screenToLayoutY( y2 );
		}

		final RefSet< TrackSchemeVertex > vs = layout.getVerticesWithin( lx1, ly1, lx2, ly2 );
		final TrackSchemeVertex vertexRef = graph.vertexRef();
		for ( final TrackSchemeVertex v : vs )
		{
			selection.setSelected( v, true );
			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				final TrackSchemeVertex t = e.getTarget( vertexRef );
				if ( vs.contains( t ) )
					selection.setSelected( e, true );
			}
		}
		graph.releaseRef( vertexRef );

		selection.resumeListeners();
	}

	private void select( final int x, final int y, final boolean addToSelection )
	{
		selection.pauseListeners();

		final TrackSchemeVertex vertex = graph.vertexRef();
		final TrackSchemeEdge edge = graph.edgeRef();

		// See if we can select a vertex.
		if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
		{
			final boolean selected = vertex.isSelected();
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( vertex, !selected );
		}
		// See if we can select an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			final boolean selected = edge.isSelected();
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( edge, !selected );
		}
		// Nothing found. clear selection if addToSelection == false
		else if ( !addToSelection )
			selection.clearSelection();

		graph.releaseRef( vertex );
		graph.releaseRef( edge );

		selection.resumeListeners();
	}

	private void navigate( final int x, final int y )
	{
		final TrackSchemeVertex vertex = graph.vertexRef();
		final TrackSchemeEdge edge = graph.edgeRef();

		// See if we can select a vertex.
		if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
		{
			navigation.notifyNavigateToVertex( vertex );
		}
		// See if we can select an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			navigation.notifyNavigateToEdge( edge );
		}

		graph.releaseRef( vertex );
		graph.releaseRef( edge );
	}


	/*
	 * BEHAVIOURS
	 */

	/**
	 * Behaviour to focus a vertex with a mouse click. If the click happens
	 * outside of a vertex, the focus is cleared.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickFocusBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickFocusBehaviour()
		{
			super( FOCUS_VERTEX_NAME );
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			final TrackSchemeVertex ref = graph.vertexRef();
			final TrackSchemeVertex vertex = graphOverlay.getVertexAt( x, y, ref );
			if ( vertex != null )
			{
				focus.focusVertex( vertex );
			}
			else
			{
				// Click outside. We clear the focus.
				focus.focusVertex( null );
			}
			graph.releaseRef( ref );
		}
	}

	/**
	 * Behaviour to navigate to a vertex with a mouse click.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickNavigateBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickNavigateBehaviour()
		{
			super( NAVIGATE_TO_VERTEX_NAME );
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			navigate( x, y );
		}
	}

	/**
	 * Behaviour to select a vertex with a mouse click.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickSelectionBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final boolean addToSelection;

		public ClickSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			select( x, y, addToSelection );
		}
	}

	/**
	 * Behaviour to select vertices and edges inside a bounding box with a mouse
	 * drag.
	 * <p>
	 * The selection happens in layout space, so it also selects vertices inside
	 * dense ranges. A vertex is inside the bounding box if its layout
	 * coordinate is inside the bounding box.
	 */
	private class BoxSelectionBehaviour extends AbstractNamedBehaviour implements DragBehaviour, OverlayRenderer
	{
		/**
		 * Coordinates where mouse dragging started.
		 */
		private int oX, oY;

		/**
		 * Coordinates where mouse dragging currently is.
		 */
		private int eX, eY;

		private boolean dragging = false;

		private boolean ignore = false;

		private final boolean addToSelection;

		public BoxSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
		}

		@Override
		public void init( final int x, final int y )
		{
			oX = x;
			oY = y;
			dragging = false;
			ignore = x < headerWidth || y < headerHeight;
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( ignore )
				return;

			eX = x;
			eY = y;
			if ( !dragging )
			{
				dragging = true;
				display.addOverlayRenderer( this );
			}
			display.repaint();
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( ignore )
				return;

			if ( dragging )
			{
				dragging = false;
				display.removeOverlayRenderer( this );
				display.repaint();
				selectWithin(
						oX - headerWidth,
						oY - headerHeight,
						eX - headerWidth,
						eY - headerHeight,
						addToSelection );
			}
		}

		/**
		 * Draws the selection box, if there is one.
		 */
		@Override
		public void drawOverlays( final Graphics g )
		{
			g.setColor( Color.RED );
			final int x = Math.min( oX, eX );
			final int y = Math.min( oY, eY );
			final int width = Math.abs( eX - oX );
			final int height = Math.abs( eY - oY );
			g.drawRect( x, y, width, height );
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}
}
