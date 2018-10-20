package org.mastodon.revised.mamut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.swing.JDialog;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.feature.FeatureComputerService;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.FeatureComputationController;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;
import org.scijava.Context;
import org.scijava.service.AbstractService;

public class MamutFeatureComputation
{

	public static final JDialog getDialog( final MamutAppModel appModel, final Context context )
	{
		// Prepare services.
		final FeatureSpecsService specsService = context.getService( FeatureSpecsService.class );
		final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setModel( appModel.getModel() );
		computerService.setSharedBdvData( appModel.getSharedBdvData() );
		final MyFeatureComputerService myComputerService = new MyFeatureComputerService( computerService, appModel.getModel().getFeatureModel() );

		// Controller.
		final Collection< Class< ? > > targets = Arrays.asList( new Class[] { Spot.class, Link.class } );
		final FeatureComputationController controller = new FeatureComputationController( specsService, myComputerService, targets );
		computerService.setPropertyChangeListener( controller.getPropertyChangeListener() );

		// Listen to model changes and echo in the GUI
		final ModelGraph graph = appModel.getModel().getGraph();
		graph.addGraphChangeListener( controller );
		// Listen to changes in spot properties.
		final SpotPool spotPool = ( SpotPool ) graph.vertices().getRefPool();
		spotPool.covarianceProperty().addPropertyChangeListener( ( o ) -> controller.graphChanged() );
		spotPool.positionProperty().addPropertyChangeListener( ( o ) -> controller.graphChanged() );

		return controller.getDialog();
	}

	private static final class MyFeatureComputerService extends AbstractService implements FeatureComputerService
	{

		private final FeatureComputerService wrapped;

		private final FeatureModel featureModel;

		public MyFeatureComputerService( final FeatureComputerService wrapped, final FeatureModel featureModel )
		{
			this.wrapped = wrapped;
			this.featureModel = featureModel;
		}

		@Override
		public boolean isCanceled()
		{
			return wrapped.isCanceled();
		}

		@Override
		public void cancel( final String reason )
		{
			wrapped.cancel( reason );
		}

		@Override
		public String getCancelReason()
		{
			return wrapped.getCancelReason();
		}

		@Override
		public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final Collection< String > featureKeys )
		{
			final Map< FeatureSpec< ?, ? >, Feature< ? > > map = wrapped.compute( featureKeys );
			if ( wrapped.isCanceled() )
				return null;

			featureModel.pauseListeners();
			// Clear feature we can compute
			final Collection< FeatureSpec< ?, ? > > featureSpecs = featureModel.getFeatureSpecs();
			final Collection< FeatureSpec< ?, ? > > toClear = new ArrayList<>();
			for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
				if ( null != wrapped.getFeatureComputerFor( featureSpec ) )
					toClear.add( featureSpec );

			for ( final FeatureSpec< ?, ? > featureSpec : toClear )
					featureModel.clear( featureSpec );

			// Pass the feature map to the feature model.
			for ( final FeatureSpec< ?, ? > featureSpec : map.keySet() )
				featureModel.declareFeature( featureSpec, map.get( featureSpec ) );

			featureModel.resumeListeners();
			return map;
		}

		@Override
		public FeatureComputer getFeatureComputerFor( final FeatureSpec< ?, ? > spec )
		{
			return wrapped.getFeatureComputerFor( spec );
		}
	}
}
