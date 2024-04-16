/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.grouping;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Manages a fixed number of groups. Groups are used to share state (Models)
 * between views. Each view can belong to at most one group at any time. A view
 * that belongs to no group is effectively in its own private group. Grouping
 * mechanic is hidden from the views using {@link ForwardingModel}s. Each view
 * has an associated {@link GroupHandle} from which it can obtain fixed
 * forwarding models whose backing models are transparently switched as group
 * membership changes.
 * <p>
 * Use {@link #createGroupHandle()} to make new {@link GroupHandle}s for new
 * views.
 * </p>
 * <p>
 * Use {@link #registerModel(GroupableModelFactory)} to register new model types
 * for which forwarding and backing models should be managed.
 * </p>
 *
 * @author Tobias Pietzsch
 */
public class GroupManager
{
	public static final int NO_GROUP = -1;

	private final TIntObjectMap< Set< GroupHandle > > groupIdToGroupHandles;

	private final int numGroups;

	/**
	 *
	 * @param numGroups
	 *            how many groups to create
	 */
	public GroupManager( final int numGroups )
	{
		this.numGroups = numGroups;
		groupIdToGroupHandles = new TIntObjectHashMap<>( DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, -1 );
		for ( int i = 0; i < numGroups; ++i )
			groupIdToGroupHandles.put( i, new HashSet<>() );
	}

	/**
	 * Create a {@code GroupHandle} for a new view.
	 *
	 * @return a new {@link GroupHandle}
	 */
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
