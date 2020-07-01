package org.mastodon.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class TagSetGraphColorGenerator< V extends Vertex< E >, E extends Edge< V > > implements GraphColorGenerator< V, E >
{
	private final ObjTagMap< V, TagSetStructure.Tag > vertexTags;

	private final ObjTagMap< E, TagSetStructure.Tag > edgeTags;

	public TagSetGraphColorGenerator( final TagSetModel< V, E > tagSetModel, final TagSetStructure.TagSet tagSet )
	{
		vertexTags = tagSetModel.getVertexTags().tags( tagSet );
		edgeTags = tagSetModel.getEdgeTags().tags( tagSet );
	}

	@Override
	public int color( final V vertex )
	{
		final TagSetStructure.Tag tag = vertexTags.get( vertex );
		return tag == null ? 0 : tag.color();
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		final TagSetStructure.Tag tag = edgeTags.get( edge );
		return tag == null ? 0 : tag.color();
	}
}
