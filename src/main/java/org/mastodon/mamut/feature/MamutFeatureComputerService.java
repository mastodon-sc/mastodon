package org.mastodon.mamut.feature;

import java.util.Map;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputerService;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.scijava.command.CommandModule;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputerService.class )
public class MamutFeatureComputerService extends FeatureComputerService
{

	private SharedBigDataViewerData sharedBdvData;

	private Model model;

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
	 *                          the image data.
	 */
	public void setSharedBdvData( final SharedBigDataViewerData sharedBdvData )
	{
		this.sharedBdvData = sharedBdvData;
	}

	/**
	 * Sets the model to be used by the feature computers.
	 *
	 * @param model
	 *                  the model.
	 */
	public void setModel( final Model model )
	{
		this.model = model;
	}
}
