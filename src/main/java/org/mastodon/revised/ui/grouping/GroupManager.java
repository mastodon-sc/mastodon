package org.mastodon.revised.ui.grouping;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * TODO: javadoc
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class GroupManager
{
	public static final int NO_GROUP = -1;

	private final TIntObjectMap< Set< GroupHandle > > groupIdToGroupHandles;

	private final int numGroups;

	public GroupManager( final int numGroups )
	{
		this.numGroups = numGroups;
		groupIdToGroupHandles = new TIntObjectHashMap<>( DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, -1 );
		for ( int i = 0; i < numGroups; ++i )
			groupIdToGroupHandles.put( i, new HashSet<>() );
	}

	public GroupHandle createGroupHandle()
	{
		return new GroupHandle( this );
	}

	public void removeGroupHandle( final GroupHandle handle )
	{
		setGroupId( handle, NO_GROUP );
	}

	public int getNumGroups()
	{
		return numGroups;
	}

	boolean setGroupId( final GroupHandle handle, final int groupId )
	{
		final int oldId = handle.groupId;
		if ( oldId == groupId )
			return false;

		if ( oldId != NO_GROUP )
			groupIdToGroupHandles.get( oldId ).remove( handle );

		final boolean copyCurrentStateToNewModel;
		if ( groupId != NO_GROUP )
		{
			final Set< GroupHandle > handles = groupIdToGroupHandles.get( groupId );
			copyCurrentStateToNewModel = handles.isEmpty();
			handles.add( handle );
		}
		else
			copyCurrentStateToNewModel = true;

		handle.groupId = groupId;
		models.values().forEach( ( m ) -> m.moveTo( handle, groupId, copyCurrentStateToNewModel ) );
		return true;
	}

	class ModelType< T >
	{
		final ArrayList< T > models;

		final GroupableModelFactory< T > factory;

		ModelType( final GroupableModelFactory< T > factory )
		{
			this.factory = factory;
			models = new ArrayList<>();
			for ( int i = 0; i < getNumGroups(); ++i )
				models.add( factory.createBackingModel() );
		}

		public void moveTo( final GroupHandle handle, final int groupId, final boolean copyCurrentStateToNewModel )
		{
			final GroupHandle.ModelData< T > data = handle.getModelData( factory );
			final T model = ( groupId == NO_GROUP )
					? data.backing
					: models.get( groupId );
			data.forwarding.linkTo( model, copyCurrentStateToNewModel );
		}
	}

	final Map< GroupableModelFactory< ? >, ModelType< ? > > models = new HashMap<>();

	public void registerModel( final GroupableModelFactory< ? > factory )
	{
		models.put( factory, new ModelType<>( factory ) );
	}
}
