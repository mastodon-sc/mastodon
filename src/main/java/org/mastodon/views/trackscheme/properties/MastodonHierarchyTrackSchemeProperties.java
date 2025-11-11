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
package org.mastodon.views.trackscheme.properties;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.model.HasLabel;
import org.mastodon.properties.PropertyChangeListener;

/**
 * A {@link TrackSchemeProperties} for hierarchy graphs, where the timepoint of
 * a vertex is defined as its depth in the hierarchy.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the vertex type, extending {@link Vertex} and {@link HasLabel}.
 * @param <E>
 *            the edge type.
 */
public class MastodonHierarchyTrackSchemeProperties< V extends Vertex< E > & HasLabel, E extends Edge< V > > implements TrackSchemeProperties< V, E >
{

	private final InverseDepthFirstIterator< V, E > it;

	public MastodonHierarchyTrackSchemeProperties( final ReadOnlyGraph< V, E > graph )
	{
		this.it = new InverseDepthFirstIterator<>( graph );
	}

	/*
	 * NOT THREAD SAFE! If issues arise when multithreading TS graph creation,
	 * they will be caused here. But for now, there is no concurrent creation of
	 * vertices or editing of vertex properties.
	 */
	@Override
	public int getTimepoint( final V v )
	{
		it.reset( v );
		int level = 0;
		while ( it.hasNext() && it.next().incomingEdges().size() > 0 )
			level++;
		return level;
	}

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< V > listener )
	{}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< V > listener )
	{}

	@Override
	public String getLabel( final V v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final V v, final String label )
	{
		v.setLabel( label );
	}

	@Override
	public E addEdge( final V source, final V target, final E ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E insertEdge( final V source, final int sourceOutIndex, final V target, final int targetInIndex, final E ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E initEdge( final E e )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEdge( final E e )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVertex( final V v )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyGraphChanged()
	{
		throw new UnsupportedOperationException();
	}
}
