package org.mastodon.mamut.views.table;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.PropertyChangeListener;

public class MamutTableProperties implements TableModelGraphProperties< Spot >
{

	private final ModelGraph graph;

	public MamutTableProperties( final ModelGraph graph )
	{
		this.graph = graph;
	}

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		graph.addVertexLabelListener( listener );

	}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		graph.removeVertexLabelListener( listener );
	}

	@Override
	public String getLabel( final Spot vertex )
	{
		return vertex.getLabel();
	}

	@Override
	public void setLabel( final Spot vertex, final String label )
	{
		vertex.setLabel( label );
	}
}