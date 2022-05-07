package org.mastodon.graph;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.NavigationHandler;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.views.table.FeatureTagTablePanel;
import org.mastodon.views.table.TableViewActions;
import org.mastodon.views.table.TableViewFrameBuilder;
import org.mastodon.views.table.TableViewFrameBuilder.MyTableViewFrame;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import com.opencsv.CSVWriter;

public class BranchGraphExample
{

	private static final String[] KEYCONFIG_CONTEXTS = new String[] { KeyConfigContexts.TABLE };

	public static final String EDIT_LABEL = "edit vertex label";

	public static final String TOGGLE_TAG = "toggle tag";

	public static final String EXPORT_TO_CSV = "export to csv";

	private static final String[] EDIT_LABEL_KEYS = new String[] { "F2" };

	private static final String[] TOGGLE_TAG_KEYS = new String[] { "SPACE" };

	private static final String[] EXPORT_TO_CSV_KEYS = new String[] { "not mapped" };

	public static String csvExportPath = null;

	public static void main( final String[] args ) throws IOException
	{
		try (final Context context = new Context())
		{
			final String projectPath = "samples/test_branchgraph.mastodon";
//			final String projectPath = "samples/mette_e1.mastodon";
//			final String projectPath = "samples/mette_e1_small.mastodon";
			final MamutProject project = new MamutProjectIO().load( projectPath );

			final WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			new MainWindow( wm ).setVisible( true );
//			final ModelBranchGraph gb = wm.getAppModel().getModel().getBranchGraph();
//			gb.addGraphChangeListener( () -> System.out.println( gb ) );

			final MamutAppModel appModel = wm.getAppModel();
			final ModelGraph graph = appModel.getModel().getGraph();
			final ViewGraph< Spot, Link, Spot, Link > viewGraph = IdentityViewGraph.wrap( graph, appModel.getModel().getGraphIdBimap() );
			final GraphColorGeneratorAdapter< Spot, Link, Spot, Link > coloringAdapter = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

			final GroupHandle groupHandle = appModel.getGroupManager().createGroupHandle();
			final NavigationHandler< Spot, Link > navigationHandler = groupHandle.getModel( appModel.NAVIGATION );

			final TableViewFrameBuilder builder = new TableViewFrameBuilder();
			final MyTableViewFrame frame = builder
					.groupHandle( groupHandle )
					.undo( appModel.getModel() )
					.addGraph( appModel.getModel().getGraph() )
						.selectionModel( appModel.getSelectionModel() )
						.highlightModel( appModel.getHighlightModel() )
						.focusModel( appModel.getFocusModel() )
						.featureModel( appModel.getModel().getFeatureModel() )
						.tagSetModel( appModel.getModel().getTagSetModel() )
						.navigationHandler( navigationHandler )
						.coloring( coloringAdapter )
						.vertexLabelGetter( s -> s.getLabel() )
						.vertexLabelSetter( ( s, label ) -> s.setLabel( label ) )
						.listenToContext( true )
						.done()
					.addGraph( appModel.getModel().getBranchGraph() )
						.vertexLabelGetter( s -> s.getLabel() )
						.featureModel( appModel.getModel().getFeatureModel() )
						.done()
					.get();
			
			/*
			 * Table actions.
			 */
			
			csvExportPath = projectPath;

			final RunnableAction editLabel = new RunnableAction( EDIT_LABEL, frame::editCurrentLabel );
			final RunnableAction toggleTag = new RunnableAction( TOGGLE_TAG, frame::toggleCurrentTag );
			final RunnableAction exportToCSV = new RunnableAction( EXPORT_TO_CSV, () -> exportToCSV( frame.getCurrentlyDisplayedTable() ) );

			final Keymap keymap = appModel.getKeymap();
			final Actions viewActions = new Actions( keymap.getConfig(), KEYCONFIG_CONTEXTS );
			viewActions.install( frame.getKeybindings(), "view" );

			viewActions.namedAction( editLabel, EDIT_LABEL_KEYS );
			viewActions.namedAction( toggleTag, TOGGLE_TAG_KEYS );
			viewActions.namedAction( exportToCSV, EXPORT_TO_CSV_KEYS );

			frame.setSize( 400, 400 );
			frame.setLocationRelativeTo( null );
			frame.setVisible( true );
		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TABLE );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( EDIT_LABEL, EDIT_LABEL_KEYS, "Edit the label of the current vertex." );
			descriptions.add( TOGGLE_TAG, TOGGLE_TAG_KEYS, "Toggle the tag at the current cell in the table." );
			descriptions.add( EXPORT_TO_CSV, EXPORT_TO_CSV_KEYS, "Export the current content of the table to two CSV files (one for vertices, one for edges)." );
		}
	}

	private static void exportToCSV( final FeatureTagTablePanel< ? > table )
	{
		// Try to get at least one object.
		final Object obj = table.getObjectForViewRow( 0 );
		if ( obj == null )
			return;

		// Ask for filename.
		final String klass = obj.getClass().getSimpleName();
		final String filename = ( csvExportPath == null )
				? new File( System.getProperty( "user.home" ), "MastodonTable.csv" ).getAbsolutePath()
				: csvExportPath;
		final File file = FileChooser.chooseFile(
				table,
				filename,
				new ExtensionFileFilter( "csv" ),
				"Export " + klass + " table as CSV file",
				FileChooser.DialogType.SAVE );
		if ( file == null )
			return;
		csvExportPath = file.getAbsolutePath();

		final int p = csvExportPath.lastIndexOf( '.' );
		final String path = csvExportPath.substring( 0, p ) + "-" + klass + ".csv";
		try
		{
			TableViewActions.export( path, table, CSVWriter.DEFAULT_SEPARATOR );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog( table,
					"Could not save to file " + path + ":\n" + e.getMessage(),
					"Error exporting vertices to CSV",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}
}
