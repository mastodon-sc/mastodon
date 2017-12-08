package org.mastodon.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.trackscheme.LineageTreeLayout;
import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;
import org.mastodon.revised.trackscheme.display.TrackSchemeFocusActions.NavigatorEtiquette;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import net.imglib2.RealPoint;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

/**
 * TrackSchemeNavigator that implements the 'Midnight-commander-like' behaviour.
 * <p>
 * SelectionModel is independent of focus. Moving the focus with arrow keys doesn't
 * alter selection. Space key toggles selection of focused vertex. When
 * extending the selection with shift+arrow keys, the selection of the currently
 * focused vertex is toggled, then the focus is moved.
 *
 * TODO: RENAME.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class TrackSchemeNavigator implements TransformListener< ScreenTransform >, OffsetHeadersListener
{
	public static final String FOCUS_VERTEX = "ts click focus vertex";
	public static final String NAVIGATE_TO_VERTEX = "ts click navigate to vertex";
	public static final String SELECT = "ts click select";
	public static final String ADD_SELECT = "ts click add to selection";
	public static final String BOX_SELECT = "ts box selection";
	public static final String BOX_ADD_SELECT = "ts box add to selection";

	private static final String[] FOCUS_VERTEX_KEYS = new String[] { "button1", "shift button1" };
	private static final String[] NAVIGATE_TO_VERTEX_KEYS = new String[] { "double-click button1", "shift double-click button1" };
	private static final String[] SELECT_KEYS = new String[] { "button1"};
	private static final String[] ADD_SELECT_KEYS = new String[] { "shift button1"};
	private static final String[] BOX_SELECT_KEYS = new String[] { "button1"};
	private static final String[] BOX_ADD_SELECT_KEYS = new String[] { "shift button1"};

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > navigation;

	private final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection;

	private final ScreenTransform screenTransform;

	private final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus;


	/**
	 * Current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * Current height of horizontal header.
	 */
	private int headerHeight;

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final BehaviourMap behaviourMap;

	private final BoxSelectionBehaviour boxSelect;

	private final BoxSelectionBehaviour boxSelectAdd;

	public TrackSchemeNavigator(
			final InteractiveDisplayCanvasComponent< ScreenTransform > display,
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final AbstractTrackSchemeOverlay graphOverlay,
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
			final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > navigation,
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection )
	{
		this.display = display;
		this.graph = graph;
		this.layout = layout;
		this.graphOverlay = graphOverlay;
		this.focus = focus;
		this.navigation = navigation;
		this.selection = selection;

		behaviourMap = new BehaviourMap();
		new ClickFocusBehaviour().put( behaviourMap );
		new ClickNavigateBehaviour().put( behaviourMap );
		new ClickSelectionBehaviour( SELECT, false ).put( behaviourMap );
		new ClickSelectionBehaviour( ADD_SELECT, true ).put( behaviourMap );
		boxSelect = new BoxSelectionBehaviour( BOX_SELECT, false );
		boxSelect.put( behaviourMap );
		boxSelectAdd = new BoxSelectionBehaviour( BOX_ADD_SELECT, true );
		boxSelectAdd.put( behaviourMap );

		screenTransform = new ScreenTransform();
	}

	public void installActionBindings( final InputActionBindings keybindings, final KeyStrokeAdder.Factory keyConfig, final NavigatorEtiquette etiquette )
	{
		final Actions actions = new Actions( keyConfig, "ts" );
		actions.install( keybindings, "navigator" );

		final TrackSchemeFocusActions.TrackSchemeAutoFocus autoFocus = new TrackSchemeFocusActions.TrackSchemeAutoFocus( layout, focus, navigation );
		TrackSchemeFocusActions.install( actions, graph, layout, autoFocus, selection, etiquette );
	}

	public void installBehaviourBindings( final TriggerBehaviourBindings triggerbindings, final InputTriggerAdder.Factory keyConfig )
	{
		final InputTriggerMap inputMap = new InputTriggerMap();
		final InputTriggerAdder adder = keyConfig.inputTriggerAdder( inputMap, "ts" );
		adder.put( FOCUS_VERTEX, FOCUS_VERTEX_KEYS );
		adder.put( NAVIGATE_TO_VERTEX, NAVIGATE_TO_VERTEX_KEYS );
		adder.put( SELECT, SELECT_KEYS );
		adder.put( ADD_SELECT, ADD_SELECT_KEYS );
		adder.put( BOX_SELECT, BOX_SELECT_KEYS );
		adder.put( BOX_ADD_SELECT, BOX_ADD_SELECT_KEYS );

		triggerbindings.addBehaviourMap( "ts navigator", behaviourMap );
		triggerbindings.addInputTriggerMap( "ts navigator", inputMap );
	}

	/*
	 * COMMON METHODS.
	 */

	private final RealPoint centerPos = new RealPoint( 2 );

	private double ratioXtoY;

	private TrackSchemeVertex getFocusedVertex( final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = focus.getFocusedVertex( ref );
		return ( vertex != null )
				? vertex
				: layout.getClosestActiveVertex( centerPos, ratioXtoY, ref );
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
			final boolean selected = selection.isSelected( vertex );
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( vertex, !selected );
		}
		// See if we can select an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			final boolean selected = selection.isSelected( edge );
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
			super( FOCUS_VERTEX );
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
			super( NAVIGATE_TO_VERTEX );
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

		private final RealPoint lpos;

		private final TrackSchemeVertex ref;

		public BoxSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
			lpos = new RealPoint( 2 );
			ref = graph.vertexRef();
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

				lpos.setPosition( screenTransform.screenToLayoutX( x - headerWidth ), 0 );
				lpos.setPosition( screenTransform.screenToLayoutY( y - headerHeight ), 1 );
				final TrackSchemeVertex v = layout.getClosestActiveVertex( lpos, ratioXtoY, ref );
				focus.focusVertex( v );
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

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized ( screenTransform )
		{
			screenTransform.set( transform );
			centerPos.setPosition( ( transform.getMaxX() + transform.getMinX() ) / 2., 0 );
			centerPos.setPosition( ( transform.getMaxY() + transform.getMinY() ) / 2., 1 );
			ratioXtoY = transform.getXtoYRatio();
		}
	}

	@Override
	public void updateHeadersVisibility( final boolean isVisibleX, final int width, final boolean isVisibleY, final int height )
	{
		headerWidth = isVisibleX ? width : 0;
		headerHeight = isVisibleY ? height : 0;
	}
}
