package org.mastodon.views.trackscheme.display;

import net.imglib2.util.Cast;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.scijava.ui.behaviour.util.Actions;

public class BranchTrackSchemeEditLabelAction extends EditFocusVertexLabelAction
{
	private static final String[] EDIT_FOCUS_LABEL_KEYS = new String[] { "ENTER" };

	private final Model model;

	private final TrackSchemeGraph<BranchSpot, BranchLink> trackSchemeGraph;

	private final ModelBranchGraph branchGraph;

	private final ModelGraph graph;

	public static void install( final Actions actions, final TrackSchemePanel panel, final FocusModel<TrackSchemeVertex, TrackSchemeEdge> focus, final Model model )
	{
		final EditFocusVertexLabelAction editFocusVertexLabelAction = new BranchTrackSchemeEditLabelAction( focus, model, panel );
		panel.getScreenTransform().listeners().add( editFocusVertexLabelAction );
		panel.getOffsetHeaders().listeners().add( editFocusVertexLabelAction );
		actions.namedAction( editFocusVertexLabelAction, EDIT_FOCUS_LABEL_KEYS );
	}

	protected BranchTrackSchemeEditLabelAction( FocusModel<TrackSchemeVertex, TrackSchemeEdge> focus, Model model, TrackSchemePanel panel )
	{
		super( focus, model, panel );
		this.model = model;
		this.graph = model.getGraph();
		this.branchGraph = model.getBranchGraph();
		this.trackSchemeGraph = Cast.unchecked(panel.getGraph());
	}

	@Override
	protected void changeLabel( TrackSchemeVertex vertex, String label )
	{
		vertex.setLabel( label );
		BranchSpot branchSpot = trackSchemeGraph.getVertexMap().getLeft( vertex );
		Spot vertexRef = graph.vertexRef();
		Spot spot = branchGraph.getLinkedVertex( branchSpot, vertexRef );
		while(spot.incomingEdges().size() == 1)
		{
			spot.setLabel( label );
			spot = spot.incomingEdges().iterator().next().getSource( spot );
			if(spot.outgoingEdges().size() != 1)
				break;
		}
		graph.releaseRef( spot );
		model.setUndoPoint();
	}
}
