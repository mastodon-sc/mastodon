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
package org.mastodon.model;

import org.mastodon.grouping.ForwardingModel;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.scijava.listeners.Listeners;

/**
 * A {@link TimepointModel} forwarding to a switchable backing
 * {@link TimepointModel}.
 * <p>
 * Used for grouping views, see {@link GroupManager}.
 *
 * @author Tobias Pietzsch
 */
public class ForwardingTimepointModel implements TimepointModel, ForwardingModel< TimepointModel >
{
	private TimepointModel model;

	private final Listeners.List< TimepointListener > listeners;

	private final TimepointListener listener;

	public ForwardingTimepointModel()
	{
		listeners = new Listeners.SynchronizedList<>();
		listener = () -> listeners.list.forEach( TimepointListener::timepointChanged );
	}

	@Override
	public void setTimepoint( final int t )
	{
		model.setTimepoint( t );
	}

	@Override
	public int getTimepoint()
	{
		return model.getTimepoint();
	}

	@Override
	public Listeners< TimepointListener > listeners()
	{
		return listeners;
	}

	@Override
	public void linkTo( final TimepointModel newModel, final boolean copyCurrentStateToNewModel )
	{
		final TimepointModel oldModel = model;
		model = newModel;

		if ( oldModel != null )
			oldModel.listeners().remove( listener );
		newModel.listeners().add( listener );

		if ( copyCurrentStateToNewModel )
		{
			newModel.setTimepoint( oldModel.getTimepoint() );
		}
		else
		{
			if ( oldModel == null || oldModel.getTimepoint() != newModel.getTimepoint() )
				listener.timepointChanged();
		}
	}

	public static final GroupableModelFactory< TimepointModel > factory = new GroupableModelFactory< TimepointModel >()
	{
		@Override
		public TimepointModel createBackingModel()
		{
			return new DefaultTimepointModel();
		}

		@Override
		public ForwardingModel< TimepointModel > createForwardingModel()
		{
			return new ForwardingTimepointModel();
		}
	};
}
