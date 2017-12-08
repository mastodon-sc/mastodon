package org.mastodon.revised.model.tag;

import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public interface TagSetModel< V extends Vertex< E >, E extends Edge< V > >
{
	public List< GraphTagPropertyMap< V, E > > getTagSets();

	public GraphTagPropertyMap< V, E > createTagSet( String name );

	public boolean removeTagSet( GraphTagPropertyMap< V, E > tagSet );

}
