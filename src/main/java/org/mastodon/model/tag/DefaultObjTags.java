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
import java.util.HashMap;
import java.util.Map;

import org.mastodon.labels.LabelSets;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Default implementation of {@link ObjTags}.
 * <p>
 * Manages tags for a collection of objects, according to a specified
 * {@link TagSetStructure}.
 *
 * @param <O>
 *            the type of object to tag.
 */
public class DefaultObjTags< O > implements ObjTags< O >
{
	private final LabelSets< O, Integer > idLabelSets;

	private final Map< Tag, TagSet > tagToTagSet = new HashMap<>();

	private final Map< TagSet, DefaultObjTagMap< O, Tag > > tagSetToTagMap = new HashMap<>();

	public DefaultObjTags(
			final LabelSets< O, Integer > idLabelSets,
			final TagSetStructure tagSetStructure )
	{
		this.idLabelSets = idLabelSets;
		update( tagSetStructure );
	}

	@Override
	public DefaultObjTagMap< O, Tag > tags( final TagSet tagSet )
	{
		return tagSetToTagMap.get( tagSet );
	}

	@Override
	public void set( final O object, final Tag tag )
	{
		if ( tag == null )
			// TODO: remove all tags (from all tag sets) instead?
			throw new NullPointerException();
		else
			tags( tagToTagSet.get( tag ) ).set( object, tag );
	}

	@Override
	public Collection< O > getTaggedWith( final Tag tag )
	{
		if ( tag == null )
			// TODO: return all objects without any tag instead?
			throw new NullPointerException();
		else
			return tags( tagToTagSet.get( tag ) ).getTaggedWith( tag );
	}

	/**
	 * Rebuild internal data structures to handle the given
	 * {@link TagSetStructure}.
	 */
	void update( final TagSetStructure tagSetStructure )
	{
		tagToTagSet.clear();
		tagSetToTagMap.clear();
		for ( final TagSet tagSet : tagSetStructure.getTagSets() )
		{
			tagSetToTagMap.put( tagSet, new DefaultObjTagMap<>( idLabelSets, tagSet.getTags(), Tag::id ) );
			for ( final Tag tag : tagSet.getTags() )
				tagToTagSet.put( tag, tagSet );
		}
	}
}
