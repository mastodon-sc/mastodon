package org.mastodon.model;

import org.mastodon.revised.ui.grouping.ForwardingModel;
import org.mastodon.revised.ui.grouping.GroupManager;
import org.mastodon.revised.ui.grouping.GroupableModelFactory;
import org.mastodon.util.Listeners;

/**
 * A {@link TimepointModel} forwarding to a switchable backing
 * {@link TimepointModel}.
 * <p>
 * Used for grouping views, see {@link GroupManager}.
 *
 * @author Tobias Pietzsch
 */
public class ForwardingTimepointModel implements TimepointModel, ForwardingModel< TimepointModel >
{
	private TimepointModel model;

	private final Listeners.List< TimepointListener > listeners;

	private final TimepointListener listener;

	public ForwardingTimepointModel()
	{
		listeners = new Listeners.SynchronizedList<>();
		listener = () -> listeners.list.forEach( TimepointListener::timepointChanged );
	}

	@Override
	public void setTimepoint( final int t )
	{
		model.setTimepoint( t );
	}

	@Override
	public int getTimepoint()
	{
		return model.getTimepoint();
	}

	@Override
	public Listeners< TimepointListener > listeners()
	{
		return listeners;
	}

	@Override
	public void linkTo( final TimepointModel newModel, final boolean copyCurrentStateToNewModel )
	{
		final TimepointModel oldModel = model;
		model = newModel;

		if ( oldModel != null )
			oldModel.listeners().remove( listener );
		newModel.listeners().add( listener );

		if ( copyCurrentStateToNewModel )
		{
			newModel.setTimepoint( oldModel.getTimepoint() );
		}
		else
		{
			if ( oldModel == null || oldModel.getTimepoint() != newModel.getTimepoint() )
				listener.timepointChanged();
		}
	}

	public static final GroupableModelFactory< TimepointModel > factory = new GroupableModelFactory< TimepointModel >()
	{
		@Override
		public TimepointModel createBackingModel()
		{
			return new DefaultTimepointModel();
		}

		@Override
		public ForwardingModel< TimepointModel > createForwardingModel()
		{
			return new ForwardingTimepointModel();
		}
	};
}
