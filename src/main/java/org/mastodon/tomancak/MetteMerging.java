package org.mastodon.tomancak;

import java.io.IOException;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;

public class MetteMerging
{
	public static final String basepath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/";

	public static final String[] paths = {
			basepath + "1.SimView2_20130315_Mastodon_Automat-segm-t0-t300",
			basepath + "2.SimView2_20130315_Mastodon_MHT",
			basepath + "3.Pavel manual",
			basepath + "4.Vlado_TrackingPlatynereis",
			basepath + "5.SimView2_20130315_Mastodon_Automat-segm-t0-t300_JG"
	};

	public static class Dataset
	{
		final MamutProject project;

		final Model model;

		Dataset( String path ) throws IOException
		{
			project = new MamutProjectIO().load( path );
			model = new Model();
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				model.loadRaw( reader );
			}
		}

		public void verify()
		{
			for ( Spot spot : model.getGraph().vertices() )
			{
				if ( spot.incomingEdges().size() > 1 )
					System.err.println( spot + " has more than one parent" );

				if ( spot.outgoingEdges().size() > 2 )
					System.err.println( spot + " has more than two children" );
			}
		}

		public void labels()
		{
			final SpotPool pool = ( SpotPool ) model.getGraph().vertices().getRefPool();
			ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
			System.out.println( "pool = " + pool );
			System.out.println( "labels = " + labels );

			for ( Spot spot : model.getGraph().vertices() )
			{
				if ( labels.isSet( spot ) )
				{
					System.out.println( "spot = " + spot + " label=" + labels.get( spot ) );
				}
			}
		}
	}

	static void compare( Model m1, Model m2 )
	{
		m1.getSpatioTemporalIndex();
	}

	public static void main( String[] args ) throws IOException
	{
		for ( String path : paths )
		{
			new Dataset( path ).verify();
		}
	}
}
