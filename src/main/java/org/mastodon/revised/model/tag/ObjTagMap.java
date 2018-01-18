package org.mastodon.revised.model.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mastodon.labels.LabelSet;
import org.mastodon.labels.LabelSets;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;

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
 * sets. The {@code ObjTagMap} is backed a {@link LabelSets} property of the
 * objects (which can be shared among all {@code ObjTagMap}s).
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
public class ObjTagMap< O, T >
{
	private final LabelSets< O, Integer > idLabelSets;

	private final Function< T, Integer > tagToIdFunction;

	private final ArrayList< Integer > ids = new ArrayList<>();

	private final HashMap< T, Integer > tagToId = new HashMap<>();

	private final HashMap< Integer, T > idToTag = new HashMap<>();

	private final HashMap< Integer, List< Integer > > idToOtherIds = new HashMap<>();

	/**
	 * Create a tag map with the given set of mutually exclusive {@code tags},
	 * mapped to IDs by the specified {@code tagToIdFunction} and backed by the
	 * specified {@code idLabelSets} property.
	 *
	 * @param idLabelSets
	 * @param tags
	 * @param tagToIdFunction
	 */
	public ObjTagMap(
			final LabelSets< O, Integer > idLabelSets,
			final Collection< T > tags,
			final Function< T, Integer > tagToIdFunction )
	{
		this.idLabelSets = idLabelSets;
		this.tagToIdFunction = tagToIdFunction;
		update( tags );
	}

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
	public void set( final O object, final T tag )
	{
		if ( tag == null )
		{
			remove( object );
		}
		else
		{
			final LabelSet< O, Integer > ref = idLabelSets.createRef();
			final Integer id = tagToIdFunction.apply( tag );
			final LabelSet< O, Integer > labels = idLabelSets.getLabels( object, ref );
			labels.removeAll( idToOtherIds.get( id ) );
			labels.add( id );
			idLabelSets.releaseRef( ref );
		}
	}

	/**
	 * Un-tag the specified object.
	 *
	 * @param object
	 *            the object whose tag to remove.
	 */
	public void remove( final O object )
	{
		final LabelSet< O, Integer > ref = idLabelSets.createRef();
		idLabelSets.getLabels( object, ref ).removeAll( ids );
		idLabelSets.releaseRef( ref );
	}

	/**
	 * Returns the tag of the specified object.
	 *
	 * @param object
	 *            the object.
	 * @return the tag, may be {@code null}.
	 */
	public T get( final O object )
	{
		final LabelSet< O, Integer > ref = idLabelSets.createRef();
		try
		{
			final LabelSet< O, Integer > labels = idLabelSets.getLabels( object, ref );
			for ( final Integer id : ids )
				if ( labels.contains( id ) )
					return idToTag.get( id );
			return null;
		}
		finally
		{
			idLabelSets.releaseRef( ref );
		}
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
		final Integer id = tagToId.get( tag );
		if ( id == null )
			throw new IllegalArgumentException( "tag is not in tag set" );
		return Collections.unmodifiableCollection( idLabelSets.getLabeledWith( id ) );
	}

	/**
	 * Rebuild internal data structures to handle the given tag set.
	 */
	private void update( final Collection< T > tags )
	{
		ids.clear();
		tagToId.clear();
		idToTag.clear();
		idToOtherIds.clear();
		for ( final T tag : tags )
		{
			final Integer id = tagToIdFunction.apply( tag );
			if ( ids.contains( id ) )
				throw new IllegalArgumentException( "inconsistent tag set: ids are not unique" );
			ids.add( id );
			tagToId.put( tag, id );
			idToTag.put( id, tag );
		}
		idToTag.keySet().forEach( id ->
				idToOtherIds.put( id,
						idToTag.keySet().stream().filter( oid -> oid != id ).collect( Collectors.toList() ) ) );
	}
}
