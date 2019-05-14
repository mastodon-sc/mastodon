package org.mastodon.feature.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.feature.update.GraphUpdateStackTest.TestFeature;
import org.mastodon.feature.update.GraphUpdateStackTest.TestFeatureComputer;
import org.mastodon.feature.update.UpdateStackSerializationTest.FT4.Spec;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import mpicbg.spim.data.SpimDataException;

/**
 * JUnit test for incremental feature state de/serialization.
 *
 * This test does the following:
 *
 * <pre>
A:
- Create a Mastodon project with 10 spots.
- Compute a feature named FT4 for it.
- Modifies the position of one spot.
- Save the project.
B:
- Reopen the project.
- Recompute the feature FT4, checking that:
	- we recompute exactly 1 spot only.
	- and have been notified that its 2 neighbor links are
in the update too.
- Check that the new feature value for the modified spot is correct.
- Resave the project.
C:
- Reopen the project.
- Recompute the feature FT4, checking that:
	- we have nothing to recompute because the feature was in
sync when we saved.
D:
- Delete the project.
 * </pre>
 *
 *
 * @author Jean-Yves Tinevez
 *
 */
public class UpdateStackSerializationTest
{

	private static final File SAVED_PROJECT = new File( "./featureserialized.mastodon" );

	@Test
	public void test() throws Exception
	{
		createProjectWithPendingChanges();
		openProjectWithPendingChanges();
		openProjectWithoutPendingChanges();
		deleteProject();
	}

	private void createProjectWithPendingChanges() throws Exception
	{
		final String bdvFile = "x=10 y=10 z=10 sx=1 sy=1 sz=1 t=10.dummy";
		final MamutProject originalProject = new MamutProject( null, new File( bdvFile ) );

		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( originalProject );

		final Model model = windowManager.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();

		final Spot vref1 = graph.vertexRef();
		final Spot vref2 = graph.vertexRef();
		final Link eref = graph.edgeRef();

		final Random ran = new Random( 1l );
		final double[] pos = new double[] { 10 * ran.nextDouble(), 10 * ran.nextDouble(), 10 * ran.nextDouble() };
		final Spot source = graph.addVertex( vref1 ).init( 0, pos, ran.nextDouble() );

		final int numTimepoints = windowManager.getAppModel().getSharedBdvData().getNumTimepoints();
		for ( int t = 1; t < numTimepoints; t++ )
		{
			pos[ 0 ] = 10 * ran.nextDouble();
			pos[ 1 ] = 10 * ran.nextDouble();
			pos[ 2 ] = 10 * ran.nextDouble();
			final Spot target = graph.addVertex( vref2 ).init( t, pos, ran.nextDouble() );
			graph.addEdge( source, target, eref ).init();
			source.refTo( target );
		}

		final MamutFeatureComputerService computerService = windowManager.getContext().getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		// Get one spot in particular.
		final FeatureModel featureModel = model.getFeatureModel();
		final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( 4 ).iterator().next();

		// Compute all for FT4, that simply stores the X position of spots.
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = computerService.compute( FT4.SPEC );
		features.values().forEach( featureModel::declareFeature );

		// Make one change.
		s0.move( 10., 0 );

		// Save project.
		windowManager.getProjectManager().saveProject( SAVED_PROJECT );
	}

	private void openProjectWithPendingChanges() throws IOException, SpimDataException
	{
		// Open serialized project.
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( new MamutProjectIO().load( SAVED_PROJECT.getAbsolutePath() ) );

		final Model model = windowManager.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();
		final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( 4 ).iterator().next();

		final FT4 ft = ( FT4 ) featureModel.getFeature( FT4.SPEC );
		final double x0 = ft.map.getDouble( s0 );

		assertNotEquals( "The feature value for FT4 should not be equal to the X position of the spot we moved, "
				+ "because we did not yet recompute the feature value.", s0.getDoublePosition( 0 ), x0, 1e-9 );

		/*
		 * Trigger recalculation of FT4.
		 */

		final MamutFeatureComputerService computerService = windowManager.getContext().getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		/*
		 * For this one, we expect that only the spot s0 will be recalculated.
		 */

		ft.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		// Spot we moved.
		ft.expectedVerticesSelf.add( s0 );
		ft.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		ft.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );
		// Its neighbor links.
		ft.expectedEdgesNeighbor.add( s0.incomingEdges().get( 0 ) );
		ft.expectedEdgesNeighbor.add( s0.outgoingEdges().get( 0 ) );

		computerService.compute( FT4.SPEC );

		final double x0b = ft.map.getDouble( s0 );
		assertEquals( "Now that we recomputed the feature value, it should be equal to the spot X position.", x0b, s0.getDoublePosition( 0 ), 1e-9 );

		// Save the project again.
		windowManager.getProjectManager().saveProject( SAVED_PROJECT );
	}

	private void openProjectWithoutPendingChanges() throws IOException, SpimDataException
	{
		// Open serialized project.
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( new MamutProjectIO().load( SAVED_PROJECT.getAbsolutePath() ) );

		final Model model = windowManager.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final FT4 ft = ( FT4 ) featureModel.getFeature( FT4.SPEC );

		final MamutFeatureComputerService computerService = windowManager.getContext().getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		/*
		 * For this one, we expect now nothing to be recalculated.
		 */

		ft.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		ft.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		ft.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );

		computerService.compute( FT4.SPEC );
	}

	private void deleteProject()
	{
		// Cleanup.
		SAVED_PROJECT.delete();
	}

	public static class FT4 extends TestFeature< Spot >
	{

		public final static String KEY = "FT4";

		public final static Spec SPEC = new Spec();

		DoublePropertyMap< Spot > map;

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< FT4, Spot >
		{
			public Spec()
			{
				super(
						KEY,
						"Dummy feature FT4",
						FT4.class,
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

	@Plugin( type = FT4computer.class )
	public static class FT4computer extends TestFeatureComputer
	{

		@Parameter
		private Model model;

		@Parameter( type = ItemIO.OUTPUT )
		private FT4 output;

		@Override
		public void createOutput()
		{
			output = ( FT4 ) model.getFeatureModel().getFeature( FT4.SPEC );
			if ( null == output )
			{
				output = new FT4();
				output.map = new DoublePropertyMap<>( model.getGraph().vertices(), Double.NaN );
			}
		}

		@Override
		protected FeatureSpec< ?, ? > getKey()
		{
			return FT4.SPEC;
		}

		@Override
		protected TestFeature< Spot > getOutput()
		{
			return output;
		}

		@Override
		public void run()
		{
			final Update< Spot > changes = spotUpdateStack.changesFor( FT4.SPEC );
			final Iterable< Spot > vertices = ( null == changes )
					? model.getGraph().vertices()
					: changes.get();

			// Fill X position.
			for ( final Spot s : vertices )
				output.map.set( s, s.getDoublePosition( 0 ) );
			super.run();
		}
	}

	@Plugin( type = FeatureSerializer.class )
	public static class FT4serializer implements FeatureSerializer< FT4, Spot >
	{

		@Override
		public Spec getFeatureSpec()
		{
			return FT4.SPEC;
		}

		@Override
		public void serialize( final FT4 feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos ) throws IOException
		{
			new DoublePropertyMapSerializer<>( feature.map ).writePropertyMap( idmap, oos );
		}

		@Override
		public FT4 deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
		{
			final FT4 ft4 = new FT4();
			ft4.map = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( ft4.map ).readPropertyMap( idmap, ois );
			return ft4;
		}
	}
}
