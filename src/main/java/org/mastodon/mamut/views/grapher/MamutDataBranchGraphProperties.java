package org.mastodon.mamut.views.grapher;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.views.grapher.datagraph.DataGraphProperties;

/**
 * Mamut app-specific implementation of {@link DataGraphProperties} for branch
 * data.
 *
 * @author Jean-Yves Tinevez
 */
public class MamutDataBranchGraphProperties implements DataGraphProperties< BranchSpot, BranchLink >
{

	private final ModelGraph graph;

	private final int nSources;

	public MamutDataBranchGraphProperties( final ModelGraph graph, final int nSources )
	{
		this.graph = graph;
		this.nSources = nSources;
	}

	@Override
	public String getLabel( final BranchSpot v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final BranchSpot v, final String label )
	{
		v.setLabel( label );
	}

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< BranchSpot > listener )
	{}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< BranchSpot > vertexLabelListener )
	{}

	@Override
	public int getTimepoint( final BranchSpot v )
	{
		return v.getTimepoint();
	}

	@Override
	public void notifyGraphChanged()
	{
		graph.notifyGraphChanged();
	}

	@Override
	public int getNSources()
	{
		return nSources;
	}
}
