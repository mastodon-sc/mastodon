package org.mastodon.revised.model.tag;

import org.mastodon.util.Listeners;

/**
 * Assigns tags to vertices and edges of a graph, according to a
 * {@link TagSetStructure}.
 *
 * @param <V>
 *            the type of the vertices in the graph.
 * @param <E>
 *            the type of the edges in the graph.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public interface TagSetModel< V, E >
{
	ObjTags< V > getVertexTags();

	ObjTags< E > getEdgeTags();

	TagSetStructure getTagSetStructure();

	void setTagSetStructure( final TagSetStructure tss );

	interface TagSetModelListener
	{
		void tagSetStructureChanged();
	}

	Listeners< TagSetModelListener > listeners();

	void pauseListeners();

	void resumeListeners();

	void clear();
}
