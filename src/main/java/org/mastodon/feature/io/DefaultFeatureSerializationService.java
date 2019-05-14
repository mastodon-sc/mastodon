package org.mastodon.feature.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.feature.FeatureSpec;
import org.scijava.InstantiableException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

@Plugin( type = FeatureSerializationService.class )
public class DefaultFeatureSerializationService extends AbstractService implements FeatureSerializationService
{

	@Parameter
	private PluginService plugins;

	private final Map< FeatureSpec< ?, ? >, FeatureSerializer< ?, ? > > serializers = new HashMap<>();

	@Override
	public void initialize()
	{
		serializers.clear();
		discover();
	}

	private void discover()
	{
		@SuppressWarnings( "rawtypes" )
		final List< PluginInfo< FeatureSerializer > > infos = plugins.getPluginsOfType( FeatureSerializer.class );
		for ( @SuppressWarnings( "rawtypes" )
		final PluginInfo< FeatureSerializer > info : infos )
		{
			try
			{
				@SuppressWarnings( "rawtypes" )
				final FeatureSerializer fs = info.createInstance();
				final FeatureSpec< ?, ? > spec = fs.getFeatureSpec();
				serializers.put( spec, fs );
			}
			catch ( final InstantiableException e )
			{
				/*
				 * TODO: instead of printing the messages, they should be
				 * collected into one big message that can then be obtained from
				 * the Service and presented to the user in some way (the
				 * "presenting to the user" part we can decide on later...).
				 */
				System.out.println( e.getMessage() );
			}
		}
	}

	@Override
	public FeatureSerializer< ?, ? > getFeatureSerializerFor( final FeatureSpec< ?, ? > spec )
	{
		return serializers.get( spec );
	}
}
