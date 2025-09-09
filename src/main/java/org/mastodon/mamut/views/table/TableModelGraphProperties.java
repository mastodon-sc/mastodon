package org.mastodon.mamut.views.table;

import org.mastodon.properties.PropertyChangeListener;

/**
 * Interface for accessing model graph properties in table views.
 * <p>
 * To make table views adaptable to various model graph type without requiring
 * the graph to implement specific interfaces, we access properties of model
 * vertices and edges this interface.
 *
 * @param <V>
 *            the type of vertices in the model graph.
 */
public interface TableModelGraphProperties< V >
{

	/**
	 * Adds the specified listener to be notified when a vertex label changes.
	 *
	 * @param listener
	 *            the vertex label listener.
	 */
	void addVertexLabelListener( final PropertyChangeListener< V > listener );

	/**
	 * Removes the specified listener so that it no longer receives vertex label
	 * change notifications.
	 *
	 * @param listener
	 *            the vertex label listener to remove.
	 */
	void removeVertexLabelListener( PropertyChangeListener< V > listener );

	/**
	 * Returns the label of the specified vertex.
	 *
	 * @param vertex
	 *            the vertex.
	 * @return the label of the specified vertex.
	 */
	String getLabel( V vertex );

	/**
	 * Sets the label of the specified vertex.
	 *
	 * @param vertex
	 *            the vertex.
	 * @param label
	 *            the new label for the specified vertex.
	 */
	void setLabel( V vertex, String label );

}
