package org.mastodon.revised.model.tag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.properties.PropertyMap;

/**
 * A {@link PropertyMap} specialized to manage a collection of tags.
 * <p>
 * This map stores tags on the objects it is instantiated with. One object can
 * have at most one tag, or 0. It is nothing more than a glorified
 * {@link ObjPropertyMap}, with facilities to retrieve the objects that have
 * been tagged with a certain tag.
 *
 * @param <O>
 *            the type of object to tag.
 * @param <K>
 *            the type of tags.
 *
 * @author Jean-Yves Tinevez
 */
public class TagPropertyMap< O, K > extends ObjPropertyMap< O, K >
{

	private final Map< K, RefSet< O > > tags;

	private final RefCollection< O > pool;

	public TagPropertyMap( final RefCollection< O > pool )
	{
		super( pool );
		this.pool = pool;
		this.tags = new HashMap<>();
	}

	/**
	 * Tags the object with the specified tag.
	 *
	 * @param o
	 *            the object to tag.
	 * @param tag
	 *            the tag to apply.
	 * @return the previous tag, if any (can be <code>null</code>).
	 */
	@Override
	public K set( final O o, final K tag )
	{
		final K previous = super.set( o, tag );
		if ( null != previous )
			removeObjectFromTagSet( o, previous );

		// Store in tag set.
		RefSet< O > coll = tags.get( tag );
		if ( null == coll )
		{
			coll = RefCollections.createRefSet( pool );
			tags.put( tag, coll );
		}
		coll.add( o );

		return previous;
	}

	/**
	 * Tags the collection of objects with the specified tag. Previous tags are
	 * removed.
	 *
	 * @param objects
	 *            the collection of objects to tag.
	 * @param tag
	 *            the tag to apply.
	 */
	public void set( final Iterable< O > objects, final K tag )
	{
		RefSet< O > set = tags.get( tag );
		if ( null == set )
		{
			set = RefCollections.createRefSet( pool );
			tags.put( tag, set );
		}

		final Set< K > tagsToCheck = new HashSet<>();
		for ( final O o : objects )
		{
			final K previousTag = super.set( o, tag );
			if ( null != previousTag )
			{
				tags.get( previousTag ).remove( o );
				tagsToCheck.add( previousTag );
			}

			set.add( o );
		}

		// Check if we emptied the tags.
		for ( final K t : tagsToCheck )
		{
			if ( tags.get( t ).isEmpty() )
				tags.remove( t );
		}
	}

	/**
	 * Removes the tag from the specified object.
	 *
	 * @param o
	 *            the object whose tag to remove.
	 * @return the previous tag this object had, if any (can be
	 *         <code>null</code>).
	 */
	@Override
	public K remove( final O o )
	{
		final K previous = super.remove( o );
		if ( null != previous )
			removeObjectFromTagSet( o, previous );
		return previous;
	}

	/**
	 * Removes the tags for the specified {@code objects} collection, whathever
	 * they are.
	 *
	 * @param objects
	 *            the collection of objects whose tags to remove.
	 */
	public void remove( final Iterable< O > objects )
	{
		final Set< K > tagsToCheck = new HashSet<>();
		for ( final O o : objects )
		{
			final K tag = super.remove( o );
			if ( null != tag )
			{
				tags.get( tag ).remove( o );
				tagsToCheck.add( tag );
			}
		}

		// Check if we emptied the tags.
		for ( final K tag : tagsToCheck )
		{
			if ( tags.get( tag ).isEmpty() )
				tags.remove( tag );
		}
	}

	/**
	 * Returns an unmodifiable collection containing all the objects that are
	 * tagged with the specified tag. Returns <code>null</code> if the tag is
	 * unknown.
	 *
	 * @param tag
	 *            the tag to query.
	 * @return the collection of object.
	 */
	public Collection< O > getTaggedWith( final K tag )
	{
		return null == tags.get( tag ) ? null : Collections.unmodifiableCollection( tags.get( tag ) );
	}

	/**
	 * Exposes an unmodifiable set containing all the tags known to this tag
	 * set.
	 *
	 * @return the tags.
	 */
	public Set< K > getTags()
	{
		return Collections.unmodifiableSet( tags.keySet() );
	}

	/**
	 * Clears the specified tag. All objects tagged with the specified tag will
	 * not be set after this method call, and the tag will be removed from the
	 * tag set.
	 *
	 * @param tag
	 *            the tag to clear.
	 */
	public void clearTag( final K tag )
	{
		final RefSet< O > set = tags.get( tag );
		if ( null == set )
			return;

		for ( final O o : set )
			super.remove( o );

		tags.remove( tag );
	}

	@Override
	public void clear()
	{
		super.beforeClearPool();
		tags.clear();
	}

	private void removeObjectFromTagSet( final O obj, final K tag )
	{
		final RefSet< O > set = tags.get( tag );
		set.remove( obj );
		if ( set.isEmpty() )
			tags.remove( tag );
	}
}
