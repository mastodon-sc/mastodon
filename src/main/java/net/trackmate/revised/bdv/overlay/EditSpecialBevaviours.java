package net.trackmate.revised.bdv.overlay;

import static net.trackmate.revised.bdv.overlay.EditBevaviours.POINT_SELECT_DISTANCE_TOLERANCE;

import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;
import bdv.viewer.ViewerPanel;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.revised.bdv.AbstractBehaviours;
import net.trackmate.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import net.trackmate.revised.model.mamut.Spot;
import net.trackmate.undo.UndoPointMarker;

public class EditSpecialBevaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends AbstractBehaviours
{
	public static final String ADD_SPOT_AND_LINK_IT = "add linked spot";

	static final String[] ADD_SPOT_AND_LINK_IT_KEYS = new String[] { "A" };

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final UndoPointMarker undo;

	private final ViewerPanel viewer;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		new EditSpecialBevaviours<>( triggerBehaviourBindings, config, viewer, overlayGraph, renderer, undo );
	}


	private EditSpecialBevaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		super( triggerBehaviourBindings, "graph-special", config, new String[] { "bdv" } );
		this.viewer = viewer;
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;
		this.undo = undo;

		behaviour( new AddSpotAndLinkIt(), ADD_SPOT_AND_LINK_IT, ADD_SPOT_AND_LINK_IT_KEYS );
	}


	private class AddSpotAndLinkIt implements DragBehaviour
	{
		private final V source;

		private final V target;

		private final E edge;

		private final double[] start;

		private final double[] pos;

		private boolean moving;

		private final JamaEigenvalueDecomposition eig;

		private final double[][] mat;

		public AddSpotAndLinkIt()
		{
			source = overlayGraph.vertexRef();
			target = overlayGraph.vertexRef();
			edge = overlayGraph.edgeRef();
			start = new double[ 3 ];
			pos = new double[ 3 ];
			moving = false;
			eig = new JamaEigenvalueDecomposition( 3 );
			mat = new double[ 3 ][ 3 ];
		}

		@Override
		public void init( final int x, final int y )
		{
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, source ) != null )
			{
				// Get vertex we clicked inside.
				renderer.getGlobalPosition( x, y, start );
				source.localize( pos );
				LinAlgHelpers.subtract( pos, start, start );

				// Move to next time point.
				viewer.nextTimePoint();

				// Compute new radius as mean of ellipse semi-axes.
				source.getCovariance( mat );
				eig.decompose( mat );
				final double[] eigs = eig.getRealEigenvalues();
				double radius = 0;
				for ( final double e : eigs )
					radius += Math.sqrt( e ) ;
				radius *= Spot.nSigmas / eigs.length;

				// Create new vertex under click location.
				final int timepoint = renderer.getCurrentTimepoint();
				overlayGraph.addVertex( timepoint, pos, radius, target );

				// Link it to source vertex.
				overlayGraph.addEdge( source, target, edge );

				overlayGraph.notifyGraphChanged();
				moving = true;
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( moving )
			{
				renderer.getGlobalPosition( x, y, pos );
				LinAlgHelpers.add( pos, start, pos );
				target.setPosition( pos );
			}
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

	}

}
