package org.mastodon.model.tag;

import org.scijava.listeners.Listeners;

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

	/*
	 TODO: It is very confusing that one can get a TagSetStructure from TagSetModel
	       and modify it, but this does not work as expected.
	       Solutions(?): Get only a copy of TSS, or get a read-only TSS.
	 */
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
