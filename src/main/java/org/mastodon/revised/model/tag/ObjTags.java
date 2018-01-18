package org.mastodon.revised.model.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mastodon.labels.LabelSets;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

/**
 * Manage tags for a collection of objects, according to a specified
 * {@link TagSetStructure}.
 *
 * @param <O>
 *            the type of object to tag.
 */
public class ObjTags< O >
{
	private final LabelSets< O, Integer > idLabelSets;

	private final Map< Tag, TagSet > tagToTagSet = new HashMap<>();

	private final Map< TagSet, ObjTagMap< O, Tag > > tagSetToTagMap = new HashMap<>();

	public ObjTags(
			final LabelSets< O, Integer > idLabelSets,
			final TagSetStructure tagSetStructure )
	{
		this.idLabelSets = idLabelSets;
		update( tagSetStructure );
	}

	public ObjTagMap< O, Tag > tags( final TagSet tagSet )
	{
		return tagSetToTagMap.get( tagSet );
	}

	/**
	 * Tags {@code object} with the specified {@code tag}. (Other tags from the
	 * same tag set are removed from the object).
	 *
	 * @param object
	 *            the object to tag.
	 * @param tag
	 *            the tag to apply.
	 */
	public void set( final O object, final Tag tag )
	{
		if ( tag == null )
			// TODO: remove all tags (from all tag sets) instead?
			throw new NullPointerException();
		else
			tags( tagToTagSet.get( tag ) ).set( object, tag );
	}

	/**
	 * Returns an unmodifiable collection containing all the objects that are
	 * tagged with the specified tag.
	 *
	 * @param tag
	 *            the tag to query.
	 * @return the collection of objects.
	 */
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
			tagSetToTagMap.put( tagSet, new ObjTagMap<>( idLabelSets, tagSet.getTags(), Tag::id ) );
			for ( final Tag tag : tagSet.getTags() )
				tagToTagSet.put( tag, tagSet );
		}
	}
}
