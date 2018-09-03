package org.mastodon.feature;

import static org.scijava.ItemIO.OUTPUT;

import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

public class Playground
{
	public static class F1 implements Feature< Spot >
	{
		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F1, Spot >
		{
			public Spec()
			{
				super( "F1", F1.class, Spot.class, "p1", "p2" );
			}
		}

		@Override
		public FeatureProjection<Spot> project( final String projectionKey )
		{
			return null;
		}
	}

	public static class F2 implements Feature< Spot >
	{
		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F2, Spot >
		{
			public Spec()
			{
				super( "F2", F2.class, Spot.class, "q1", "q2" );
			}
		}

		@Override
		public FeatureProjection<Spot> project( final String projectionKey )
		{
			return null;
		}
	}

	public static class F3 implements Feature< Spot >
	{
		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F3, Spot >
		{
			public Spec()
			{
				super( "F3", F3.class, Spot.class, "p" );
			}
		}

		@Override
		public FeatureProjection<Spot> project( final String projectionKey )
		{
			return null;
		}
	}

	public static class F4 implements Feature< Spot >
	{
		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F4, Spot >
		{
			public Spec()
			{
				super( "F4", F4.class, Spot.class, "p" );
			}
		}

		@Override
		public FeatureProjection<Spot> project( final String projectionKey )
		{
			return null;
		}
	}

	@Plugin( type = FC1.class )
	public static class FC1 implements FeatureComputer
	{
		@Parameter( type = OUTPUT )
		private F1 f1;

		@Override
		public void run()
		{
		}

		@Override
		public void createOutput()
		{
			f1 = new F1();
		}
	}

	@Plugin( type = FC2.class )
	public static class FC2 implements FeatureComputer
	{
		@Parameter
		private F1 f1;

		@Parameter( type = OUTPUT )
		private F2 f2;

		@Override
		public void run()
		{
		}

		@Override
		public void createOutput()
		{
			if ( f1 == null )
				throw new IllegalStateException( "inputs not set!" );
			f2 = new F2();
		}
	}

	@Plugin( type = FC3.class )
	public static class FC3 implements FeatureComputer
	{
		@Parameter
		private F1 f1;

		@Parameter
		private F2 f2;

		@Parameter( type = OUTPUT )
		private F3 f3;

		@Override
		public void run()
		{
		}

		@Override
		public void createOutput()
		{
			if ( f1 == null || f2 == null )
				throw new IllegalStateException( "inputs not set!" );
			f3 = new F3();
		}
	}

	@Plugin( type = FC4.class )
	public static class FC4 implements FeatureComputer
	{
		@Parameter
		private F1 f1;

		@Parameter( type = OUTPUT )
		private F4 f4;

		@Override
		public void run()
		{
		}

		@Override
		public void createOutput()
		{
			if ( f1 == null )
				throw new IllegalStateException( "inputs not set!" );
			f4 = new F4();
		}
	}

	public static void main( final String[] args )
	{
		final Context context = new Context( PluginService.class, CommandService.class, FeatureSpecsService.class );
		final FeatureComputerService featureComputerService = new FeatureComputerService();
		context.inject( featureComputerService );
		featureComputerService.initialize();
		featureComputerService.compute( "F1", "F3" );

//		testSpecs( context );
	}

	private static void testSpecs( final Context context )
	{
		final FeatureSpecsService specs = new FeatureSpecsService();
		context.inject( specs );
		specs.initialize();
		System.out.println( "specs.getSpecs( Spot.class ) = " + specs.getSpecs( Spot.class ) );
		System.out.println( "specs.getSpecs( Link.class ) = " + specs.getSpecs( Link.class ) );
		System.out.println( "specs.getSpec( \"F1\" ) = " + specs.getSpec( "F1" ) );
		System.out.println( "specs.getSpec( \"F2\" ) = " + specs.getSpec( "F2" ) );
		System.out.println( "specs.getSpec( \"F3\" ) = " + specs.getSpec( "F3" ) );
	}
}
