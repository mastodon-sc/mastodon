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
package org.mastodon.views.table;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.mastodon.RefPool;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.ui.coloring.ColorGenerator;
import org.mastodon.undo.UndoPointMarker;

public class TablePanelBuilder< O >
{

	private final RefPool< O > pool;

	private ObjTags< O > tags;

	private Function< O, String > labelGenerator;

	private BiConsumer< O, String > labelSetter;

	private UndoPointMarker undo;

	private ColorGenerator< O > coloring;

	private TablePanelBuilder( final RefPool< O > pool )
	{
		this.pool = pool;
		this.labelGenerator = o -> o.toString();
		this.labelSetter = ( o, str ) -> {};
	}

	public TablePanelBuilder< O > tags( final ObjTags< O > tags )
	{
		this.tags = tags;
		return this;
	}

	public TablePanelBuilder< O > labelGetter( final Function< O, String > labelGetter )
	{
		this.labelGenerator = labelGetter;
		return this;
	}

	public TablePanelBuilder< O > labelSetter( final BiConsumer< O, String > labelSetter )
	{
		this.labelSetter = labelSetter;
		return this;
	}

	public TablePanelBuilder< O > coloring( final ColorGenerator< O > coloring )
	{
		this.coloring = coloring;
		return this;
	}

	public TablePanelBuilder< O > undo( final UndoPointMarker undo )
	{
		this.undo = undo;
		return this;
	}

	public FeatureTagTablePanel< O > get()
	{
		if ( pool == null )
			throw new IllegalArgumentException( "The object pool cannot be null." );
		final FeatureTagTablePanel< O > tablePanel = new FeatureTagTablePanel<>(
				tags,
				pool,
				labelGenerator,
				labelSetter,
				undo,
				coloring );
		return tablePanel;
	}

	public static < O > TablePanelBuilder< O > create( final RefPool< O > pool )
	{
		return new TablePanelBuilder<>( pool );
	}
}
