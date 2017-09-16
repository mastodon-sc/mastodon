package org.mastodon.revised.ui.grouping;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;
import static org.mastodon.revised.ui.grouping.GroupHandle.NO_GROUP;

/**
 * TODO: javadoc
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
		final int id = handle.getGroupId();
		if ( id != NO_GROUP && handles( id ).remove( handle ) )
			++modCount;
	}

	boolean setGroupId( final GroupHandle handle, final int groupId )
	{
		final int oldId = handle.groupId;
		if ( oldId == groupId )
			return false;
		handles( oldId ).remove( handle );
		handles( groupId ).add( handle );
		handle.groupId = groupId;
		++modCount;
		return true;
	}

	int modCount()
	{
		return modCount;
	}

	void incModCount()
	{
		++modCount;
	}

	private Set< GroupHandle > handles( int groupId )
	{
		Set< GroupHandle > handles = groupIdToGroupHandles.get( groupId );
		if ( handles == null )
		{
			handles = new HashSet<>();
			groupIdToGroupHandles.put( groupId, handles );
		}
		return handles;
	}

	Set< GroupHandle > getAllGroupMembers( final GroupHandle handle )
	{
		final int id = handle.getGroupId();
		return ( id == NO_GROUP )
				? Collections.singleton( handle )
				: new HashSet<>( handles( id ) );
	}
}
