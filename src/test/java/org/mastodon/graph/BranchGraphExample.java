package org.mastodon.graph;

import java.io.IOException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

public class BranchGraphExample
{

	public static void main( final String[] args ) throws IOException
	{
		try (Context context = new Context())
		{
			final MamutProject project = new MamutProjectIO().load( "samples/test_branchgraph.mastodon" );

			final WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			final MamutAppModel appModel = wm.getAppModel();

			new MainWindow( wm ).setVisible( true );

			final Model model = appModel.getModel();
			final ModelBranchGraph bg = new ModelBranchGraph( model.getGraph() );


//			new MamutBranchViewBdv( appModel );
//			final MamutBranchViewTrackScheme view = new MamutBranchViewTrackScheme( appModel );
//			new MamutBranchViewTrackSchemeHierarchy( appModel );

			model.getGraph().addGraphChangeListener( () -> {
				System.out.println( bg );
			} );
		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}

	}

}
