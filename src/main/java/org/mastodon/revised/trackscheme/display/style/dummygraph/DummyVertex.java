package org.mastodon.revised.trackscheme.display.style.dummygraph;

import org.mastodon.graph.object.AbstractObjectIdVertex;
import org.mastodon.revised.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;

public class DummyVertex extends AbstractObjectIdVertex< DummyVertex, DummyEdge > implements HasTimepoint, HasLabel
{
	private String label;

	private int timepoint;

	DummyVertex()
	{}

	@Override
	public int getTimepoint()
	{
		return timepoint;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel( final String label )
	{
		this.label = label;
	}

	public DummyVertex init( final String label, final int timepoint )
	{
		this.label = label;
		this.timepoint = timepoint;
		return this;
	}

	@Override
	public String toString()
	{
		return label + " @t=" + timepoint;
	}
}
