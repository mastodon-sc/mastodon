package org.mastodon.revised.model.tag;

import java.util.Collection;

import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

/**
 * Manage tags for a collection of objects, according to a specified
 * {@link TagSetStructure}.
 *
 * @param <O>
 *            the type of object to tag.
 */
public interface ObjTags< O >
{
	ObjTagMap< O, Tag > tags( final TagSet tagSet );

	/**
	 * Tags {@code object} with the specified {@code tag}. (Other tags from the
	 * same tag set are removed from the object).
	 *
	 * @param object
	 *            the object to tag.
	 * @param tag
	 *            the tag to apply.
	 */
	void set( final O object, final Tag tag );

	/**
	 * Returns an unmodifiable collection containing all the objects that are
	 * tagged with the specified tag.
	 *
	 * @param tag
	 *            the tag to query.
	 * @return the collection of objects.
	 */
	Collection< O > getTaggedWith( final Tag tag );
}
