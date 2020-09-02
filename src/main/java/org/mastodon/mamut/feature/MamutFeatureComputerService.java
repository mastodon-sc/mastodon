package org.mastodon.mamut.feature;

import java.util.Collection;
import java.util.Map;

import org.mastodon.feature.DefaultFeatureComputerService;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.update.GraphFeatureUpdateListeners;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.SpotPool;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.command.CommandModule;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputerService.class )
public class MamutFeatureComputerService extends DefaultFeatureComputerService
{

	private SharedBigDataViewerData sharedBdvData;

	private Model model;

	@Parameter
	private FeatureSpecsService featureSpecsService;

	public MamutFeatureComputerService()
	{
		super( MamutFeatureComputer.class );
	}

	@Override
	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final boolean forceComputeAll, final Collection< FeatureSpec< ?, ? > > featureKeys )
	{
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();
		final SpotUpdateStack spotUpdates = SpotUpdateStack.getOrCreate( featureModel, graph.vertices() );
		final LinkUpdateStack linkUpdates = LinkUpdateStack.getOrCreate( featureModel, graph.edges() );

		// Clear the update stacks if we have the force flag.
		if ( forceComputeAll )
		{
			spotUpdates.clear();
			linkUpdates.clear();
		}

		final Map< FeatureSpec< ?, ? >, Feature< ? > > results = super.compute( forceComputeAll, featureKeys );
		if ( isCanceled() )
			return null;

		// Store updates.
		spotUpdates.commit( featureKeys );
		linkUpdates.commit( featureKeys );
		return results;
	}

	@Override
	protected void provideParameters( final ModuleItem< ? > item, final CommandModule module, final Class< ? > parameterClass, final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel )
	{
		if ( Model.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< Model > modelItem = ( ModuleItem< Model > ) item;
			modelItem.setValue( module, model );
			return;
		}

		if ( ModelGraph.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< ModelGraph > graphItem = ( ModuleItem< ModelGraph > ) item;
			graphItem.setValue( module, model.getGraph() );
			return;
		}

		if ( SharedBigDataViewerData.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< SharedBigDataViewerData > bdvItem = ( ModuleItem< SharedBigDataViewerData > ) item;
			bdvItem.setValue( module, sharedBdvData );
			return;
		}

		super.provideParameters( item, module, parameterClass, featureModel );
	}

	/**
	 * Sets the image data to be used by the feature computers.
	 *
	 * @param sharedBdvData
	 *            the image data.
	 */
	public void setSharedBdvData( final SharedBigDataViewerData sharedBdvData )
	{
		this.sharedBdvData = sharedBdvData;
	}

	/**
	 * Sets the model to be used by the feature computers.
	 *
	 * @param model
	 *            the model.
	 */
	public void setModel( final Model model )
	{
		/*
		 * TODO: Unregister listeners from previous this.model.getGraph()
		 *       For this, the listeners should be remembered (graphListener, vertexPropertyListener)
		 */

		this.model = model;

		// Listen to graph changes to support incremental computation.
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();
		final SpotUpdateStack spotUpdates = SpotUpdateStack.getOrCreate( featureModel, graph.vertices() );
		final LinkUpdateStack linkUpdates = LinkUpdateStack.getOrCreate( featureModel, graph.edges() );

		graph.addGraphListener( GraphFeatureUpdateListeners.graphListener( spotUpdates, linkUpdates, graph.vertexRef() ) );
		// Listen to changes in spot properties.
		final SpotPool spotPool = ( SpotPool ) graph.vertices().getRefPool();
		final PropertyChangeListener< Spot > vertexPropertyListener = GraphFeatureUpdateListeners.vertexPropertyListener( spotUpdates, linkUpdates );
		spotPool.covarianceProperty().addPropertyChangeListener( vertexPropertyListener );
		spotPool.positionProperty().addPropertyChangeListener( vertexPropertyListener );
	}
}
