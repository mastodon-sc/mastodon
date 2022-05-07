package org.mastodon.graph;

import java.io.IOException;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.ViewFrame;
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
import org.mastodon.views.table.TableViewFrameBuilder;
import org.scijava.Context;

public class BranchGraphExample
{

	public static void main( final String[] args ) throws IOException
	{
		try (final Context context = new Context())
		{
			final MamutProject project = new MamutProjectIO().load( "samples/test_branchgraph.mastodon" );
//			final MamutProject project = new MamutProjectIO().load( "samples/mette_e1.mastodon" );
//			final MamutProject project = new MamutProjectIO().load( "samples/mette_e1_small.mastodon" );

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
			final ViewFrame viewFrame = builder
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
						.listenToContext( true )
						.done()
					.addGraph( appModel.getModel().getBranchGraph() )
						.vertexLabelGetter( s -> s.getLabel() )
						.featureModel( appModel.getModel().getFeatureModel() )
						.done()
					.get();
			
			viewFrame.setSize( 400, 400 );
			viewFrame.setLocationRelativeTo( null );
			viewFrame.setVisible( true );
		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}
	}

}
