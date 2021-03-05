/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
