package org.mastodon.views.trackscheme.display;

import java.util.Iterator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.scijava.ui.behaviour.util.Actions;

import net.imglib2.util.Cast;

public class BranchTrackSchemeEditLabelAction
{

	private static final String[] EDIT_FOCUS_LABEL_KEYS = new String[] { "ENTER" };

	public static < BV extends Vertex< BE >, BE extends Edge< BV >, V extends Vertex< E > & HasLabel, E extends Edge< V > > void install(
			final Actions actions,
			final TrackSchemePanel panel,
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
			final UndoPointMarker undoPointMarker,
			final BranchGraph< BV, BE, V, E > branchGraph )
	{
		final BranchTrackSchemeEditLabelActionImp< BV, BE, V, E > editBranchVerticesLabelAction =
				new BranchTrackSchemeEditLabelActionImp<>( focus, undoPointMarker, panel, branchGraph );
		panel.getScreenTransform().listeners().add( editBranchVerticesLabelAction );
		panel.getOffsetHeaders().listeners().add( editBranchVerticesLabelAction );
		actions.namedAction( editBranchVerticesLabelAction, EDIT_FOCUS_LABEL_KEYS );
	}

	private static class BranchTrackSchemeEditLabelActionImp< BV extends Vertex< BE >, BE extends Edge< BV >, V extends Vertex< E > & HasLabel, E extends Edge< V > >
			extends EditFocusVertexLabelAction
	{

		private static final long serialVersionUID = 1L;

		private final TrackSchemeGraph< BV, BE > trackSchemeGraph;

		private final BranchGraph< BV, BE, V, E > branchGraph;

		protected BranchTrackSchemeEditLabelActionImp(
				final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
				final UndoPointMarker undoPointMarker,
				final TrackSchemePanel panel,
				final BranchGraph< BV, BE, V, E > branchGraph )
		{
			super( focus, undoPointMarker, panel );
			this.branchGraph = branchGraph;
			this.trackSchemeGraph = Cast.unchecked( panel.getGraph() );
		}

		@Override
		protected void changeLabel( final TrackSchemeVertex vertex, final String label )
		{
			final BV branchSpot = trackSchemeGraph.getVertexMap().getLeft( vertex );
			final Iterator< V > it = branchGraph.vertexBranchIterator( branchSpot );
			while ( it.hasNext() )
			{
				final V v = it.next();
				v.setLabel( label );
			}
			undoPointMarker.setUndoPoint();
		}
	}
}
