/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display.style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mastodon.app.ui.AbstractStyleManagerYaml;
import org.yaml.snakeyaml.Yaml;

/**
 * Manages a collection of {@link DataDisplayStyle}.
 * <p>
 * Has serialization / deserialization facilities and can return models based on
 * the collection it manages.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class DataDisplayStyleManager extends AbstractStyleManagerYaml< DataDisplayStyleManager, DataDisplayStyle >
{
	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/datagraphstyles.yaml";

	/**
	 * A {@code DataDisplayStyle} that has the same properties as the default
	 * style. In contrast to defaultStyle this will always refer to the same
	 * object, so a data graph view can just use this one style to listen for
	 * changes and for painting.
	 */
	private final DataDisplayStyle forwardDefaultStyle;

	private final DataDisplayStyle.UpdateListener updateForwardDefaultListeners;

	public DataDisplayStyleManager()
	{
		this( true );
	}

	public DataDisplayStyleManager( final boolean loadStyles )
	{
		forwardDefaultStyle = DataDisplayStyle.defaultStyle().copy();
		updateForwardDefaultListeners = () -> forwardDefaultStyle.set( selectedStyle );
		selectedStyle.updateListeners().add( updateForwardDefaultListeners );
		if ( loadStyles )
			loadStyles();
	}

	@Override
	protected List< DataDisplayStyle > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( DataDisplayStyle.defaults ) );
	}

	@Override
	public synchronized void setSelectedStyle( final DataDisplayStyle style )
	{
		selectedStyle.updateListeners().remove( updateForwardDefaultListeners );
		selectedStyle = style;
		forwardDefaultStyle.set( selectedStyle );
		selectedStyle.updateListeners().add( updateForwardDefaultListeners );
	}

	/**
	 * Returns a final {@link DataDisplayStyle} instance that always has the
	 * same properties as the default style.
	 *
	 * @return a style instance that always has the same properties as the default style.
	 */
	public DataDisplayStyle getForwardDefaultStyle()
	{
		return forwardDefaultStyle;
	}

	public void loadStyles()
	{
		loadStyles( STYLE_FILE );
	}

	@Override
	public void saveStyles()
	{
		saveStyles( STYLE_FILE );
	}

	@Override
	protected Yaml createYaml()
	{
		return DataDisplayStyleIO.createYaml();
	}
}
