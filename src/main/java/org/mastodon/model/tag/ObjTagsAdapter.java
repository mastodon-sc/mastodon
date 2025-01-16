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

import java.util.Collection;

import org.mastodon.adapter.CollectionAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Adapts a {@code ObjTags<O>} as a {@code ObjTags<WO>}. The mapping between
 * source objects ({@code O}) and wrapped objects ({@code WI}) is established by a
 * {@link RefBimap}.
 *
 * @param <O> object type of source {@code ObjTags}.
 *
 * @param <WO>
 *            object type of this wrapped {@code ObjTags}.
 *
 * @author Tobias Pietzsch
 */
public class ObjTagsAdapter< O, WO > implements ObjTags< WO >
{
	private final ObjTags< O > objTags;

	private final RefBimap< O, WO > refmap;

	public ObjTagsAdapter( final ObjTags< O > objTags, final RefBimap< O, WO > refmap )
	{
		this.objTags = objTags;
		this.refmap = refmap;
	}

	@Override
	public ObjTagMap< WO, Tag > tags( final TagSet tagSet )
	{
		return new ObjTagMapAdapter<>( objTags.tags( tagSet ), refmap );
	}

	@Override
	public void set( final WO object, final Tag tag )
	{
		objTags.set( refmap.getLeft( object ), tag );
	}

	@Override
	public Collection< WO > getTaggedWith( final Tag tag )
	{
		return new CollectionAdapter<>( objTags.getTaggedWith( tag ), refmap );
	}
}
