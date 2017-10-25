package org.mastodon.revised.mamut;

import org.mastodon.app.MastodonAppModel;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.mamut.BoundingSphereRadiusStatistics;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

/**
 * Data class that stores the data model and the application model of the MaMuT
 * application.
 *
 * @author Jean-Yves Tinevez
 */
public class MamutAppModel extends MastodonAppModel< Model, Spot, Link >
{
	private static final int NUM_GROUPS = 3;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final SharedBigDataViewerData sharedBdvData;

	private final InputTriggerConfig keyconf; // TODO: should this really be here???

	private final KeyPressedManager keyPressedManager; // TODO: should this really be here???

	private final int minTimepoint;

	private final int maxTimepoint;

	/**
	 * Actions that should be available in all views.
	 */
	private final Actions appActions;

	public MamutAppModel(
			final Model model,
			final SharedBigDataViewerData sharedBdvData,
			final InputTriggerConfig keyconf,
			final KeyPressedManager keyPressedManager )
	{
		super( NUM_GROUPS, model );

		this.radiusStats = new BoundingSphereRadiusStatistics( model );
		this.sharedBdvData = sharedBdvData;
		this.keyconf = keyconf;
		this.keyPressedManager = keyPressedManager;
		this.minTimepoint = 0;
		this.maxTimepoint = sharedBdvData.getNumTimepoints() - 1;
		/*
		 * TODO: (?) For now, we use timepoint indices in MaMuT model, instead
		 * of IDs/names. This is because BDV also displays timepoint index, and
		 * it would be confusing to have different labels in TrackScheme. If
		 * this is changed in the future, then probably only in the model files.
		 */

		this.appActions = new Actions( keyconf, "mamut" );
	}

	public BoundingSphereRadiusStatistics getRadiusStats()
	{
		return radiusStats;
	}

	public SharedBigDataViewerData getSharedBdvData()
	{
		return sharedBdvData;
	}

	public int getMinTimepoint()
	{
		return minTimepoint;
	}

	public int getMaxTimepoint()
	{
		return maxTimepoint;
	}

	public InputTriggerConfig getKeyconf()
	{
		return keyconf;
	}

	public KeyPressedManager getKeyPressedManager()
	{
		return keyPressedManager;
	}

	public Actions getAppActions()
	{
		return appActions;
	}
}
