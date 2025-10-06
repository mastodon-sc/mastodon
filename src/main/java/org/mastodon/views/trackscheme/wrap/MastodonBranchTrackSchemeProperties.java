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
import org.mastodon.model.branch.BranchVertex;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.spatial.HasTimepoint;

/**
 * A {@link DefaultTrackSchemeProperties} for branch graphs.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the vertex type, extending {@link BranchVertex}.
 * @param <E>
 *            the edge type.
 */
public class MastodonBranchTrackSchemeProperties< V extends Vertex< E > & HasTimepoint & HasLabel & BranchVertex, E extends Edge< V > > extends DefaultTrackSchemeProperties< V, E >
{

	@Override
	public int getFirstTimePoint( final V v )
	{
		return v.getFirstTimePoint();
	}

	@Override
	public String getFirstLabel( final V v )
	{
		return v.getFirstLabel();
	}

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< V > listener )
	{}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< V > listener )
	{}
}
