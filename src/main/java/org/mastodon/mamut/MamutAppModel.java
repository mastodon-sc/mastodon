package org.mastodon.mamut;

import org.mastodon.app.BdvAppModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugins;
import org.mastodon.mamut.views.MamutViewFactory2;
import org.mastodon.mamut.views.table.MastodonViewTable2;
import org.mastodon.mamut.views.trackscheme.MastodonViewTrackScheme2;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.MastodonKeymapManager;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.KeymapManager;

/**
 * Core app model for the Mastodon app 'Mamut', that displays cells as
 * ellipsoids. It is typed against {@link Spot} and {@link Link}.
 */
@SuppressWarnings( "rawtypes" )
public class MamutAppModel extends BdvAppModel<
		Model,
		ModelGraph,
		Spot,
		Link,
		MamutViewFactory2 >
{

	public static MamutAppModel create( final Context context, final Model model, final SharedBigDataViewerData imageData, final MamutProject project )
	{
		final KeymapManager keymapManager = new MastodonKeymapManager( true );
		return new MamutAppModel( context, model, imageData, keymapManager, project );
	}

	private static final int NUM_GROUPS = 3;

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
				new String[] { KeyConfigContexts.MASTODON },
				KeyConfigScopes.MAMUT,
				NUM_GROUPS );
	}

	@SuppressWarnings( "unchecked" )
	public MastodonViewTrackScheme2< Model, ModelGraph, Spot, Link > createTrackScheme()
	{
		return uiModel.createView( this, MastodonViewTrackScheme2.class );
	}

	@SuppressWarnings( "unchecked" )
	public MastodonViewTable2< Model, ModelGraph, Spot, Link > createTable()
	{
		return uiModel.createView( this, MastodonViewTable2.class );
	}
}
