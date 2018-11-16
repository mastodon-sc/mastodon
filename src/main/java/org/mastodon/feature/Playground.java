package org.mastodon.feature;

import static org.scijava.ItemIO.OUTPUT;

import java.util.Collections;
import java.util.Set;

import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

public class Playground
{
	public static class PlaygroundSpot {};

	public static class PlaygroundLink {};

	public static class F1 implements Feature< PlaygroundSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F1, PlaygroundSpot >
		{
			public Spec()
			{
				super(
						"F1",
						"Dummy feature F1",
						F1.class,
						PlaygroundSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< PlaygroundSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjectionKey > projectionKeys()
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< PlaygroundSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	public static class F2 implements Feature< PlaygroundSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F2, PlaygroundSpot >
		{
			public Spec()
			{
				super(
						"F2",
						"Dummy feature F2",
						F2.class,
						PlaygroundSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< PlaygroundSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjectionKey > projectionKeys()
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< PlaygroundSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	public static class F3 implements Feature< PlaygroundSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F3, PlaygroundSpot >
		{
			public Spec()
			{
				super(
						"F3",
						"Dummy feature F3",
						F3.class,
						PlaygroundSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< PlaygroundSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjectionKey > projectionKeys()
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< PlaygroundSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	public static class F4 implements Feature< PlaygroundSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F4, PlaygroundSpot >
		{
			public Spec()
			{
				super(
						"F4",
						"Dummy feature F4",
						F4.class,
						PlaygroundSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< PlaygroundSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjectionKey > projectionKeys()
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< PlaygroundSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	@Plugin( type = FC1.class )
	public static class FC1 implements FeatureComputer
	{
		@Parameter( type = OUTPUT )
		private F1 f1;

		@Override
		public void run()
		{}

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
		{}

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
		{}

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
		{}

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
		final DefaultFeatureComputerService featureComputerService = new DefaultFeatureComputerService();
		context.inject( featureComputerService );
		featureComputerService.initialize();
		featureComputerService.compute( F1.SPEC, F3.SPEC );

		testSpecs( context );
	}

	private static void testSpecs( final Context context )
	{
		final FeatureSpecsService specs = new FeatureSpecsService();
		context.inject( specs );
		specs.initialize();
		System.out.println( "specs.getSpecs( PlaygroundSpot.class ) = " + specs.getSpecs( PlaygroundSpot.class ) );
		System.out.println( "specs.getSpecs( PlaygroundLink.class ) = " + specs.getSpecs( PlaygroundLink.class ) );
		System.out.println( "specs.getSpec( \"F1\" ) = " + specs.getSpec( "F1" ) );
		System.out.println( "specs.getSpec( \"F2\" ) = " + specs.getSpec( "F2" ) );
		System.out.println( "specs.getSpec( \"F3\" ) = " + specs.getSpec( "F3" ) );
	}
}
