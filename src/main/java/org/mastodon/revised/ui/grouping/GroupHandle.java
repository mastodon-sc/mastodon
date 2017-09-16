package org.mastodon.revised.ui.grouping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.mastodon.util.Listeners;

/**
 * TODO: javadoc
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class GroupHandle
{
	public static final int NO_GROUP = -1;

	private final GroupManager manager;

	private int expectedModCount;

	/**
	 * The id of the group this {@link GroupHandle} belongs to currently.
	 * <p>
	 * {@code groupId == NO_GROUP} implies, that this GroupHandle currently
	 * belongs to no group, respectively the singleton group containing only the
	 * GroupHandle itself.
	 */
	int groupId = NO_GROUP;

	private final Listeners.List< GroupChangeListener > listeners = new Listeners.SynchronizedList<>();

	private final HashMap< Class< ? >, Set< ? > > registered = new HashMap<>();

	private final HashMap< Class< ? >, ArrayList< ? > > cache = new HashMap<>();

	GroupHandle( final GroupManager manager )
	{
		this.manager = manager;
		expectedModCount = manager.modCount() - 1;
	}

	public int getGroupId()
	{
		return groupId;
	}

	void setGroupId( final int id )
	{
		if ( manager.setGroupId( this, id ) )
			for ( final GroupChangeListener l : listeners.list )
				l.groupChanged();
	}

	public Listeners< GroupChangeListener > groupChangeListeners()
	{
		return listeners;
	}

	public < T > void add( final T o )
	{
		final Class< ? > clazz = o.getClass();
		@SuppressWarnings( "unchecked" )
		Set< T > ts = ( Set< T > ) registered.get( clazz );
		if ( ts == null )
		{
			ts = new HashSet<>();
			registered.put( clazz, ts );
		}
		if ( ts.add( o ) )
			manager.incModCount();
	}

	public < T > void remove( final T o )
	{
		final Class< ? > clazz = o.getClass();
		@SuppressWarnings( "unchecked" )
		final Set< T > ts = ( Set< T > ) registered.get( clazz );
		if ( ts != null && ts.remove( o ) )
			manager.incModCount();
	}

	@SuppressWarnings( "unchecked" )
	public < T > ArrayList< T > allInSharedGroups( final T o )
	{
		validateCache();
		return ( ArrayList< T > ) cache.get( o.getClass() );
	}

	private synchronized void validateCache()
	{
		final int modCount = manager.modCount();
		if ( expectedModCount != modCount )
		{
			expectedModCount = modCount;
			cache.clear();
			for ( final Class< ? > clazz : registered.keySet() )
				rebuildCacheEntry( clazz );
		}
	}

	@SuppressWarnings( "unchecked" )
	private < T > void rebuildCacheEntry( final Class< T > clazz )
	{
		final Set< GroupHandle > allMembers = manager.getAllGroupMembers( this );
		final HashSet< T > all = new HashSet<>();
		for ( final GroupHandle m : allMembers )
			all.addAll( ( Set< T > ) m.registered.get( clazz ) );
		cache.put( clazz, new ArrayList<>( all ) );
	}
}
