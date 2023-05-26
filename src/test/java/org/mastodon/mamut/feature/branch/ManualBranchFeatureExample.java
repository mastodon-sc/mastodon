package org.mastodon.mamut.feature.branch;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;
import org.scijava.plugin.Plugin;

import mpicbg.spim.data.SpimDataException;

public class ManualBranchFeatureExample
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		try
		{
			// Load project.
			final WindowManager windowManager = new WindowManager( new Context() );
			final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.mastodon" );
			windowManager.getProjectManager().open( project );
			final Model model = windowManager.getAppModel().getModel();

			// Instantiate feature.
			final MyManualFeature f = new MyManualFeature( model );

			// Compute and store some values.
			double i = 1.;
			for ( final BranchSpot b : model.getBranchGraph().vertices() )
				f.set( b, 1.45 * i++ );

			// Declare this feature.
			final FeatureModel featureModel = model.getFeatureModel();
			featureModel.declareFeature( f );

			// Play with it.
			new MainWindow( windowManager ).setVisible( true );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	public static class MyManualFeature extends BranchScalarFeature< BranchSpot, Spot >
	{

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< MyManualFeature, BranchSpot >
		{
			public Spec()
			{
				super(
						KEY,
						INFO_STRING,
						MyManualFeature.class,
						BranchSpot.class,
						Multiplicity.SINGLE,
						PROJECTION_SPEC );
			}
		}

		public static final String INFO_STRING = "Testing a manual branch spot feature";

		private static final String KEY = "Test manual branch feature";

		private static final Dimension DIMENSION = Dimension.LENGTH;

		public static final FeatureProjectionSpec PROJECTION_SPEC =
				new FeatureProjectionSpec( KEY, DIMENSION );

		private static final Spec specs = new Spec();

		public MyManualFeature( final Model model )
		{
			super( KEY, DIMENSION, DIMENSION.getUnits( model.getSpaceUnits(), model.getTimeUnits() ),
					model.getBranchGraph(), model.getGraph().vertices().getRefPool() );
		}

		@Override
		public Spec getSpec()
		{
			return specs;
		}
	}

	public static abstract class BranchScalarFeature< BV extends Vertex< ? >, V extends Vertex< ? > > implements Feature< BV >
	{

		private final FeatureProjectionKey key;

		private final DoublePropertyMap< V > values;

		private final String units;

		private final BranchGraph< BV, ?, V, ? > branchGraph;

		private final RefPool< V > pool;

		private final V ref;

		public BranchScalarFeature(
				final String key,
				final Dimension dimension,
				final String units,
				final BranchGraph< BV, ?, V, ? > branchGraph,
				final RefPool< V > pool )
		{
			final FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( key, dimension );
			this.key = FeatureProjectionKey.key( projectionSpec );
			this.units = units;
			this.branchGraph = branchGraph;
			this.pool = pool;
			this.values = new DoublePropertyMap<>( pool, Double.NaN );
			this.ref = pool.createRef();
		}

		@Override
		public abstract FeatureSpec< ? extends Feature< BV >, BV > getSpec();

		private V toVertex( final BV bv )
		{
			return branchGraph.getLastLinkedVertex( bv, ref );
		}
		
		public boolean isSet( final BV bv )
		{
			return values.isSet( toVertex( bv ) );
		}

		public double value( final BV bv )
		{
			return values.getDouble( toVertex( bv ) );
		}

		public void set( final BV bv, final double value )
		{
			values.set( toVertex( bv ), value );
		}

		@Override
		public void invalidate( final BV bv )
		{
			values.remove( toVertex( bv ) );
		}

		@Override
		public FeatureProjection< BV > project( final FeatureProjectionKey key )
		{
			if ( this.key.equals( key ) )
				return new BranchAdaptingFeatureProjection( pool.createRef() );

			return null;
		}

		@Override
		public Set< FeatureProjection< BV > > projections()
		{
			return Collections.singleton( new BranchAdaptingFeatureProjection( pool.createRef() ) );
		}

		public class BranchAdaptingFeatureProjection implements FeatureProjection< BV >
		{

			private final V ref;

			public BranchAdaptingFeatureProjection( final V ref )
			{
				this.ref = ref;
			}

			private V toVertex( final BV bv )
			{
				return branchGraph.getLastLinkedVertex( bv, ref );
			}

			public void set( final BV bv, final double value )
			{
				values.set( toVertex( bv ), value );
			}

			public void remove( final BV bv )
			{
				values.remove( toVertex( bv ) );
			}

			@Override
			public boolean isSet( final BV bv )
			{
				return values.isSet( toVertex( bv ) );
			}

			@Override
			public double value( final BV bv )
			{
				return values.getDouble( toVertex( bv ) );
			}

			@Override
			public FeatureProjectionKey getKey()
			{
				return key;
			}

			@Override
			public String units()
			{
				return units;
			}
		}
	}
}
