package org.mastodon.revised.mamut;

import org.mastodon.app.MastodonAppModel;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.mastodon.model.DefaultFocusModel;
import org.mastodon.model.DefaultHighlightModel;
import org.mastodon.model.DefaultSelectionModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.ForwardingNavigationHandler;
import org.mastodon.model.ForwardingTimepointModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.mamut.BoundingSphereRadiusStatistics;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

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

	private final int minTimepoint;

	private final int maxTimepoint;

	public MamutAppModel(
			final Model model,
			final SharedBigDataViewerData sharedBdvData )
	{
		super( NUM_GROUPS, model );

		this.radiusStats = new BoundingSphereRadiusStatistics( model );
		this.sharedBdvData = sharedBdvData;
		this.minTimepoint = 0;
		this.maxTimepoint = sharedBdvData.getNumTimepoints() - 1;
		/*
		 * TODO: (?) For now, we use timepoint indices in MaMuT model, instead
		 * of IDs/names. This is because BDV also displays timepoint index, and
		 * it would be confusing to have different labels in TrackScheme. If
		 * this is changed in the future, then probably only in the model files.
		 */
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
}
