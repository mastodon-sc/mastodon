package org.mastodon.revised.ui.keymap;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.mastodon.app.ui.CloseWindowActions;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.revised.bdv.BehaviourTransformEventHandler3DMamut;
import org.mastodon.revised.bdv.BigDataViewerActionsMamut;
import org.mastodon.revised.bdv.NavigationActionsMamut;
import org.mastodon.revised.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.revised.bdv.overlay.EditBehaviours;
import org.mastodon.revised.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.mamut.ProjectManager;
import org.mastodon.revised.mamut.UndoActions;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.revised.trackscheme.display.InertialScreenTransformEventHandler;
import org.mastodon.revised.trackscheme.display.ToggleLinkBehaviour;
import org.mastodon.revised.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.revised.trackscheme.display.TrackSchemeNavigationBehaviours;
import org.mastodon.revised.ui.EditTagActions;
import org.mastodon.revised.ui.FocusActions;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.scijava.Context;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

public class DumpInputConfig
{
	static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir != null && dir.mkdirs();
	}

	public static void writeToYaml( final String fileName, final WindowManager wm ) throws IOException
	{
		mkdirs( fileName );
		final List< InputTriggerDescription > descriptions = new InputTriggerDescriptionsBuilder( wm.getAppModel().getKeymap().getConfig() ).getDescriptions();
		YamlConfigIO.write( descriptions, fileName );
	}

	public static void writeDefaultConfigToYaml( final String fileName, final Context context ) throws IOException
	{
		mkdirs( fileName );
		final List< InputTriggerDescription > descriptions = new InputTriggerDescriptionsBuilder( buildCommandDescriptions( context ).createDefaultKeyconfig() ).getDescriptions();
		YamlConfigIO.write( descriptions, fileName );
	}

	private static CommandDescriptions buildCommandDescriptions( final Context context )
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		context.inject( builder );

		builder.addManually( new CloseWindowActions.Descriptions(), KeyConfigContexts.MASTODON);
		builder.addManually( new ProjectManager.Descriptions(), KeyConfigContexts.MASTODON);
		builder.addManually( new UndoActions.Descriptions(), KeyConfigContexts.MASTODON);
		builder.addManually( new SelectionActions.Descriptions(), KeyConfigContexts.MASTODON);
		builder.addManually( new WindowManager.Descriptions(), KeyConfigContexts.MASTODON);

		builder.addManually( new MastodonFrameViewActions.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new BehaviourTransformEventHandler3DMamut.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new BigDataViewerActionsMamut.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new NavigationActionsMamut.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new BdvSelectionBehaviours.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new EditBehaviours.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new EditSpecialBehaviours.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new FocusActions.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );
		builder.addManually( new HighlightBehaviours.Descriptions(), KeyConfigContexts.BIGDATAVIEWER );

		builder.addManually( new MastodonFrameViewActions.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new InertialScreenTransformEventHandler.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new EditFocusVertexLabelAction.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new ToggleLinkBehaviour.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new TrackSchemeNavigationActions.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new TrackSchemeNavigationBehaviours.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new EditTagActions.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new FocusActions.Descriptions(), KeyConfigContexts.TRACKSCHEME );
		builder.addManually( new HighlightBehaviours.Descriptions(), KeyConfigContexts.TRACKSCHEME );

		builder.verifyManuallyAdded();

		return builder.build();
	}

}
