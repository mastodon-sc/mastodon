/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.io.yaml;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public abstract class AbstractWorkaroundConstruct extends AbstractConstruct
{
	private final WorkaroundConstructor c;

	private final Tag tag;

	public AbstractWorkaroundConstruct( final WorkaroundConstructor c, final Tag tag )
	{
		this.c = c;
		this.tag = tag;
	}

	protected List< ? > constructSequence( final SequenceNode node )
	{
		return c.constructSequence( node );
	}

	protected Map< Object, Object > constructMapping( final MappingNode node )
	{
		return c.constructMapping( node );
	}

	protected Tag getTag()
	{
		return tag;
	}

	/*
	 * Static utilities to facilitate 'manually' reconstructing an object from a
	 * YAML list, and generate meaningful exception when something goes wrong.
	 */

	protected static final String getString( final Map< Object, Object > mapping, final String key )
	{
		return getStringOrDefault( mapping, key, null );
	}

	protected static final String getStringOrDefault( final Map< Object, Object > mapping, final String key, final String defaultValue )
	{
		return getOrDefault( mapping, key, String.class, defaultValue );
	}

	protected static final int getInt( final Map< Object, Object > mapping, final String key )
	{
		return getOrDefault( mapping, key, Number.class, null ).intValue();
	}

	protected static final int getIntOrDefault( final Map< Object, Object > mapping, final String key, final int defaultValue )
	{
		return getOrDefault( mapping, key, Number.class, defaultValue ).intValue();
	}

	protected static final double getDouble( final Map< Object, Object > mapping, final String key )
	{
		return getOrDefault( mapping, key, Number.class, null ).doubleValue();
	}

	protected static final double getDoubleOrDefault( final Map< Object, Object > mapping, final String key, final double defaultValue )
	{
		return getOrDefault( mapping, key, Number.class, defaultValue ).doubleValue();
	}

	protected static final float getFloat( final Map< Object, Object > mapping, final String key )
	{
		return getOrDefault( mapping, key, Number.class, null ).floatValue();
	}

	protected static final float getFloatOrDefault( final Map< Object, Object > mapping, final String key, final float defaultValue )
	{
		return getOrDefault( mapping, key, Number.class, defaultValue ).floatValue();
	}

	protected static final boolean getBoolean( final Map< Object, Object > mapping, final String key )
	{
		return getOrDefault( mapping, key, Boolean.class, null );
	}

	protected static final boolean getBooleanOrDefault( final Map< Object, Object > mapping, final String key, final boolean defaultValue )
	{
		return getOrDefault( mapping, key, Boolean.class, defaultValue );
	}


	@SuppressWarnings( "unchecked" )
	protected static final < T > T getOrDefault( final Map< Object, Object > mapping, final String key, final Class< T > klass, final T defaultValue )
	{
		final Object obj = mapping.getOrDefault( key, defaultValue );
		if ( obj == null )
			throw new YAMLException( "Could not find value for required parameter '" + key + "'." );

		if ( klass.isInstance( obj ) )
			return ( T ) obj;
		else
			throw new YAMLException( "Incorrect value class for parameter '" + key
					+ "'. Expected " + klass.getSimpleName()
					+ " but found " + obj.getClass().getSimpleName() + "." );
	}

}
