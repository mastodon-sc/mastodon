package org.mastodon.revised.model.tag;

import java.io.File;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;

public interface TagSetModel< V extends Vertex< E >, E extends Edge< V > >
{
	public List< GraphTagPropertyMap< V, E > > getTagSets();

	public GraphTagPropertyMap< V, E > createTagSet( String name );

	public boolean removeTagSet( GraphTagPropertyMap< V, E > tagSet );

	public void saveRaw( File baseFolder, GraphToFileIdMap< V, E > graphToFileIdMap );

	public void loadRaw( File baseFolder, FileIdToGraphMap< V, E > fileIdToGraphMap );

}
