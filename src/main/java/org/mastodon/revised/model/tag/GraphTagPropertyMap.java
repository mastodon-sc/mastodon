package org.mastodon.revised.model.tag;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;

/**
 * Manages a collection of tags for a graph.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the vertices in the graph.
 * @param <E>
 *            the type of the edges in the graph.
 */
public class GraphTagPropertyMap< V extends Vertex< E >, E extends Edge< V > >
{

	private String name;

	private final TagPropertyMap< V, Tag > vertexTag;

	private final TagPropertyMap< E, Tag > edgeTag;

	private final List< Tag > tags;

	/**
	 * The internal Tag id generator. Starts at 0.
	 */
	private final AtomicInteger tagID;

	public GraphTagPropertyMap( final String name, final ReadOnlyGraph< V, E > graph )
	{
		this.name = name;
		this.vertexTag = new TagPropertyMap<>( graph.vertices() );
		this.edgeTag = new TagPropertyMap<>( graph.edges() );
		this.tags = new ArrayList<>();
		this.tagID = new AtomicInteger( 0 );
	}

	public String getName()
	{
		return name;
	}

	public void setName( final String name )
	{
		this.name = name;
	}

	/**
	 * Creates a new tag.
	 *
	 * @return the new tag.
	 */
	public Tag createTag()
	{
		final Random ran = new Random();
		final Tag tag = new Tag(
				"label " + tagID.get(),
				new Color( ran.nextInt() ) );
		tags.add( tag );
		return tag;
	}

	/**
	 * Returns an unmodifiable view of the tags registered in this property.
	 *
	 * @return a view of the tags.
	 */
	public Collection< Tag > getTags()
	{
		return Collections.unmodifiableList( tags );
	}

	/**
	 * Tags the objects in the selection with the specified tag. If the tag is
	 * not registered in this property, this method does nothing.
	 *
	 * @param selection
	 *            the selection.
	 * @param tag
	 *            the tag to apply.
	 */
	public void tag( final SelectionModel< V, E > selection, final Tag tag )
	{
		if ( !tags.contains( tag ) )
			return;

		vertexTag.set( selection.getSelectedVertices(), tag );
		edgeTag.set( selection.getSelectedEdges(), tag );
	}

	/**
	 * Clears the tag (if any) on the objects in the selection.
	 *
	 * @param selection
	 *            the selection.
	 */
	public void clearTag( final SelectionModel< V, E > selection )
	{
		vertexTag.remove( selection.getSelectedVertices() );
		edgeTag.remove( selection.getSelectedEdges() );
	}

	/**
	 * Removes the specified tag from this property. If there are objects tagged
	 * with the specified tag, their tag is cleared.
	 *
	 * @param tag
	 *            the tag to remove.
	 */
	public void removeTag( final Tag tag )
	{
		vertexTag.clearTag( tag );
		edgeTag.clearTag( tag );
		tags.remove( tag );
	}

	public void clear()
	{
		vertexTag.clear();
		edgeTag.clear();
		tags.clear();
	}

	public Collection< V > getTaggedVertices( final Tag tag )
	{
		return vertexTag.getTaggedWith( tag ) == null ? Collections.emptyList() : vertexTag.getTaggedWith( tag );
	}

	public Collection< E > getTaggedEdges( final Tag tag )
	{
		return edgeTag.getTaggedWith( tag ) == null ? Collections.emptyList() : edgeTag.getTaggedWith( tag );
	}
}
