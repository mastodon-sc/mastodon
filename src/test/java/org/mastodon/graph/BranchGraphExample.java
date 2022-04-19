package org.mastodon.graph;

import java.io.IOException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.MamutBranchViewBdv;
import org.mastodon.mamut.MamutBranchViewTrackScheme;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelUtils;
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

			final String str = ModelUtils.dump( model );
			System.out.println( str ); // DEBUG

			final ModelBranchGraph bg = new ModelBranchGraph( model.getGraph() );
			System.out.println( bg ); // DEBUG

			new MamutBranchViewBdv( appModel );
			new MamutBranchViewTrackScheme( appModel );


		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}

	}

}
