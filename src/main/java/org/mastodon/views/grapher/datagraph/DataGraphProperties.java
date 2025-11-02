/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.grapher.datagraph;

import org.mastodon.feature.Multiplicity;
import org.mastodon.properties.PropertyChangeListener;

/**
 * Interface for accessing model graph properties.
 * <p>
 * To make {@link DataGraph} adaptable to various model graph type without
 * requiring the graph to implement specific interfaces, we access properties of
 * model vertices and edges (for example the label of a vertex) through
 * implementations of this interface.
 * <p>
 * There are only minimal properties required for the grapher to work, which is
 * logical as we mainly pull the values we want to play via the numerical
 * features. The properties managed here are:
 * <ul>
 * <li>setting and getting the label of a vertex, which is used to display a
 * label in individual data points in the graph.
 * <li>listening to changes in vertex labels, so that the graph can update
 * itself when a label changes.
 * <li>getting the time-point of a vertex. A time-point is normally not required
 * to display data in a grapher (which does not care about data being sliced by
 * time), but this property is used to navigate within a track, e.g. to move
 * across siblings. If the model graph you want to plot does not have a
 * time-point simply return a constant value here. For non-time-lapse data, this
 * property could be used to reslice the data along another dimension than time.
 * <li>notifying that the graph has been changed by the data graph view, so that
 * listeners can react accordingly.
 * <li>getting the number of sources the features are defined on for this graph.
 * </ul>
 *
 * @param <V>
 *            the type of vertices in the model graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public interface DataGraphProperties< V, E >
{

	/**
	 * Gets the label of a vertex.
	 * 
	 * @param v
	 *            the vertex.
	 * @return the label.
	 */
	public String getLabel( V v );

	/**
	 * Sets the label of a vertex.
	 * 
	 * @param v
	 * @param label
	 */
	public void setLabel( V v, String label );

	/**
	 * Adds a listener to vertex label changes.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addVertexLabelListener( final PropertyChangeListener< V > listener );

	/**
	 * Removes a listener to vertex label changes.
	 * 
	 * @param vertexLabelListener
	 *            the listener to remove.
	 */
	public void removeVertexLabelListener( PropertyChangeListener< V > vertexLabelListener );

	/**
	 * Gets the time-point of a vertex.
	 * 
	 * @param v
	 *            the vertex.
	 * @return the time-point.
	 */
	public int getTimepoint( V v );

	/**
	 * Notifies that the graph has been changed.
	 */
	public void notifyGraphChanged();

	/**
	 * Returns the number of sources the features are defined on for this graph.
	 * This is used to multiply the features with a
	 * {@link Multiplicity#ON_SOURCES} in the feature selection UI.
	 * 
	 * @return
	 */
	public int getNSources();

}
