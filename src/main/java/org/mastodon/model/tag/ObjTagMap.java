package org.mastodon.model.tag;

import java.util.Collection;

import org.mastodon.labels.LabelSets;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.properties.ObjPropertyMap;

/**
 * A property map specialized to assign (mutually exclusive) tags from a tag set
 * to objects.
 * <p>
 * This map stores tags on the objects it is instantiated with. One object can
 * have at most one tag, or 0. It is nothing more than a glorified
 * {@link ObjPropertyMap}, with facilities to retrieve the objects that have
 * been tagged with a certain tag.
 * </p>
 * <p>
 * Tags must map to {@link Integer} IDs that are globally unique across all tag
 * sets. The default implementation ({@code DefaultObjTagMap}) is backed by a
 * {@link LabelSets} property of the objects (which can be shared among all
 * {@code DefaultObjTagMap}s).
 * </p>
 *
 * @param <O>
 *            the type of object to tag.
 * @param <T>
 *            the type of tags.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public interface ObjTagMap< O, T >
{
	/**
	 * Tags the object with the specified {@code tag}. The specified {@code tag}
	 * may be {@code null}, in which case the object is un-tagged (see
	 * {@link #remove(Object)}).
	 *
	 * @param object
	 *            the object to tag.
	 * @param tag
	 *            the tag to apply.
	 */
	void set( final O object, final T tag );

	/**
	 * Un-tag the specified object.
	 *
	 * @param object
	 *            the object whose tag to remove.
	 */
	void remove( final O object );

	/**
	 * Returns the tag of the specified object.
	 *
	 * @param object
	 *            the object.
	 * @return the tag, may be {@code null}.
	 */
	T get( final O object );

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
