package net.trackmate.revised.ui.grouping;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: javadoc
 *
 * @param <T>
 *            the specific {@link GroupHandle} type handled by this manages.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class GroupManager
{
	private final TIntObjectMap< Set< GroupHandle > > groupIdToGroupHandles;

	private int modCount;

	public GroupManager()
	{
		groupIdToGroupHandles = new TIntObjectHashMap<>( DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, -1 );
		modCount = 0;
	}

	public GroupHandle createGroupHandle()
	{
		return new GroupHandle( this );
	}

	public void removeGroupHandle( final GroupHandle handle )
	{
		final TIntIterator ids = new TIntArrayList( handle.groupIds ).iterator();
		while ( ids.hasNext() )
			removeFromGroup( handle, ids.next() );
	}

	void addToGroup( final GroupHandle handle, final int groupId )
	{
		Set< GroupHandle > handles = groupIdToGroupHandles.get( groupId );
		if ( handles == null )
		{
			handles = new HashSet<>();
			groupIdToGroupHandles.put( groupId, handles );
		}
		if ( handles.add( handle ) )
			++modCount;
	}

	void removeFromGroup( final GroupHandle handle, final int groupId )
	{
		final Set< GroupHandle > nandles = groupIdToGroupHandles.get( groupId );
		if ( nandles != null && nandles.remove( handle ) )
			++modCount;
	}

	int modCount()
	{
		return modCount;
	}

	void incModCount()
	{
		++modCount;
	}

	Set< GroupHandle > getAllGroupMembers( final GroupHandle member )
	{
		final HashSet< GroupHandle > members = new HashSet<>();
		members.add( member );
		final TIntIterator ids = member.groupIds.iterator();
		while ( ids.hasNext() )
			members.addAll( groupIdToGroupHandles.get( ids.next() ) );
		return members;
	}
}
