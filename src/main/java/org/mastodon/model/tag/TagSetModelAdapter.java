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
package org.mastodon.model.tag;

import org.mastodon.adapter.ForwardedListeners;
import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.scijava.listeners.Listeners;

/**
 * Adapts a {@code TagSetModel<V, E>} as a {@code TagSetModel<WV, WE>}. The
 * mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type of this wrapped {@link TagSetModel}.
 * @param <WE>
 *            edge type of this wrapped {@link TagSetModel}.
 *
 * @author Tobias Pietzsch
 */
public class TagSetModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >,
		WE extends Edge< WV > > implements TagSetModel< WV, WE >
{
	private final TagSetModel< V, E > tagSetModel;

	private final ObjTags< WV > vertexTags;

	private final ObjTags< WE > edgeTags;

	private final ForwardedListeners< TagSetModelListener > listeners;

	public TagSetModelAdapter(
			final TagSetModel< V, E > tagSetModel,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.tagSetModel = tagSetModel;
		vertexTags = new ObjTagsAdapter<>( tagSetModel.getVertexTags(), vertexMap );
		edgeTags = new ObjTagsAdapter<>( tagSetModel.getEdgeTags(), edgeMap );
		this.listeners = new ForwardedListeners.List<>( tagSetModel.listeners() );
	}

	@Override
	public ObjTags< WV > getVertexTags()
	{
		return vertexTags;
	}

	@Override
	public ObjTags< WE > getEdgeTags()
	{
		return edgeTags;
	}

	@Override
	public TagSetStructure getTagSetStructure()
	{
		return tagSetModel.getTagSetStructure();
	}

	@Override
	public void setTagSetStructure( final TagSetStructure tss )
	{
		tagSetModel.setTagSetStructure( tss );
	}

	@Override
	public Listeners< TagSetModelListener > listeners()
	{
		return listeners;
	}

	@Override
	public void pauseListeners()
	{
		tagSetModel.pauseListeners();
	}

	@Override
	public void resumeListeners()
	{
		tagSetModel.resumeListeners();
	}

	@Override
	public void clear()
	{
		tagSetModel.clear();
	}
}
