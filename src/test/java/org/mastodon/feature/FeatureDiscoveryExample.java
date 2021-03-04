/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.feature;

import static org.scijava.ItemIO.OUTPUT;

import java.util.Set;

import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

public class FeatureDiscoveryExample
{
	public static class ExampleSpot {};

	public static class ExampleLink {};

	public static class F1 implements Feature< ExampleSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F1, ExampleSpot >
		{
			public Spec()
			{
				super(
						"F1",
						"Dummy feature F1",
						F1.class,
						ExampleSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< ExampleSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< ExampleSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}

		@Override
		public void invalidate( final ExampleSpot obj )
		{}
	}

	public static class F2 implements Feature< ExampleSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F2, ExampleSpot >
		{
			public Spec()
			{
				super(
						"F2",
						"Dummy feature F2",
						F2.class,
						ExampleSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< ExampleSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< ExampleSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}

		@Override
		public void invalidate( final ExampleSpot obj )
		{}
	}

	public static class F3 implements Feature< ExampleSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F3, ExampleSpot >
		{
			public Spec()
			{
				super(
						"F3",
						"Dummy feature F3",
						F3.class,
						ExampleSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< ExampleSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< ExampleSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}

		@Override
		public void invalidate( final ExampleSpot obj )
		{}
	}

	public static class F4 implements Feature< ExampleSpot >
	{
		public static final Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< F4, ExampleSpot >
		{
			public Spec()
			{
				super(
						"F4",
						"Dummy feature F4",
						F4.class,
						ExampleSpot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< ExampleSpot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< ExampleSpot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}

		@Override
		public void invalidate( final ExampleSpot obj )
		{}
	}

	@Plugin( type = FeatureComputer.class )
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

	@Plugin( type = FeatureComputer.class )
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

	@Plugin( type = FeatureComputer.class )
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

	@Plugin( type = FeatureComputer.class )
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
		System.out.println( "specs.getSpecs( ExampleSpot.class ) = " + specs.getSpecs( ExampleSpot.class ) );
		System.out.println( "specs.getSpecs( ExampleLink.class ) = " + specs.getSpecs( ExampleLink.class ) );
		System.out.println( "specs.getSpec( \"F1\" ) = " + specs.getSpec( "F1" ) );
		System.out.println( "specs.getSpec( \"F2\" ) = " + specs.getSpec( "F2" ) );
		System.out.println( "specs.getSpec( \"F3\" ) = " + specs.getSpec( "F3" ) );
	}
}
