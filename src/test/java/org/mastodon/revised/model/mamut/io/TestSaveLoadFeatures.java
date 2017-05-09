package org.mastodon.revised.model.mamut.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

public class TestSaveLoadFeatures
{
	public static void main( final String[] args ) throws IOException
	{
		final String modelFile = "samples/model_revised.raw";

		final Model model = new Model();
		model.loadRaw( new File( modelFile ) );

		final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
		System.out.println( "Found " + vertices.size() + " vertices." ); // DEBUG

		final DoublePropertyMap< Spot > order = new DoublePropertyMap<>( vertices, Double.NaN, vertices.size() );
		int index = 100;
		for ( final Spot spot : vertices )
			order.set( spot, index++ );

		// Take one spot.
		final Iterator< Spot > it = vertices.iterator();
		it.next();
		it.next();
		final Spot spot = it.next();
		System.out.println( "\nBefore removal." );
		System.out.println( spot + " -> " + order.get( spot ) + ", id = " + spot.getInternalPoolIndex() );

		// Remove it
		model.getGraph().remove( spot );

		// Still there?
		System.out.println( "\nAfter removal." );
		System.out.println( spot + " -> " + order.isSet( spot ) + ", id = " + spot.getInternalPoolIndex() );

		// Add a new spot.
		System.out.println( "\nNew spot, possibly occupying the same id." );
		final Spot spot2 = model.getGraph().addVertex().init( 0, new double[ 3 ], new double[ 3 ][ 3 ] );
		System.out.println( spot2 + " -> " + order.isSet( spot2 ) + ", id = " + spot2.getInternalPoolIndex() );

		// Set its value
		System.out.println( "\nSet the value of the new spot." );
		order.set( spot2, 3.14 );
		System.out.println( spot2 + " -> " + order.get( spot2 ) + ", id = " + spot2.getInternalPoolIndex() );
	}
}
