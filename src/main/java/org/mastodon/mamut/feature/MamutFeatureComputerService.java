package org.mastodon.mamut.feature;

import java.util.Collection;
import java.util.Map;

import org.mastodon.feature.DefaultFeatureComputerService;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.update.GraphUpdateStack;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;
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

	private GraphUpdateStack< Spot, Link > graphUpdate;

	public MamutFeatureComputerService()
	{
		super( MamutFeatureComputer.class );
	}

	@Override
	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final Collection< String > featureKeys )
	{
		final Map< FeatureSpec< ?, ? >, Feature< ? > > results = super.compute( featureKeys );
		if ( isCanceled() )
			return null;

		// Store updates.
		graphUpdate.commit( featureKeys );
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

		if ( GraphUpdateStack.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< GraphUpdateStack< Spot, Link > > updateItem = ( ModuleItem< GraphUpdateStack< Spot, Link > > ) item;
			updateItem.setValue( module, graphUpdate );
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
		this.model = model;
		// Listen to graph changes to support incremental computation.
		final ModelGraph graph = model.getGraph();
		this.graphUpdate = new GraphUpdateStack<>( graph );
		graph.addGraphListener( graphUpdate.graphListener() );
		// Listen to changes in spot properties.
		final SpotPool spotPool = ( SpotPool ) graph.vertices().getRefPool();
		spotPool.covarianceProperty().addPropertyChangeListener( graphUpdate.vertexPropertyChangeListener() );
		spotPool.positionProperty().addPropertyChangeListener( graphUpdate.vertexPropertyChangeListener() );
	}
}
