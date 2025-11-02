package org.mastodon.mamut.views.grapher;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.views.grapher.datagraph.DataGraphProperties;

public class MamutDataGraphProperties implements DataGraphProperties< Spot, Link >
{

	private final ModelGraph graph;

	private final int nSources;

	public MamutDataGraphProperties( final ModelGraph graph, final int nSources )
	{
		this.graph = graph;
		this.nSources = nSources;
	}

	@Override
	public String getLabel( final Spot v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final Spot v, final String label )
	{
		v.setLabel( label );
	}

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		graph.addVertexLabelListener( listener );
	}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< Spot > vertexLabelListener )
	{
		graph.removeVertexLabelListener( vertexLabelListener );
	}

	@Override
	public int getTimepoint( final Spot v )
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
