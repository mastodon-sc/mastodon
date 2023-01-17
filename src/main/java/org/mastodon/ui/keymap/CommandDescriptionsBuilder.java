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
package org.mastodon.ui.keymap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.AbstractContextual;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

/**
 * Helper class to populate {@link CommandDescriptions} from
 * {@link CommandDescriptionProvider}s.
 */
public class CommandDescriptionsBuilder extends AbstractContextual
{
	@Parameter
	private PluginService pluginService;

	private static class ProviderAndContext
	{
		final CommandDescriptionProvider provider;

		final String context;

		public ProviderAndContext( final CommandDescriptionProvider provider, final String context )
		{
			this.provider = provider;
			this.context = context;
		}
	}

	private final List< ProviderAndContext > registered = new ArrayList<>();

	/**
	 * Manually adds a {@code provider} in a specified {@code context}. For
	 * example, this is useful for adding stuff in a specific order, for
	 * building nice {@code keyconfig.yaml} files.
	 *
	 * @param provider
	 *            the provider to add.
	 * @param context
	 *            the context to add to.
	 */
	public void addManually( final CommandDescriptionProvider provider, final String context )
	{
		for ( final ProviderAndContext pac : registered )
		{
			if ( pac.context.equals( context ) && pac.provider.getClass().equals( provider.getClass() ) )
				System.err.println( "Potential problem: a provider of class " + provider.getClass()
						+ " is already registered for context \"" + context + "\"." );
		}
		registered.add( new ProviderAndContext( provider, context ) );
	}

	/**
	 * Manually add a {@code provider} in the specified {@code contexts}. For
	 * example, this is useful for adding stuff in a specific order, for
	 * building nice {@code keyconfig.yaml} files.
	 *
	 * @param provider
	 *            the provider to add.
	 * @param contexts
	 *            the list of contexts to add to.
	 */
	public void addManually( final CommandDescriptionProvider provider, final String... contexts )
	{
		Arrays.stream( contexts ).forEachOrdered( context -> addManually( provider, context ) );
	}

	/**
	 * Adds all {@link CommandDescriptionProvider}s on the plugin index, with
	 * their respective {@link CommandDescriptionProvider#getExpectedContexts()
	 * expected contexts}.
	 */
	public void discoverProviders()
	{
		final List< CommandDescriptionProvider > providers =
				pluginService.createInstancesOfType( CommandDescriptionProvider.class );
		for ( final CommandDescriptionProvider provider : providers )
			for ( final String context : provider.getExpectedContexts() )
				registered.add( new ProviderAndContext( provider, context ) );
	}

	/**
	 * Debugging helper. Checks whether all manually added providers are
	 * automatically discovered, and vice versa. Prints warnings to stderr
	 * otherwise.
	 */
	public void verifyManuallyAdded()
	{
		final List< ProviderAndContext > discovered = new ArrayList<>();
		final List< CommandDescriptionProvider > providers =
				pluginService.createInstancesOfType( CommandDescriptionProvider.class );
		for ( final CommandDescriptionProvider provider : providers )
			for ( final String context : provider.getExpectedContexts() )
				discovered.add( new ProviderAndContext( provider, context ) );

		// Can all registered providers be discovered?
		boolean anyFailed = false;
		A: for ( final ProviderAndContext r : registered )
		{
			for ( final ProviderAndContext d : discovered )
				if ( r.context.equals( d.context ) && r.provider.getClass().equals( d.provider.getClass() ) )
					continue A;
			System.err.println( r.provider.getClass() + " (\"" + r.context
					+ "\") is manually registered, but could not be discovered." );
			anyFailed = true;
		}
		if ( anyFailed )
			System.err.println();

		// Were all discovered providers manually added?
		A: for ( final ProviderAndContext d : discovered )
		{
			for ( final ProviderAndContext r : registered )
				if ( r.context.equals( d.context ) && r.provider.getClass().equals( d.provider.getClass() ) )
					continue A;
			System.err.println( d.provider.getClass() + " (\"" + d.context
					+ "\") was discovered, but was not manually registered." );
		}
	}

	public CommandDescriptions build()
	{
		final CommandDescriptions descriptions = new CommandDescriptions();
		for ( final ProviderAndContext pac : registered )
		{
			descriptions.setKeyconfigContext( pac.context );
			pac.provider.getCommandDescriptions( descriptions );
		}
		return descriptions;
	}
}
