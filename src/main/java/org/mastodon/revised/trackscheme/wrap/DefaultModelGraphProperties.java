package org.mastodon.revised.trackscheme.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;

public class DefaultModelGraphProperties< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > >
		implements ModelGraphProperties< V, E >
{
	@Override
	public int getTimepoint( final V v )
	{
		return v.getTimepoint();
	}

	@Override
	public String getLabel( final V v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final V v, final String label )
	{
		v.setLabel( label );
	}
}
