package net.trackmate.revised.bdv.overlay;

import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.revised.bdv.AbstractBehaviours;
import net.trackmate.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import net.trackmate.undo.UndoPointMarker;

public class EditBevaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends AbstractBehaviours
{
	public static final String MOVE_SPOT = "move spot";
	public static final String ADD_SPOT = "add spot";
	public static final String INCREASE_SPOT_RADIUS = "increase spot radius";
	public static final String INCREASE_SPOT_RADIUS_ALOT = "increase spot radius a lot";
	public static final String INCREASE_SPOT_RADIUS_ABIT = "increase spot radius a bit";
	public static final String DECREASE_SPOT_RADIUS = "decrease spot radius";
	public static final String DECREASE_SPOT_RADIUS_ALOT = "decrease spot radius a lot";
	public static final String DECREASE_SPOT_RADIUS_ABIT = "decrease spot radius a bit";

	static final String[] ADD_SPOT_KEYS = new String[] { "A" };
	static final String[] MOVE_SPOT_KEYS = new String[] { "SPACE" };
	static final String[] INCREASE_SPOT_RADIUS_KEYS = new String[] { "E" };
	static final String[] INCREASE_SPOT_RADIUS_KEYS_ALOT = new String[] { "shift E" };
	static final String[] INCREASE_SPOT_RADIUS_KEYS_ABIT = new String[] { "control E" };
	static final String[] DECREASE_SPOT_RADIUS_KEYS = new String[] { "Q" };
	static final String[] DECREASE_SPOT_RADIUS_KEYS_ALOT = new String[] { "shift Q" };
	static final String[] DECREASE_SPOT_RADIUS_KEYS_ABIT = new String[] { "control Q" };

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	/** Minimal radius below which changes in vertex size are rejected. */
	private static final double MIN_RADIUS = 1.0;

	/**
	 * Ratio by which we change the radius upon change radius action.
	 */
	private static final double NORMAL_RADIUS_CHANGE = 0.1;

	/**
	 * Ratio by which we change the radius upon change radius a but action.
	 */
	private static final double ABIT_RADIUS_CHANGE = 0.01;

	/**
	 * Ratio by which we change the radius upon change radius a lot action.
	 */
	private static final double ALOT_RADIUS_CHANGE = 1.;

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
		behaviour( new ResizeSpot( NORMAL_RADIUS_CHANGE ), INCREASE_SPOT_RADIUS, INCREASE_SPOT_RADIUS_KEYS );
		behaviour( new ResizeSpot( ALOT_RADIUS_CHANGE ), INCREASE_SPOT_RADIUS_ALOT, INCREASE_SPOT_RADIUS_KEYS_ALOT );
		behaviour( new ResizeSpot( ABIT_RADIUS_CHANGE ), INCREASE_SPOT_RADIUS_ABIT, INCREASE_SPOT_RADIUS_KEYS_ABIT );
		behaviour( new ResizeSpot( -NORMAL_RADIUS_CHANGE / ( 1 + NORMAL_RADIUS_CHANGE ) ), DECREASE_SPOT_RADIUS, DECREASE_SPOT_RADIUS_KEYS );
		behaviour( new ResizeSpot( -ALOT_RADIUS_CHANGE / ( 1 + ALOT_RADIUS_CHANGE ) ), DECREASE_SPOT_RADIUS_ALOT, DECREASE_SPOT_RADIUS_KEYS_ALOT );
		behaviour( new ResizeSpot( -ABIT_RADIUS_CHANGE / ( 1 + ABIT_RADIUS_CHANGE ) ), DECREASE_SPOT_RADIUS_ABIT, DECREASE_SPOT_RADIUS_KEYS_ABIT );
	}

	private class AddSpot implements ClickBehaviour
	{
		private final double[] pos;

		private final V tmp;

		public AddSpot()
		{
			pos = new double[ 3 ];
			tmp = overlayGraph.vertexRef();
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, tmp ) != null )
			{
				// Do not create a spot if we click inside an existing spot.
				return;
			}

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

	private class ResizeSpot implements ClickBehaviour
	{

		private final double[][] mat;

		private final double factor;

		private final JamaEigenvalueDecomposition eig;

		public ResizeSpot( final double factor )
		{
			this.factor = factor;
			mat = new double[ 3 ][ 3 ];
			eig = new JamaEigenvalueDecomposition( 3 );
		}

		@Override
		public void click( final int x, final int y )
		{
			final V vertex = overlayGraph.vertexRef();
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
			{
				// Scale the covariance matrix.
				vertex.getCovariance( mat );
				LinAlgHelpers.scale( mat, 1 + factor, mat );

				// Check if the min radius is not too small.
				eig.decomposeSymmetric( mat );
				final double[] eigVals = eig.getRealEigenvalues();
				for ( final double eigVal : eigVals )
					if ( eigVal < MIN_RADIUS )
						return;

				vertex.setCovariance( mat );
				overlayGraph.notifyGraphChanged();
				undo.setUndoPoint();
			}
			overlayGraph.releaseRef( vertex );
		}
	}
}
