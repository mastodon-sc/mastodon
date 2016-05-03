package net.trackmate.graph.revised;

import java.util.Random;

import net.imglib2.util.BenchmarkHelper;
import net.trackmate.graph.features.IntVertexFeature;
import net.trackmate.revised.model.mamut.Model;
import net.trackmate.revised.model.mamut.Spot;

public class FeaturesBenchmark
{

	private static final Random ran = new Random( 0l );

	public static void main( final String[] args )
	{
		final int N_RUNS = 100;

		System.out.println( "Creating model." );
		final long s = System.currentTimeMillis();
		final Model model = new CreateLargeModelExample().run( 6, 18, 8 );
		final int size = model.getGraph().vertices().size();
		final long e = System.currentTimeMillis();
		System.out.println( String.format( "Created a model with %d spots in %.1f s.", size, ( ( double ) e - s ) / 1000. ) );

		final int[] storage = new int[ size ];

		final IntVertexFeature< Spot > POSITIVE_NUMBER = new IntVertexFeature< >( "POS_NUMBER", -1 );

		System.out.println();
		final long s1 = System.currentTimeMillis();
		BenchmarkHelper.benchmarkAndPrint( N_RUNS, false, () -> putFeatureValue( model, POSITIVE_NUMBER ) );
		final long e1 = System.currentTimeMillis();
		System.out.println( String.format( "Put a int feature value in %d spots in %.1f ms.", size, ( ( double ) e1 - s1 ) / N_RUNS ) );

		System.out.println();
		final long s2 = System.currentTimeMillis();
		BenchmarkHelper.benchmarkAndPrint( N_RUNS, false, () -> readGraphValue( model, storage ) );
		final long e2 = System.currentTimeMillis();
		System.out.println( String.format( "Read a int graph value in %d spots in %.1f ms.", size, ( ( double ) e2 - s2 ) / N_RUNS ) );

		System.out.println();
		final long s3 = System.currentTimeMillis();
		BenchmarkHelper.benchmarkAndPrint( N_RUNS, false, () -> readFeatureValue( model, storage, POSITIVE_NUMBER ) );
		final long e3 = System.currentTimeMillis();
		System.out.println( String.format( "Read a int feature value in %d spots in %.1f ms.", size, ( ( double ) e3 - s3 ) / N_RUNS ) );

	}

	private static void readFeatureValue( final Model model, final int[] storage, final IntVertexFeature< Spot > feature )
	{
		int index = 0;
		for ( final Spot spot : model.getGraph().vertices() )
		{
			storage[ index++ ] = spot.feature( feature ).getInt();
		}
	}

	private static void putFeatureValue( final Model model, final IntVertexFeature< Spot > feature )
	{
		for ( final Spot spot : model.getGraph().vertices() )
		{
			spot.feature( feature ).set( spot.getTimepoint() );
		}
	}

	private static void readGraphValue( final Model model, final int[] storage )
	{
		int index = 0;
		for ( final Spot spot : model.getGraph().vertices() )
		{
			storage[ index++ ] = spot.getTimepoint();
		}
	}

}
