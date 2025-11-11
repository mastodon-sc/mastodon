package org.mastodon.mamut;

import static org.mastodon.model.app.UIModel.COMPUTE_FEATURE_DIALOG;
import static org.mastodon.model.app.UIModel.OPEN_ONLINE_DOCUMENTATION;
import static org.mastodon.model.app.UIModel.PREFERENCES_DIALOG;
import static org.mastodon.model.app.UIModel.PREFERENCES_DIALOG_KEYS;
import static org.mastodon.model.app.UIModel.TAGSETS_DIALOG;
import static org.mastodon.model.app.UIModel.TAGSETS_DIALOG_KEYS;

import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugins;
import org.mastodon.mamut.views.MamutViewFactory2;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.mamut.views.bdv.MamutViewBranchBdv;
import org.mastodon.mamut.views.grapher.MamutViewBranchGrapher;
import org.mastodon.mamut.views.grapher.MamutViewGrapher;
import org.mastodon.mamut.views.table.MamutViewSelectionTable2;
import org.mastodon.mamut.views.table.MamutViewTable2;
import org.mastodon.mamut.views.trackscheme.MamutViewBranchTrackScheme2;
import org.mastodon.mamut.views.trackscheme.MamutViewHierarchyTrackScheme2;
import org.mastodon.mamut.views.trackscheme.MamutViewTrackScheme2;
import org.mastodon.model.app.BdvAppModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.MastodonKeymapManager;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.KeymapManager;

/**
 * Core app model for the Mastodon app 'Mamut', that displays cells as
 * ellipsoids. It is typed against {@link Spot} and {@link Link}.
 */
public class MamutAppModel extends BdvAppModel<
		MamutAppModel,
		Model,
		ModelGraph,
		Spot,
		Link >
{

	public static MamutAppModel create( final Context context, final Model model, final SharedBigDataViewerData imageData, final MamutProject project )
	{
		final KeymapManager keymapManager = new MastodonKeymapManager( true );
		return new MamutAppModel( context, model, imageData, keymapManager, project );
	}

	private static final int NUM_GROUPS = 3;

	public static final String DOCUMENTATION_URL = "https://mastodon.readthedocs.io/en/latest/";

	private final BoundingSphereRadiusStatistics radiusStats;

	private MamutAppModel(
			final Context context,
			final Model model,
			final SharedBigDataViewerData sharedBdvData,
			final KeymapManager keymapManager,
			final MamutProject project )
	{
		super(
				context,
				model,
				sharedBdvData,
				MamutViewFactory2.class,
				new KeyPressedManager(),
				new MastodonKeymapManager( true ),
				new MamutPlugins( keymapManager.getForwardSelectedKeymap() ),
				new Actions( keymapManager.getForwardSelectedKeymap().getConfig(), KeyConfigContexts.MASTODON ),
				project,
				new String[] { KeyConfigContexts.MASTODON },
				KeyConfigScopes.MAMUT,
				NUM_GROUPS );
		this.radiusStats = new BoundingSphereRadiusStatistics( model );
	}

	public MamutViewBdv createBdv()
	{
		return uiModel.createView( this, MamutViewBdv.class );
	}

	public MamutViewBranchBdv createBranchBdv()
	{
		return uiModel.createView( this, MamutViewBranchBdv.class );
	}

	public MamutViewTrackScheme2 createTrackScheme()
	{
		return uiModel.createView( this, MamutViewTrackScheme2.class );
	}

	public MamutViewBranchTrackScheme2 createBranchTrackScheme()
	{
		return uiModel.createView( this, MamutViewBranchTrackScheme2.class );
	}

	public MamutViewHierarchyTrackScheme2 createHierarchyTrackScheme()
	{
		return uiModel.createView( this, MamutViewHierarchyTrackScheme2.class );
	}

	public MamutViewTable2 createTable()
	{
		return uiModel.createView( this, MamutViewTable2.class );
	}

	public MamutViewSelectionTable2 createSelectionTable()
	{
		return uiModel.createView( this, MamutViewSelectionTable2.class );
	}

	public MamutViewGrapher createGrapher()
	{
		return uiModel.createView( this, MamutViewGrapher.class );
	}

	public MamutViewBranchGrapher createBranchGrapher()
	{
		return uiModel.createView( this, MamutViewBranchGrapher.class );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Edit Mastodon preferences." );
			descriptions.add( TAGSETS_DIALOG, TAGSETS_DIALOG_KEYS, "Edit tag definitions." );
			descriptions.add( COMPUTE_FEATURE_DIALOG, COMPUTE_FEATURE_DIALOG_KEYS, "Show the feature computation dialog." );
			descriptions.add( OPEN_ONLINE_DOCUMENTATION, OPEN_ONLINE_DOCUMENTATION_KEYS, "Open a browser with the online documentation for Mastodon." );
		}
	}

	/**
	 * Exposes the statistics about bounding sphere radii of spots in the model.
	 * 
	 * @return the radius statistics.
	 */
	public BoundingSphereRadiusStatistics getRadiusStats()
	{
		return radiusStats;
	}
}
