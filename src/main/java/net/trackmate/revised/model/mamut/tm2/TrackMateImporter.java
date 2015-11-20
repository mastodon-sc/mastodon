package net.trackmate.revised.model.mamut.tm2;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.IntRefMap;
import net.trackmate.revised.model.mamut.Link;
import net.trackmate.revised.model.mamut.Model;
import net.trackmate.revised.model.mamut.Spot;

import org.jgrapht.graph.DefaultWeightedEdge;

import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.io.TmXmlReader;

public class TrackMateImporter
{
	public Model importTrackMate( final File file )
	{
		final TmXmlReader reader = new TmXmlReader( file );
		if ( !reader.isReadingOk() )
		{
			System.err.println( reader.getErrorMessage() );
			return null;
		}

		final fiji.plugin.trackmate.Model m = reader.getModel();
		final TrackModel tm = m.getTrackModel();
		final Set< Integer > trackIDs = tm.trackIDs( true );

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		final Model model = new Model();

		final Spot ref1 = model.getGraph().vertexRef();
		final Spot ref2 = model.getGraph().vertexRef();
		final Link edgeRef = model.getGraph().edgeRef();

		for ( final Integer trackID : trackIDs )
		{
			final IntRefMap< Spot > map = CollectionUtils.createIntVertexMap( model.getGraph(), -1 );

			final Set< fiji.plugin.trackmate.Spot > spots = tm.trackSpots( trackID );
			for ( final fiji.plugin.trackmate.Spot spot : spots )
			{
				spot.localize( pos );
				final double radius = spot.getFeature( fiji.plugin.trackmate.Spot.RADIUS );
				cov[0][0] = radius;
				cov[1][1] = radius;
				cov[2][2] = radius;
				final int id = spot.ID();
				final int frame = spot.getFeature( fiji.plugin.trackmate.Spot.FRAME ).intValue();

				final net.trackmate.revised.model.mamut.Spot addSpot = model.addSpot( frame, pos, cov, ref1 );
				map.put( id, addSpot );
			}

			final Set< DefaultWeightedEdge > edges = tm.trackEdges( trackID );
			for ( final DefaultWeightedEdge edge : edges )
			{
				final fiji.plugin.trackmate.Spot source = tm.getEdgeSource( edge );
				final int sourceID = source.ID();

				final fiji.plugin.trackmate.Spot target = tm.getEdgeTarget( edge );
				final int targetID = target.ID();

				model.addLink( map.get( sourceID, ref1 ), map.get( targetID, ref2 ), edgeRef );
			}

		}

		model.getGraph().releaseRef( edgeRef );
		model.getGraph().releaseRef( ref1 );
		model.getGraph().releaseRef( ref2 );

		return model;
	}

	public static void main( final String[] args ) throws IOException
	{
		final File file = new File( "/Users/tinevez/Projects/JYTinevez/CelegansLineage/Data/LSM700U/10-03-29-3hours.xml" );
		final File target = new File( "/Volumes/Data/Celegans.raw" );

		final long start = System.currentTimeMillis();
		System.out.println( "Importing " + file );
		final Model model = new TrackMateImporter().importTrackMate( file );
		System.out.println( "Saving to " + target );
		model.saveRaw( target );
		final long end = System.currentTimeMillis();
		System.out.println( "Total time: " + ( end - start ) / 1000 + " s." );
	}
}
