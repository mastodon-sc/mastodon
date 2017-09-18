package org.mastodon.revised.ui.grouping;

import static org.mastodon.revised.ui.grouping.GroupManager.NO_GROUP;

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

	public void setGroupId( final int id )
	{
		if ( manager.setGroupId( this, id ) )
			for ( final GroupChangeListener l : listeners.list )
				l.groupChanged();
	}

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

	public < T > T getModel( final GroupableModelFactory< T > factory )
	{
		return getModelData( factory ).forwarding.asT();
	}
}
