package org.mastodon.mamut.feature.branch.exampleGraph;

import javax.annotation.Nonnull;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

public abstract class AbstractExampleGraph
{
	@Nonnull
	protected final Model model;

	@Nonnull
	protected final ModelGraph modelGraph;

	private boolean branchGraphRequiresRebuild;

	@Nonnull
	private final ModelBranchGraph modelBranchGraph;

	public AbstractExampleGraph()
	{
		this.model = new Model();
		this.modelGraph = model.getGraph();
		this.modelBranchGraph = model.getBranchGraph();
		this.branchGraphRequiresRebuild = false;
	}

	@Nonnull
	public Model getModel()
	{
		rebuiltGraphIfRequired();
		return model;
	}

	public BranchSpot getBranchSpot(@Nonnull Spot spot)
	{
		rebuiltGraphIfRequired();
		return modelBranchGraph.getBranchVertex( spot, modelBranchGraph.vertexRef() );
	}

	private void rebuiltGraphIfRequired()
	{
		if ( ! branchGraphRequiresRebuild )
			return;
		this.model.getBranchGraph().graphRebuilt();
		branchGraphRequiresRebuild = false;
	}

	protected Spot addNode( @Nonnull String label, int timepoint, double[] xyz )
	{
		Spot spot = modelGraph.addVertex();
		spot.init( timepoint, xyz, 0 );
		spot.setLabel( label );
		branchGraphRequiresRebuild = true;
		return spot;
	}

	protected void addEdge( @Nonnull Spot source, @Nonnull Spot target )
	{
		modelGraph.addEdge( source, target );
		branchGraphRequiresRebuild = true;
	}
}
