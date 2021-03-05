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
package org.mastodon.ui.keymap;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Implementations of this interface, annotated with {@code	@Plugin}, are
 * discovered for automatically adding actions/behaviours to a
 * {@link CommandDescriptions} map.
 * <p>
 * (This allows to discover Plugin shortcuts which cannot be hardwired into the
 * default keymap.)
 */
public abstract class CommandDescriptionProvider implements SciJavaPlugin
{
	private final String[] expectedContexts;

	private final Scope scope;

	protected CommandDescriptionProvider( final Scope scope, final String... expectedContexts )
	{
		this.scope = scope;
		this.expectedContexts = expectedContexts;
	}

	/**
	 * The contexts in which the described actions/bahaviours are expected to be
	 * used.
	 * <p>
	 * The scope describes the application / Fiji plugin that the actions are
	 * defined in. This is to make it possible to only harvest descriptions
	 * for the desired scopes.
	 * <p>
	 * Note that {@code CommandDescriptionProvider} is only used for harvesting
	 * actions/behaviours for the config dialog. So basically it has nothing to
	 * do with reality, necessarily. Whether these actions are ever actualized
	 * depends on other code! The {@code scope} is for which scope
	 * the config dialog will make the actions configurable. For example, if you
	 * put {"mastodon"} there, then the {@code InputTriggerConfig} will be picked
	 * up by a config dialog requesting these scopes.
	 */
	public Scope getScope()
	{
		return scope;
	}

	/**
	 * The contexts in which the described actions/bahaviours are expected to be
	 * used.
	 * <p>
	 * Note that {@code CommandDescriptionProvider} is only used for harvesting
	 * actions/behaviours for the config dialog. So basically it has nothing to
	 * do with reality, necessarily. Whether these actions are ever actualized
	 * depends on other code! The {@code expectedContexts} is for which context
	 * the config dialog will make the actions configurable. For example, if you
	 * put {"bdv", "ts"} there, then the {@code InputTriggerConfig} made by the
	 * config dialog will put InputTriggers with these contexts.
	 */
	public String[] getExpectedContexts()
	{
		return expectedContexts;
	}

	public abstract void getCommandDescriptions( final CommandDescriptions descriptions );

	public static class Scope
	{
		private final String name;

		public Scope( final String name )
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( !( o instanceof Scope ) )
				return false;
			return name.equals( ( ( Scope ) o ).name );
		}

		@Override
		public int hashCode()
		{
			return name.hashCode();
		}
	}
}
