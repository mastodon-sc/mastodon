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
package org.mastodon.mamut.views.table;

import org.mastodon.app.views.table.AbstractMastodonViewTableFactory;
import org.mastodon.app.views.table.TableModelGraphProperties;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.views.MamutViewFactory2;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory2.class, priority = Priority.NORMAL - 1 )
public class MamutViewSelectionTableFactory2
		extends AbstractMastodonViewTableFactory< MamutViewSelectionTable2, ModelGraph, MamutAppModel >
		implements MamutViewFactory2< MamutViewSelectionTable2 >
{

	@Override
	protected TableModelGraphProperties< Spot > getModelGraphProperties( final ModelGraph graph )
	{
		return new MamutTableProperties( graph );
	}

	@Override
	public MamutViewSelectionTable2 create( final MamutAppModel appModel )
	{
		final TableModelGraphProperties< Spot > modelGraphProperties = getModelGraphProperties( appModel.dataModel().getGraph() );
		return new MamutViewSelectionTable2( appModel.dataModel(), appModel.uiModel(), modelGraphProperties );
	}
}
