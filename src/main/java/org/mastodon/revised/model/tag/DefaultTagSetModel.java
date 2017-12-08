package org.mastodon.revised.model.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.tag.TagSetPanel.GraphTagPropertyMapFactory;

public class DefaultTagSetModel< V extends Vertex< E >, E extends Edge< V > > implements TagSetModel< V, E >
{

	private final GraphTagPropertyMapFactory factory;

	private final List< GraphTagPropertyMap< V, E > > tagSets;

	public DefaultTagSetModel( final ReadOnlyGraph< V, E > graph )
	{
		this.factory = ( name ) -> new GraphTagPropertyMap<>( name, graph );
		this.tagSets = new ArrayList<>();
	}

	@Override
	public List< GraphTagPropertyMap< V, E > > getTagSets()
	{
		return Collections.unmodifiableList( tagSets );
	}

	@Override
	public GraphTagPropertyMap< V, E > createTagSet( final String name )
	{
		@SuppressWarnings( "unchecked" )
		final GraphTagPropertyMap< V, E > tagSet = ( GraphTagPropertyMap< V, E > ) factory.create( name );
		tagSets.add( tagSet );
		return tagSet;
	}

	@Override
	public boolean removeTagSet( final GraphTagPropertyMap< V, E > tagSet )
	{
		return tagSets.remove( tagSet );
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		for ( final GraphTagPropertyMap< V, E > tagSet : tagSets )
		{
			str.append( "\n" + tagSet.getName() + ":" );
			for ( final Tag tag : tagSet.getTags() )
			{
				str.append( "\n  - " + tag.label() + ":" );
				str.append( "\n    V: " );
				for ( final V v : tagSet.getTaggedVertices( tag ) )
					str.append( v + "," );
				str.append( "\n    E: " );
				for ( final E e : tagSet.getTaggedEdges( tag ) )
					str.append( e + "," );
			}
		}

		return str.toString();
	}
}
