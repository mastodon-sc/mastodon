package net.trackmate.revised.ui.grouping;

import gnu.trove.TCollections;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: javadoc
 *
 * @param <T>
 *            recursive type of this class.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class GroupHandle
{
	private final GroupManager manager;

	private int expectedModCount;

	/**
	 * The IDs of the groups this {@link GroupHandle} belongs to currently.
	 * <p>
	 * Empty {@code groupIds} implies, that this {@link GroupHandle} currently
	 * belongs to no group, respectively the singleton group containing only the
	 * {@link GroupHandle} itself.
	 */
	final TIntSet groupIds;

	private final TIntSet unmodifiableGroupIds;

	private final ArrayList< GroupChangeListener > listeners;

	private final HashMap< Class< ? >, Set< ? > > registered = new HashMap<>();

	private final HashMap< Class< ? >, ArrayList< ? > > cache = new HashMap<>();

	GroupHandle( final GroupManager manager )
	{
		this.manager = manager;
		expectedModCount = manager.modCount() - 1;
		groupIds = new TIntHashSet();
		listeners = new ArrayList<>();
		unmodifiableGroupIds = TCollections.unmodifiableSet( groupIds );
	}

	public TIntSet getGroupIds()
	{
		return unmodifiableGroupIds;
	}

	public boolean isInGroup( final int groupId )
	{
		return groupIds.contains( groupId );
	}

	public boolean addGroupChangeListener( final GroupChangeListener listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	public boolean removeGroupChangeListener( final GroupChangeListener listener )
	{
		return listeners.remove( listener );
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

	void addToGroup( final int groupId )
	{
		manager.addToGroup( this, groupId );
		groupIds.add( groupId );
		notifyListeners();
	}

	void removeFromGroup( final int groupId )
	{
		manager.removeFromGroup( this, groupId );
		groupIds.remove( groupId );
		notifyListeners();
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

	private < T > void rebuildCacheEntry( final Class< T > clazz )
	{
		final Set< GroupHandle > allMembers = manager.getAllGroupMembers( this );
		final HashSet< T > all = new HashSet<>();
		for ( final GroupHandle m : allMembers )
			all.addAll( ( Set< T > ) m.registered.get( clazz ) );
		cache.put( clazz, new ArrayList<>( all ) );
	}

	private void notifyListeners()
	{
		for ( final GroupChangeListener l : listeners )
			l.groupChanged();
	}
}
