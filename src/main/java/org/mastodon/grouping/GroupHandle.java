/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.grouping.GroupManager.NO_GROUP;

import java.util.HashMap;

import org.scijava.listeners.Listeners;

/**
 * Each Mastodon view has a {@code GroupHandle}. The view can
 * {@link #getModel(GroupableModelFactory) obtain} forwarding models that
 * transparently switch between backing models of the current group. The
 * {@code GroupHandle} also manages {@link #groupChangeListeners()} that are
 * notified when the its group membership changes.
 *
 * @author Tobias Pietzsch
 */
public class GroupHandle
{
	private final GroupManager manager;

	/**
	 * The id of the group this {@link GroupHandle} belongs to currently.
	 * <p>
	 * {@code groupId == NO_GROUP} implies, that this GroupHandle currently
	 * belongs to no group, respectively the singleton group containing only the
	 * GroupHandle itself.
	 */
	int groupId = NO_GROUP;

	private final Listeners.List< GroupChangeListener > listeners = new Listeners.SynchronizedList<>();

	GroupHandle( final GroupManager manager )
	{
		this.manager = manager;
	}

	public int getGroupId()
	{
		return groupId;
	}

	public Listeners< GroupChangeListener > groupChangeListeners()
	{
		return listeners;
	}

	public int getNumGroups()
	{
		return manager.getNumGroups();
	}

	/**
	 * Move this handle to the specified group.
	 *
	 * @param id
	 *            group index or {@link GroupManager#NO_GROUP}.
	 */
	public void setGroupId( final int id )
	{
		if ( manager.setGroupId( this, id ) )
			for ( final GroupChangeListener l : listeners.list )
				l.groupChanged();
	}

	/**
	 * Holds a forwarding model, and a backing model which will be forwarded to
	 * if this handle belongs to NO_GROUP.
	 */
	class ModelData< T >
	{
		final ForwardingModel< T > forwarding;

		final T backing;

		ModelData( final GroupableModelFactory< T > factory )
		{
			forwarding = factory.createForwardingModel();
			backing = factory.createBackingModel();
		}
	}

	private final HashMap< GroupableModelFactory< ? >, ModelData< ? > > models = new HashMap<>();

	< T > ModelData< T > getModelData( final GroupableModelFactory< T > factory )
	{
		@SuppressWarnings( "unchecked" )
		ModelData< T > data = ( ModelData< T > ) models.get( factory );
		if ( data == null )
		{
			data = new ModelData<>( factory );
			models.put( factory, data );
			@SuppressWarnings( "unchecked" )
			final GroupManager.ModelType< T > modelType = ( GroupManager.ModelType< T > ) manager.models.get( factory );
			modelType.moveTo( GroupHandle.this, groupId, false );
		}
		return data;
	}

	/**
	 * Get the forwarding model with the specified {@code key}.
	 *
	 * @param <T>
	 *            the type of the backing model.
	 * @param key
	 *            the factory by which this kind of model was
	 *            {@link GroupManager#registerModel(GroupableModelFactory)
	 *            registered} with the {@code GroupManager}.
	 * @return the forwarding model with the specified {@code key}.
	 */
	public < T > T getModel( final GroupableModelFactory< T > key )
	{
		return getModelData( key ).forwarding.asT();
	}
}
