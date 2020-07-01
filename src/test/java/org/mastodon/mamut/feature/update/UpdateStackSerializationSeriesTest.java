package org.mastodon.mamut.feature.update;

import bdv.spimdata.SpimDataMinimal;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.SpotNLinksFeature;
import org.mastodon.mamut.feature.update.UpdateStackSerializationTest.FT4;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.util.DummySpimData;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mastodon.mamut.feature.update.UpdateStackSerializationTest.loadProject;
import static org.mastodon.mamut.feature.update.UpdateStackSerializationTest.saveProject;

/**
 * JUnit test for incremental feature state de/serialization.
 *
 * This test builds on UpdateStackSerializationTest but piles up several events of de/serializations.
 *
 * <pre>
A:
- Create a Mastodon project with 10 spots.
- Compute a feature named FT4 for it.
- Modifies the position of one spot.
- Save the project.
B:
- Reopen the project.
- Modify another spot and recompute the SpotNLinks feature (NOT FT4).
C: Does B several times with different spots.
D:
- Reopen the project.
- Recompute the feature FT4, checking that:
	- we recompute exactly the number of spots we modified since last FT4 computation.
	- and check their neighbors too.
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
public class UpdateStackSerializationSeriesTest
{

	private static final File SAVED_PROJECT = new File( "./featureserialized.mastodon" );

	@Test
	public void test() throws Exception
	{
		createProjectWithPendingChanges( 4 );
		makeOtherChangesAndRecompute( 1 );
		makeOtherChangesAndRecompute( 2 );
		makeOtherChangesAndRecompute( 3 );
		makeOtherChangesAndRecompute( 5 );
		openProjectWithPendingChanges( new int[] { 1, 2, 3, 4, 5 } );
		openProjectWithoutPendingChanges();
		deleteProject();
	}

	private void createProjectWithPendingChanges( final int id ) throws Exception
	{
		final String bdvFile = "x=10 y=10 z=10 sx=1 sy=1 sz=1 t=10.dummy";
		final SpimDataMinimal spimData = DummySpimData.tryCreate( bdvFile );
		final MamutProject originalProject = new MamutProject( SAVED_PROJECT, new File( bdvFile ) );

		final Model model = new Model();

		final Context context = new Context();
		final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		final ModelGraph graph = model.getGraph();
		final Spot vref1 = graph.vertexRef();
		final Spot vref2 = graph.vertexRef();
		final Link eref = graph.edgeRef();

		final Random ran = new Random( 1l );
		final double[] pos = new double[] { 10 * ran.nextDouble(), 10 * ran.nextDouble(), 10 * ran.nextDouble() };
		final Spot source = graph.addVertex( vref1 ).init( 0, pos, ran.nextDouble() );

		final int numTimepoints = spimData.getSequenceDescription().getTimePoints().size();
		for ( int t = 1; t < numTimepoints; t++ )
		{
			pos[ 0 ] = 10 * ran.nextDouble();
			pos[ 1 ] = 10 * ran.nextDouble();
			pos[ 2 ] = 10 * ran.nextDouble();
			final Spot target = graph.addVertex( vref2 ).init( t, pos, ran.nextDouble() );
			graph.addEdge( source, target, eref ).init();
			source.refTo( target );
		}

		// Compute all for FT4, that simply stores the X position of spots.
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = computerService.compute( FT4.SPEC );
		final FeatureModel featureModel = model.getFeatureModel();
		features.values().forEach( featureModel::declareFeature );

		// Get one spot in particular.
		final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( id ).iterator().next();

		// Make one change.
		s0.move( 10., 0 );

		// Save project.
		saveProject( context, originalProject, model );
	}

	private void makeOtherChangesAndRecompute( final int id ) throws IOException
	{
		// Open serialized project.
		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( SAVED_PROJECT.getAbsolutePath() );
		final Model model = new Model();
		loadProject( context, project, model );

		final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		// Modify one spot in particular.
		final FeatureModel featureModel = model.getFeatureModel();
		final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( id ).iterator().next();

		// Make one change.
		s0.move( 10., 0 );

		// Compute feature but NOT FT4
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = computerService.compute( SpotNLinksFeature.SPEC );
		features.values().forEach( featureModel::declareFeature );

		// Save project.
		saveProject( context, project, model );
	}

	private void openProjectWithPendingChanges( final int[] ids ) throws IOException
	{
		// Open serialized project.
		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( SAVED_PROJECT.getAbsolutePath() );
		final Model model = new Model();
		loadProject( context, project, model );

		final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();
		final FT4 ft = ( FT4 ) featureModel.getFeature( FT4.SPEC );
		for ( final int id : ids )
		{
			final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( id ).iterator().next();
			final double x0 = ft.map.getDouble( s0 );
			assertNotEquals( "The feature value for FT4 should not be equal to the X position of the spot we moved, "
					+ "because we did not yet recompute the feature value.", s0.getDoublePosition( 0 ), x0, 1e-9 );
		}

		/*
		 * Trigger recalculation of FT4.
		 */

		/*
		 * For this one, we expect that only the collection of spots we moved to be recalculated.
		 */

		ft.expectedVerticesSelf = new RefSetImp<>( graph.vertices().getRefPool() );
		ft.expectedEdgesNeighbor = new RefSetImp<>( graph.edges().getRefPool() );
		ft.expectedVerticesNeighbor = new RefSetImp<>( graph.vertices().getRefPool() );
		ft.expectedEdgesSelf = new RefSetImp<>( graph.edges().getRefPool() );
		// Spots we moved.
		for ( final int id : ids )
		{
			final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( id ).iterator().next();
			ft.expectedEdgesNeighbor.add( s0.incomingEdges().get( 0 ) );
			ft.expectedEdgesNeighbor.add( s0.outgoingEdges().get( 0 ) );
			ft.expectedVerticesSelf.add( s0 );
		}
		// Its neighbor links.

		computerService.compute( FT4.SPEC );

		for ( final int id : ids )
		{
			final Spot s0 = model.getSpatioTemporalIndex().getSpatialIndex( id ).iterator().next();
			final double x0b = ft.map.getDouble( s0 );
			assertEquals( "Now that we recomputed the feature value, it should be equal to the spot X position.", x0b, s0.getDoublePosition( 0 ), 1e-9 );
		}

		// Save the project again.
		saveProject( context, project, model );
	}

	private void openProjectWithoutPendingChanges() throws IOException
	{
		// Open serialized project.
		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( SAVED_PROJECT.getAbsolutePath() );
		final Model model = new Model();
		loadProject( context, project, model );

		final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setModel( model );

		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final FT4 ft = ( FT4 ) featureModel.getFeature( FT4.SPEC );

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
}
