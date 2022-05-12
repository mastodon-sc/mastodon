package org.mastodon.graph;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.model.branch.BranchGraphTagSetAdapter;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.ui.coloring.BranchGraphColoringModel;
import org.mastodon.ui.coloring.ColoringMenu;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModelWithBranchGraph;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
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
		setSystemLookAndFeelAndLocale();
		try (final Context context = new Context())
		{
//			final String projectPath = "samples/test_branchgraph.mastodon";
			final String projectPath = "samples/mette_e1.mastodon";
//			final String projectPath = "samples/mette_e1_small.mastodon";
			final MamutProject project = new MamutProjectIO().load( projectPath );

			final WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			wm.getAppModel().getModel().getBranchGraph().graphRebuilt();
			new MainWindow( wm ).setVisible( true );

			csvExportPath = projectPath;
			new Thread( () -> createBranchTable( wm.getAppModel() ) ).start();
		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}
	}

	private static MyTableViewFrame createBranchTable( final MamutAppModel appModel )
	{
		// Rebuild the branch graph.
		final ReadLock lock = appModel.getModel().getGraph().getLock().readLock();
		lock.lock();
		try
		{
			appModel.getModel().getBranchGraph().graphRebuilt();
		}
		finally
		{
			lock.unlock();
		}

		// Build the global table view.
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
					.vertexLabelSetter( ( s, label ) -> s.setLabel( label ) )
					.featureModel( appModel.getModel().getFeatureModel() )
					.tagSetModel( branchTagSetModel( appModel ) )
					.selectionModel( branchSelectionModel( appModel ) )
					.highlightModel( branchHighlightModel( appModel ) )
					.focusModel( branchFocusfocusModel( appModel ) )
					.navigationHandler( branchGraphNavigation( appModel, navigationHandler ) )
					.done()
				.get();

		/*
		 * Table actions.
		 */

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

		return frame;
	}

	private static final ColoringModel registerColoring(
			final MamutAppModel appModel,
			final GraphColorGeneratorAdapter< BranchSpot, BranchLink, BranchSpot, BranchLink > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final FeatureModel featureModel = appModel.getModel().getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = appModel.getFeatureColorModeManager();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final BranchGraphColoringModel< BranchSpot, BranchLink > coloringModel = new BranchGraphColoringModel<>( tagSetModel, featureColorModeManager, featureModel );
		final ColoringMenu coloringMenu = new ColoringMenu( menuHandle.getMenu(), coloringModel );

		tagSetModel.listeners().add( coloringModel );
		onClose( () -> tagSetModel.listeners().remove( coloringModel ) );
		tagSetModel.listeners().add( coloringMenu );
		onClose( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.listeners().add( coloringModel );
		onClose( () -> featureColorModeManager.listeners().remove( coloringModel ) );
		featureColorModeManager.listeners().add( coloringMenu );
		onClose( () -> featureColorModeManager.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		onClose( () -> featureModel.listeners().remove( coloringMenu ) );

		final ColoringModelWithBranchGraph.ColoringChangedListener coloringChangedListener = () -> {
			if ( coloringModel.noColoring() )
				colorGeneratorAdapter.setColorGenerator( null );
			else if ( coloringModel.getTagSet() != null )
				colorGeneratorAdapter.setColorGenerator( new TagSetGraphColorGenerator<>( branchTagSetModel( appModel ), coloringModel.getTagSet() ) );
			else if ( coloringModel.getFeatureColorMode() != null )
				colorGeneratorAdapter.setColorGenerator( coloringModel.getFeatureGraphColorGenerator() );
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );

		return coloringModel;
	}

	private static void onClose( final Runnable runnable )
	{
		// TODO Auto-generated method stub
		System.out.println( "added " + runnable ); // DEBUG
	}

	private static TagSetModel< BranchSpot, BranchLink > branchTagSetModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final BranchGraphTagSetAdapter< Spot, Link, BranchSpot, BranchLink > branchGraphTagSetModel =
				new BranchGraphTagSetAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), tagSetModel );
		return branchGraphTagSetModel;
	}

	private static NavigationHandler< BranchSpot, BranchLink > branchGraphNavigation( final MamutAppModel appModel, final NavigationHandler< Spot, Link > navigationHandler )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final NavigationHandler< BranchSpot, BranchLink > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), navigationHandler );
		return branchGraphNavigation;
	}

	private static HighlightModel< BranchSpot, BranchLink > branchHighlightModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final HighlightModel< Spot, Link > graphHighlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchSpot, BranchLink > branchHighlightModel =
				new BranchGraphHighlightAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphHighlightModel );
		return branchHighlightModel;
	}

	private static FocusModel< BranchSpot, BranchLink > branchFocusfocusModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final FocusModel< Spot, Link > graphFocusModel = appModel.getFocusModel();
		final FocusModel< BranchSpot, BranchLink > branchFocusfocusModel =
				new BranchGraphFocusAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphFocusModel );
		return branchFocusfocusModel;
	}

	private static SelectionModel< BranchSpot, BranchLink > branchSelectionModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final SelectionModel< Spot, Link > graphSelectionModel = appModel.getSelectionModel();
		final SelectionModel< BranchSpot, BranchLink > branchSelectionModel =
				new BranchGraphSelectionAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphSelectionModel );
		return branchSelectionModel;
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

	private static final void setSystemLookAndFeelAndLocale()
	{
		Locale.setDefault( Locale.ROOT );
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			e.printStackTrace();
		}
	}
}
