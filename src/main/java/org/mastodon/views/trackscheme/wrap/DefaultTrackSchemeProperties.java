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
package org.mastodon.views.trackscheme.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HasLabel;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.spatial.HasTimepoint;

/**
 * Defautl implementation of {@link TrackSchemeProperties} for graphs whose
 * vertices implement {@link HasTimepoint} and {@link HasLabel}.
 *
 * @param <V>
 *            the type of vertices in the model graph (not the TrackScheme
 *            graph).
 * @param <E>
 *            the type of edges in the graph.
 */
public class DefaultTrackSchemeProperties< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > >
		implements TrackSchemeProperties< V, E >
{
	@Override
	public int getTimepoint( final V v )
	{
		return v.getTimepoint();
	}

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
	public E insertEdge( final V source, final int sourceOutIndex, final V target, final int targetInIndex,
			final E ref )
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

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< V > listener )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< V > listener )
	{
		throw new UnsupportedOperationException();
	}

}
