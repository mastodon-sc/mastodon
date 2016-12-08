package org.mastodon.revised.ui.selection;

import java.util.Collection;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.Vertex;

/**
 * A class that manages a selection of vertices and edges of a graph.
 * <p>
 * Created instances register themselves as a {@link GraphListener} to always
 * return consistent results. For instance, if a vertex marked as selected in
 * this class is later removed from the graph, the
 * {@link #getSelectedVertices()} method will not return it.
 * <p>
 * TODO: less severe synchronization
 *
 * @author Tobias Pietzsch
 *
 * @param <V>
 *            the type of the vertices.
 * @param <E>
 *            the type of the edges.
 */
public interface Selection< V extends Vertex< E >, E extends Edge< V > >
{
	/**
	 * Get the selected state of a vertex.
	 *
	 * @param v
	 *            a vertex.
	 * @return {@code true} if specified vertex is selected.
	 */
	public boolean isSelected( final V vertex );

	/**
	 * Get the selected state of an edge.
	 *
	 * @param e
	 *            an edge.
	 * @return {@code true} if specified edge is selected.
	 */
	public boolean isSelected( final E edge );

	/**
	 * Sets the selected state of a vertex.
	 *
	 * @param v
	 *            a vertex.
	 * @param selected
	 *            selected state to set for specified vertex.
	 */
	public void setSelected( final V vertex, final boolean selected );

	/**
	 * Sets the selected state of an edge.
	 *
	 * @param e
	 *            an edge.
	 * @param selected
	 *            selected state to set for specified edge.
	 */
	public void setSelected( final E edge, final boolean selected );

	/**
	 * Toggles the selected state of a vertex.
	 *
	 * @param v
	 *            a vertex.
	 */
	public void toggle( final V vertex );

	/**
	 * Toggles the selected state of an edge.
	 *
	 * @param e
	 *            an edge.
	 */
	public void toggle( final E edge );

	/**
	 * Sets the selected state of a collection of edges.
	 *
	 * @param edges
	 *            the edge collection.
	 * @param selected
	 *            selected state to set for specified edge collection.
	 * @return {@code true} if the selection was changed by this call.
	 */
	public boolean setEdgesSelected( final Collection< E > edges, final boolean selected );

	/**
	 * Sets the selected state of a collection of vertices.
	 *
	 * @param vertices
	 *            the vertex collection.
	 * @param selected
	 *            selected state to set for specified vertex collection.
	 * @return {@code true} if the selection was changed by this call.
	 */
	public boolean setVerticesSelected( final Collection< V > vertices, final boolean selected );

	/**
	 * Clears this selection.
	 *
	 * @return {@code true} if this selection was not empty prior to
	 *         calling this method.
	 */
	public boolean clearSelection();

	/**
	 * Get the selected edges.
	 *
	 * @return a <b>new</b> {@link RefSet} containing the selected edges.
	 */
	public RefSet< E > getSelectedEdges();

	/**
	 * Get the selected vertices.
	 *
	 * @return a <b>new</b> {@link RefSet} containing the selected vertices.
	 */
	public RefSet< V > getSelectedVertices();

	public boolean addSelectionListener( final SelectionListener listener );

	public boolean removeSelectionListener( final SelectionListener listener );

	public void resumeListeners();

	public void pauseListeners();
}
