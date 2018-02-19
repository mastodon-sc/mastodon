package org.mastodon.revised.model.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mastodon.labels.LabelSets;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

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
