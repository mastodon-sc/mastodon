package net.trackmate.revised.bdv.overlay;

import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.revised.bdv.AbstractBehaviours;
import net.trackmate.undo.UndoPointMarker;

public class EditBevaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends AbstractBehaviours
{
	public static final String MOVE_SPOT = "move spot";
	public static final String ADD_SPOT = "add spot";

	static final String[] ADD_SPOT_KEYS = new String[] { "A" };
	static final String[] MOVE_SPOT_KEYS = new String[] { "SPACE" };

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final UndoPointMarker undo;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		new EditBevaviours<>( triggerBehaviourBindings, config, overlayGraph, renderer, undo );
	}

	private EditBevaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		super( triggerBehaviourBindings, "graph", config, new String[] { "bdv" } );
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;
		this.undo = undo;

		behaviour( new MoveSpot(), MOVE_SPOT, MOVE_SPOT_KEYS );
		behaviour( new AddSpot(), ADD_SPOT, ADD_SPOT_KEYS );
	}

	private class AddSpot implements ClickBehaviour
	{
		private final double[] pos;

		public AddSpot()
		{
			pos = new double[ 3 ];
		}

		@Override
		public void click( final int x, final int y )
		{
			final int timepoint = renderer.getCurrentTimepoint();
			renderer.getGlobalPosition( x, y, pos );
			final V ref = overlayGraph.vertexRef();
			overlayGraph.addVertex( timepoint, pos, 10, ref );
			overlayGraph.releaseRef( ref );
			overlayGraph.notifyGraphChanged();
			undo.setUndoPoint();
		}
	}

	private class MoveSpot implements DragBehaviour
	{
		private final V vertex;

		private final double[] start;

		private final double[] pos;

		private boolean moving;

		public MoveSpot()
		{
			vertex = overlayGraph.vertexRef();
			start = new double[ 3 ];
			pos = new double[ 3 ];
			moving = false;
		}

		@Override
		public void init( final int x, final int y )
		{
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
			{
				renderer.getGlobalPosition( x, y, start );
				vertex.localize( pos );
				LinAlgHelpers.subtract( pos, start, start );
				moving = true;
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			renderer.getGlobalPosition( x, y, pos );
			LinAlgHelpers.add( pos, start, pos );
			vertex.setPosition( pos );

		}

		@Override
		public void end( final int x, final int y )
		{
			if ( moving )
			{
				undo.setUndoPoint();
				moving = false;
			}
		}
	};
}
