package net.trackmate.revised.bdv.overlay;

import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Util;
import net.trackmate.revised.bdv.AbstractBehaviours;

public class GraphBevaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends AbstractBehaviours
{
	public static final String MOVE_SPOT = "move spot";

	static final String[] MOVE_SPOT_KEYS = new String[] { "SPACE" };

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer )
	{
		new GraphBevaviours<>( triggerBehaviourBindings, config, overlayGraph, renderer );
	}

	private GraphBevaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer )
	{
		super( triggerBehaviourBindings, "graph", config, new String[] { "bdv" } );
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;

		behaviour( new MoveSpot(), MOVE_SPOT, MOVE_SPOT_KEYS );
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
				System.out.println( Util.printCoordinates( start ) );
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
				// TODO: setUndoPoint
				moving = false;
			}
		}
	};
}
