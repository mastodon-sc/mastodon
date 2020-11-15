package org.mastodon.mamut.feature;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mastodon.feature.DefaultFeatureComputerService;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.update.GraphFeatureUpdateListeners;
import org.mastodon.mamut.model.Link;
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

	private final AtomicBoolean shouldRecomputeAll = new AtomicBoolean( false );

	@Parameter
	private FeatureSpecsService featureSpecsService;

	private PropertyChangeListener< Spot > vertexPropertyListener;

	public MamutFeatureComputerService()
	{
		super( MamutFeatureComputer.class );
	}

	@Override
	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final boolean forceComputeAll, final Collection< FeatureSpec< ?, ? > > featureKeys )
	{
		// Set the force flag.
		shouldRecomputeAll.set( forceComputeAll );

		final Map< FeatureSpec< ?, ? >, Feature< ? > > results = super.compute( forceComputeAll, featureKeys );
		if ( isCanceled() )
			return null;

		return results;
	}

	@Override
	protected void provideParameters(
			final ModuleItem< ? > item,
			final CommandModule module, final Class< ? > parameterClass,
			final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel )
	{

		// Pass the model is required.
		if ( Model.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< Model > modelItem = ( ModuleItem< Model > ) item;
			modelItem.setValue( module, model );
			return;
		}

		// Pass the model graph.
		if ( ModelGraph.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< ModelGraph > graphItem = ( ModuleItem< ModelGraph > ) item;
			graphItem.setValue( module, model.getGraph() );
			return;
		}

		// Pass the BDV data.
		if ( SharedBigDataViewerData.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< SharedBigDataViewerData > bdvItem = ( ModuleItem< SharedBigDataViewerData > ) item;
			bdvItem.setValue( module, sharedBdvData );
			return;
		}

		// Pass the "force recompute" flag.
		if ( AtomicBoolean.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< AtomicBoolean > forceRecomputeAllItem = ( ModuleItem< AtomicBoolean > ) item;
			forceRecomputeAllItem.setValue( module, shouldRecomputeAll );
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
		 * Unregister listeners from previous this.model.getGraph().
		 */

		if ( this.model != null )
		{
			final SpotPool previousSpotPool = ( SpotPool ) this.model.getGraph().vertices().getRefPool();
			previousSpotPool.covarianceProperty().propertyChangeListeners().remove( vertexPropertyListener );
			previousSpotPool.positionProperty().propertyChangeListeners().remove( vertexPropertyListener );
		}

		/*
		 * Listen to graph changes to support incremental computation.
		 *
		 * Every-time a spot or a link is modified, they are removed from all
		 * the features of the feature model. That way we limit (but don't
		 * eliminate) the problem of features being out-of-sync after model
		 * modification.
		 *
		 * Feature computer that want and can support incremental computation
		 * can then only compute values for objects not present in the feature
		 * map(s). Unless the #shoudRecomputeAll flag is set.
		 *
		 * This does not eliminate out-of-sync values for all possibilities.
		 * Indeed, there might be feature values that depend on the neighbor
		 * values. If a neighbor of an object is changed, a feature value of the
		 * object that depends on the neighbors will become out of sync.
		 */

		this.model = model;

		// Create listener.
		final FeatureModel featureModel = model.getFeatureModel();
		this.vertexPropertyListener = GraphFeatureUpdateListeners.vertexPropertyListener( featureModel, Spot.class, Link.class );

		// Listen to changes in spot properties.
		final ModelGraph graph = model.getGraph();
		final SpotPool spotPool = ( SpotPool ) graph.vertices().getRefPool();
		spotPool.covarianceProperty().propertyChangeListeners().add( vertexPropertyListener );
		spotPool.positionProperty().propertyChangeListeners().add( vertexPropertyListener );
	}
}
