package org.mastodon.feature.update;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.update.GraphUpdate.UpdateLocality;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

public class GraphUpdateStackTest
{

	private ModelGraph graph;

	private Spot s0;

	private Spot s1;

	private Spot s2;

	private Spot s3;

	private Spot s4;

	private Link e01;

	private Link e12;

	private Link e23;

	private Link e34;

	private MamutFeatureComputerService computerService;

	@Before
	public void setUp()
	{
		final Context context = new Context( MamutFeatureComputerService.class, FeatureSpecsService.class );

		final Model model = new Model();
		graph = model.getGraph();
		s0 = graph.addVertex().init( 0, new double[] { 0., 0., 0. }, 5. );
		s1 = graph.addVertex().init( 1, new double[] { 0., 0., 0. }, 5. );
		s2 = graph.addVertex().init( 2, new double[] { 0., 0., 0. }, 5. );
		s3 = graph.addVertex().init( 3, new double[] { 0., 0., 0. }, 5. );
		s4 = graph.addVertex().init( 4, new double[] { 0., 0., 0. }, 5. );
		e01 = graph.addEdge( s0, s1 );
		e12 = graph.addEdge( s1, s2 );
		e23 = graph.addEdge( s2, s3 );
		e34 = graph.addEdge( s3, s4 );

		computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setModel( model );
	}

	@Test
	public void testManyUpdates()
	{
		/*
		 * Test that the many updates will be 'forgotten' at some point, because
		 * we use a fixed-max size stack to store changes.
		 */

		// Compute all for FT1.
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = computerService.compute( FT1.SPEC );

		// Grab FT1.
		final FT1 ft1 = ( FT1 ) features.get( FT1.SPEC );

		// Make one change.
		s0.move( 10., 0 );

		/*
		 * Do plenty of computation, but without FT1. Up to 10, we should catch
		 * up.
		 */
		for ( int i = 0; i < GraphUpdateStack.BUFFER_SIZE - 1; i++ )
			computerService.compute();

		ft1.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		ft1.expectedVerticesSelf.add( s0 );
		ft1.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft1.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		ft1.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );
		ft1.expectedEdgesNeighbor.add( e01 );
		computerService.compute( FT1.SPEC );

		// Redo one change.
		s1.move( 10., 0 );

		/*
		 * Do plenty of computation, but without FT1. More than 10, we should
		 * not catch up.
		 */
		for ( int i = 0; i < GraphUpdateStack.BUFFER_SIZE; i++ )
			computerService.compute();

