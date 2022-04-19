package org.mastodon.graph;

import java.io.File;
import java.io.IOException;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class TestBranchGraph
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{

		final WindowManager wm = new WindowManager( new Context() );
		wm.getProjectManager().open( new MamutProject( null, new File( "x=100 y=100 z=100 sx=1 sy=1 sz=1 t=10.dummy" ) ) );

		final Model model = wm.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();
		
		final Spot s0 = graph.addVertex().init( 0, new double[] { 0., 0., 0. }, 1. );
		final Spot s1 = graph.addVertex().init( 1, new double[] { 0., 0., 0. }, 1. );
		graph.addEdge( s0, s1 ).init();
		System.out.println( model.getBranchGraph() );

		final Spot s2 = graph.addVertex().init( 2, new double[] { 0., 0., 0. }, 1. );
		graph.addEdge( s1, s2 ).init();
		System.out.println( model.getBranchGraph() );

		final Spot s3 = graph.addVertex().init( 3, new double[] { 0., 0., 0. }, 1. );
		final Link e23 = graph.addEdge( s2, s3 ).init();
		System.out.println( model.getBranchGraph() );

		final Spot s4 = graph.addVertex().init( 4, new double[] { 0., 0., 0. }, 1. );
		graph.addEdge( s3, s4 ).init();
		System.out.println( model.getBranchGraph() );

		final Spot s5 = graph.addVertex().init( 3, new double[] { 0., 0., 0. }, 1. );
		graph.addEdge( s2, s5 ).init();
		System.out.println( model.getBranchGraph() );

		final Spot s6 = graph.addVertex().init( 4, new double[] { 0., 0., 0. }, 1. );
		graph.addEdge( s5, s6 ).init();
		System.out.println( model.getBranchGraph() );
		
		graph.remove( e23 );
		System.out.println( model.getBranchGraph() );

		System.out.println( "Finished!" ); 
		System.out.println( graph ); 

		wm.createBranchTrackScheme();

	}
}
