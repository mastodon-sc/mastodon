package org.mastodon.revised.model.tag;

import java.util.Collection;

import org.mastodon.adapter.CollectionAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

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
