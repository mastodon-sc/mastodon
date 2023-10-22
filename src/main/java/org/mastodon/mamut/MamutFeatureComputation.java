/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.feature.FeatureComputerService;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.ui.FeatureComputationController;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.SpotPool;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;
import org.scijava.service.AbstractService;

public class MamutFeatureComputation
{

	public static final JDialog getDialog( final ProjectModel appModel, final Context context )
	{
		// Prepare services.
		final MamutFeatureComputerService computerService = MamutFeatureComputerService.newInstance( context );
		computerService.setModel( appModel.getModel() );
		computerService.setSharedBdvData( appModel.getSharedBdvData() );
		final MyFeatureComputerService myComputerService =
				new MyFeatureComputerService( computerService, appModel.getModel().getFeatureModel() );

		// Controller.
		final Collection< Class< ? > > targets =
				Arrays.asList( Spot.class, Link.class, BranchSpot.class, BranchLink.class );
		final MamutFeatureComputationController controller =
				new MamutFeatureComputationController( myComputerService, targets, appModel.getBranchGraphSync() );
		computerService.computationStatusListeners().add( controller.getComputationStatusListener() );

		// Listen to model changes and echo in the GUI
		final ModelGraph graph = appModel.getModel().getGraph();
		graph.addGraphChangeListener( controller );
		// Listen to changes in spot properties.
		final SpotPool spotPool = ( SpotPool ) graph.vertices().getRefPool();
		spotPool.covarianceProperty().propertyChangeListeners().add( ( o ) -> controller.graphChanged() );
		spotPool.positionProperty().propertyChangeListeners().add( ( o ) -> controller.graphChanged() );

		return controller.getDialog();
	}

	/**
	 * A {@link FeatureComputationController} that regenerate the branch-graph
	 * prior to feature computation if the branch-graph is out of sync.
	 * 
	 * @author Jean-Yves Tinevez
	 */
	private static final class MamutFeatureComputationController extends FeatureComputationController
	{

		private final BranchGraphSynchronizer branchGraphSynchronizer;

		public MamutFeatureComputationController(
				final FeatureComputerService computerService,
				final Collection< Class< ? > > targets,
				final BranchGraphSynchronizer branchGraphSynchronizer )
		{
			super( computerService, targets );
			this.branchGraphSynchronizer = branchGraphSynchronizer;
		}

		@Override
		protected synchronized void compute()
		{
			// Regen branch-graph prior to computation if out of sync.
			if ( !branchGraphSynchronizer.isUptodate() )
				branchGraphSynchronizer.sync();

			super.compute();
		}
	}

	private static final class MyFeatureComputerService implements FeatureComputerService
	{

		private final FeatureComputerService wrapped;

		private final FeatureModel featureModel;

		public MyFeatureComputerService( final FeatureComputerService wrapped, final FeatureModel featureModel )
		{
			this.wrapped = wrapped;
			this.featureModel = featureModel;
		}

		@Override
		public Context getContext()
		{
			return wrapped.getContext();
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
		public Set< FeatureSpec< ?, ? > > getFeatureSpecs()
		{
			return wrapped.getFeatureSpecs();
		}

		@Override
		public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final boolean forceComputeAll,
				final Collection< FeatureSpec< ?, ? > > featureKeys )
		{
			final Map< FeatureSpec< ?, ? >, Feature< ? > > map = wrapped.compute( forceComputeAll, featureKeys );
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
			map.values().forEach( featureModel::declareFeature );

			featureModel.resumeListeners();
			return map;
		}

		@Override
		public FeatureComputer getFeatureComputerFor( final FeatureSpec< ?, ? > spec )
		{
			return wrapped.getFeatureComputerFor( spec );
		}

		@Override
		public Collection< FeatureSpec< ?, ? > > getDependencies( final FeatureSpec< ?, ? > spec )
		{
			return wrapped.getDependencies( spec );
		}
	}
}
