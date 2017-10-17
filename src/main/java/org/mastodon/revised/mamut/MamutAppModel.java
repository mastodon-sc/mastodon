package org.mastodon.revised.mamut;

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
public class MamutAppModel
{
	private static final int NUM_GROUPS = 3;

	public static final GroupableModelFactory< NavigationHandler< Spot, Link > > NAVIGATION = new ForwardingNavigationHandler.Factory<>();

	public static final GroupableModelFactory< TimepointModel > TIMEPOINT = ForwardingTimepointModel.factory;

	private final Model model;

	private final SelectionModel< Spot, Link > selectionModel;

	private final HighlightModel< Spot, Link > highlightModel;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final FocusModel< Spot, Link > focusModel;

	private final SharedBigDataViewerData sharedBdvData;

	private final int minTimepoint;

	private final int maxTimepoint;

	private final GroupManager groupManager;

	public MamutAppModel(
			final Model model,
			final SharedBigDataViewerData sharedBdvData )
	{
		this.model = model;

		final ListenableReadOnlyGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();

		final DefaultSelectionModel< Spot, Link > selectionModel = new DefaultSelectionModel<>( graph, idmap );
		graph.addGraphListener( selectionModel );
		this.selectionModel = selectionModel;

		final DefaultHighlightModel< Spot, Link > highlightModel = new DefaultHighlightModel<>( idmap );
		graph.addGraphListener( highlightModel );
		this.highlightModel = highlightModel;

		final DefaultFocusModel< Spot, Link > focusModel = new DefaultFocusModel<>( idmap );
		graph.addGraphListener( focusModel );
		this.focusModel = focusModel;

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

		groupManager = new GroupManager( NUM_GROUPS );
		groupManager.registerModel( TIMEPOINT );
		groupManager.registerModel( NAVIGATION );
	}

	public Model getModel()
	{
		return model;
	}

	public SelectionModel< Spot, Link > getSelectionModel()
	{
		return selectionModel;
	}

	public HighlightModel< Spot, Link > getHighlightModel()
	{
		return highlightModel;
	}

	public BoundingSphereRadiusStatistics getRadiusStats()
	{
		return radiusStats;
	}

	public FocusModel< Spot, Link > getFocusModel()
	{
		return focusModel;
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

	public GroupManager getGroupManager()
	{
		return groupManager;
	}
}
