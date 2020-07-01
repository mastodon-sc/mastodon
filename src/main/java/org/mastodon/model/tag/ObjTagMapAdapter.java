package org.mastodon.model.tag;

import java.util.Collection;

import org.mastodon.adapter.CollectionAdapter;
import org.mastodon.adapter.RefBimap;

/**
 * Adapts a {@code ObjTagMap<O, T>} as a {@code ObjTagMap<WO, T>}. The mapping
 * between source objects ({@code O}) and wrapped objects ({@code WI}) is
 * established by a {@link RefBimap}.
 *
 * @param <O> object type of source {@code ObjTagMap}.
 *
 * @param <WO>
 *            object type of this wrapped {@code ObjTagMap}.
 * @param <T>
 *            the type of tags.
 *
 * @author Tobias Pietzsch
 */
public class ObjTagMapAdapter< O, WO, T > implements ObjTagMap< WO, T >
{
	private final ObjTagMap< O, T > objTagMap;

	private final RefBimap< O, WO > refmap;

	public ObjTagMapAdapter( final ObjTagMap< O, T > objTagMap, final RefBimap< O, WO > refmap )
	{
		this.objTagMap = objTagMap;
		this.refmap = refmap;
	}

	@Override
	public void set( final WO object, final T tag )
	{
		objTagMap.set( refmap.getLeft( object ), tag );
	}

	@Override
	public void remove( final WO object )
	{
		objTagMap.remove( refmap.getLeft( object ) );
	}

	@Override
	public T get( final WO object )
	{
		return objTagMap.get( refmap.getLeft( object ) );
	}

	@Override
	public Collection< WO > getTaggedWith( final TagSetStructure.Tag tag )
	{
		return new CollectionAdapter<>( objTagMap.getTaggedWith( tag ), refmap );
	}
}