		ft1.expectedVerticesSelf = null;
		ft1.expectedVerticesNeighbor = null;
		ft1.expectedEdgesSelf = null;
		ft1.expectedEdgesNeighbor = null;
		computerService.compute( FT1.SPEC );
	}

	@Test
	public void testUpdates()
	{
		/*
		 * First calculation for FT1 and FT2. They should receive the null flag
		 * stating that all values are to be recomputed.
		 */

		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = computerService.compute( FT1.SPEC, FT2.SPEC );
		final FT1 ft1 = ( FT1 ) features.get( FT1.SPEC );
		final FT2 ft2 = ( FT2 ) features.get( FT2.SPEC );

		/*
		 * Second calculation for FT1 and FT2. They should receive empty changes
		 * since we did not change the model.
		 */
		ft1.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		ft1.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft1.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		ft1.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );
		ft2.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		ft2.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft2.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		ft2.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );
		computerService.compute( FT1.SPEC, FT2.SPEC );

		/*
		 * Third calculation. We move one spot. We only recompute FT1.
		 */

		s0.move( 10., 0 );
		ft1.expectedVerticesSelf.add( s0 );
		ft1.expectedEdgesNeighbor.add( e01 );
		computerService.compute( FT1.SPEC );

		/*
		 * Fourth calculation. We move another spot. We compute FT1 and FT2. FT2
		 * should receive the changes for two spots.
		 */

		s1.move( 20., 0 );
		ft1.expectedVerticesSelf.remove( s0 ); // Up to date now.
		ft1.expectedVerticesSelf.add( s1 );
		ft1.expectedEdgesNeighbor.add( e12 );
		ft2.expectedVerticesSelf.add( s0 );
		ft2.expectedVerticesSelf.add( s1 );
		ft2.expectedEdgesNeighbor.add( e01 );
		ft2.expectedEdgesNeighbor.add( e12 );
		computerService.compute( FT1.SPEC, FT2.SPEC );

		/*
		 * Fifth calculation. We now compute FT3. It should receive null
		 * changes, triggering full re-computation.
		 */

		final Map< FeatureSpec< ?, ? >, Feature< ? > > features2 = computerService.compute( FT3.SPEC );
		final FT3 ft3 = ( FT3 ) features2.get( FT3.SPEC );

		/*
		 * Sixth & seventh calculations. We make two changes but only catch up
		 * after having removed a spot marked for changes. So it should be gone
		 * from the changes and its edges too. But because the edges of s1 are
		 * gone, their vertices will be marked for changes in NEIGHBOR.
		 */

		s1.move( 10., 0 );
		computerService.compute();

		// Test that FT1 has a value for s1 for now.
		assertTrue( "FT1 should have a value for s1.", ft1.map.isSet( s1 ) );

		graph.remove( s1 );
		ft3.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		ft3.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft3.expectedVerticesNeighbor.add( s0 );
		ft3.expectedVerticesNeighbor.add( s2 );
		ft3.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		ft3.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );
		computerService.compute( FT3.SPEC );

		// Test that FT1 does not have a value for s1.
		assertFalse( "FT1 should not have a value for s1 anymore.", ft1.map.isSet( s1 ) );

		/*
		 * Eighth and ninth calculations. We mark two spots as modified in
		 * NEIGHBOR, then one of them for SELF. The later should not be in the
		 * NEIGHBOR collection anymore.
		 */

		// Will mark s3 and s4 as modified in NEIGHBOR.
		graph.remove( e34 );

		computerService.compute();

		// Will send s3 to the SELF modified collection.
		s3.move( 10., 0 );

		ft3.expectedVerticesSelf.add( s3 );
		ft3.expectedVerticesNeighbor.clear();
		ft3.expectedVerticesNeighbor.add( s4 );
		// Since we moved s3, e23 should be in NEIGHBOR.
		ft3.expectedEdgesNeighbor.add( e23 );
		computerService.compute( FT3.SPEC );
	}

	public static class FT1 extends TestFeature< Spot >
	{

		public final static String KEY = "FT1";

		public final static Spec SPEC = new Spec();

		private DoublePropertyMap< Spot > map;

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< FT1, Spot >
		{
			public Spec()
			{
				super(
						KEY,
						"Dummy feature FT1",
						FT1.class,
						Spot.class,
						Multiplicity.SINGLE );
			}
		}

		@Override
		public FeatureProjection< Spot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< Spot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	@Plugin( type = FT1computer.class )
	public static class FT1computer extends TestFeatureComputer
	{

		@Parameter
		private ModelGraph graph;

		@Parameter( type = ItemIO.OUTPUT )
		private FT1 output;

		@Override
		public void createOutput()
		{
			if ( null == output )
			{
				output = new FT1();
				output.map = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
			}
		}

		@Override
		protected FeatureSpec< ?, ? > getKey()
		{
			return FT1.SPEC;
		}

		@Override
		protected TestFeature< Spot > getOutput()
		{
			return output;
		}

		@Override
		public void run()
		{
			// Fill with stuff.
			for ( final Spot s : graph.vertices() )
				output.map.set( s, 1. );
			super.run();
		}
	}

	public static class FT2 extends TestFeature< Spot >
	{
		public final static String KEY = "FT2";

		public final static Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< FT2, Spot >
		{
			public Spec()
			{
				super(
						KEY,
						"Dummy feature FT2",
						FT2.class,
						Spot.class,
						Multiplicity.SINGLE );
			}
		}

		public DoublePropertyMap< Spot > map;

		@Override
		public FeatureProjection< Spot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< Spot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	@Plugin( type = FT2computer.class )
	public static class FT2computer extends TestFeatureComputer
	{

		@Parameter
		private ModelGraph graph;

		@Parameter( type = ItemIO.OUTPUT )
		private FT2 output;

		@Override
		public void createOutput()
		{
			if ( null == output )
			{
				output = new FT2();
				output.map = new DoublePropertyMap<>( graph.vertices().getRefPool(), Double.NaN );
			}
		}

		@Override
		protected FeatureSpec< ?, ? > getKey()
		{
			return FT2.SPEC;
		}

		@Override
		protected TestFeature< Spot > getOutput()
		{
			return output;
		}
	}

	public static class FT3 extends TestFeature< Spot >
	{

		public final static String KEY = "FT3";

		public final static Spec SPEC = new Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< FT3, Spot >
		{
			public Spec()
			{
				super(
						KEY,
						"Dummy feature FT3",
						FT3.class,
						Spot.class,
						Multiplicity.SINGLE );
			}
		}

		public DoublePropertyMap< Spot > map;

		@Override
		public FeatureProjection< Spot > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< Spot > > projections()
		{
			return null;
		}

		@Override
		public Spec getSpec()
		{
			return SPEC;
		}
	}

	@Plugin( type = FT3computer.class )
	public static class FT3computer extends TestFeatureComputer
	{

		@Parameter
		private ModelGraph graph;

		@Parameter( type = ItemIO.OUTPUT )
		private FT3 output;

		@Override
		public void createOutput()
		{
			if ( null == output )
			{
				output = new FT3();
				output.map = new DoublePropertyMap<>( graph.vertices().getRefPool(), Double.NaN );
			}
		}

		@Override
		protected FeatureSpec< ?, ? > getKey()
		{
			return FT3.SPEC;
		}

		@Override
		protected TestFeature< Spot > getOutput()
		{
			return output;
		}
	}

	private static abstract class TestFeatureComputer implements MamutFeatureComputer
	{
		@Parameter
		protected GraphUpdateStack< Spot, Link > graphUpdateStack;

		@Override
		public void run()
		{
			final TestFeature< Spot > output = getOutput();
			final GraphUpdate< Spot, Link > changes = graphUpdateStack.changesFor( getKey() );

			if ( null == output.expectedVerticesSelf )
				assertNull( "Graph update for " + getKey() + " should be null.", changes );
			else
				assertThat( "Unexpected SELF vertices: ", output.expectedVerticesSelf,
						IsIterableContainingInAnyOrder.containsInAnyOrder( changes.vertices( UpdateLocality.SELF ).toArray() ) );

			if ( null == output.expectedVerticesNeighbor )
				assertNull( "Graph update for " + getKey() + " should be null.", changes );
			else
				assertThat( "Unexpected NEIGHBOR vertices: ", output.expectedVerticesNeighbor,
						IsIterableContainingInAnyOrder.containsInAnyOrder( changes.vertices( UpdateLocality.NEIGHBOR ).toArray() ) );

			if ( null == output.expectedEdgesSelf )
				assertNull( "Graph update for " + getKey() + " should be null.", changes );
			else
				assertThat( "Unexpected SELF edges: ", output.expectedEdgesSelf,
						IsIterableContainingInAnyOrder.containsInAnyOrder( changes.edges( UpdateLocality.SELF ).toArray() ) );

			if ( null == output.expectedEdgesNeighbor )
				assertNull( "Graph update for " + getKey() + " should be null.", changes );
			else
				assertThat( "Unexpected NEIGHBOR edges: ", output.expectedEdgesNeighbor,
						IsIterableContainingInAnyOrder.containsInAnyOrder( changes.edges( UpdateLocality.NEIGHBOR ).toArray() ) );

		}

		protected abstract FeatureSpec< ?, ? > getKey();

		protected abstract TestFeature< Spot > getOutput();
	}

	private static abstract class TestFeature< O > implements Feature< O >
	{
		protected RefSet< Spot > expectedVerticesSelf = null;

		protected RefSet< Spot > expectedVerticesNeighbor = null;

		protected RefSet< Link > expectedEdgesSelf = null;

		protected RefSet< Link > expectedEdgesNeighbor = null;
	}
}
