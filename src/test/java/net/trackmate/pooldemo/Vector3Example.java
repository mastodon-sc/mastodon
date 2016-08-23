package net.trackmate.pooldemo;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealPoint;
import net.trackmate.collection.ref.RefArrayList;
import net.trackmate.graph.features.DoubleFeature;
import net.trackmate.graph.features.ObjFeature;

public class Vector3Example
{
	public static void main( final String[] args )
	{
		final Vector3Pool pool = new Vector3Pool( 1000000 );

		// Object vs PoolObject
		{
			final RealPoint p = new RealPoint( 1.0, 1.0, 1.0 );
			final Vector3 v = pool.create().init( 1.0, 1.0, 1.0 );
			System.out.println( p );
			System.out.println( v );
		}


		// ArrayList stores one proxy Object for each element
		{
			final List< Vector3 > vecs = new ArrayList<>();
			for ( int i = 0; i < 10; ++i )
				vecs.add( pool.create().init( i, i, i ) );
			System.out.println( vecs );
		}

		// Reusing proxies.
		// RefArrayList just stores pool indices in Trove TIntArrayList. No proxy Objects.
		{
			final Vector3 ref = pool.createRef();

			final List< Vector3 > vecs = new RefArrayList<>( pool );
			for ( int i = 0; i < 3; ++i )
				vecs.add( pool.create( ref ).init( i, i, i ) );
			System.out.println( vecs );

			pool.releaseRef( ref );
		}

		// Be careful when reusing proxies.
		{
			final Vector3 ref = pool.createRef();

			final Vector3 v2 = pool.create( ref ).init( 2, 2, 2 );
			final Vector3 v3 = pool.create( ref ).init( 3, 3, 3 ); // v3 == v2 == ref !!!

			pool.releaseRef( ref );
		}

		// adding Features to PoolObjects
		{
			final Vector3 ref = pool.createRef();

			final ObjFeature< Vector3, String > COLOR = new ObjFeature<>( "v3 color" );
			final DoubleFeature< Vector3 > RADIUS = new DoubleFeature<>( "v3 radius", Double.NEGATIVE_INFINITY );

			final List< Vector3 > vecs = new RefArrayList< >( pool );
			for ( int i = 0; i < 10; ++i )
			{
				final Vector3 v = pool.create( ref ).init( i, i, i );
				if ( i % 2 == 0 )
					v.feature( COLOR ).set( "blue" );
				vecs.add( v );
			}

			vecs.stream()
				.filter( v -> v.feature( COLOR ).get() == "blue" )
				.forEach( v -> System.out.println( "LOOK!!! a blue vector! " + v ) );

			final Vector3 v = vecs.get( 5 );
			v.feature( COLOR ).get();
			v.feature( RADIUS ).getDouble();

			pool.releaseRef( ref );
		}
	}
}
