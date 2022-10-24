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
package org.mastodon.mamut.model.branch;

import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphListener;
import org.scijava.listeners.Listeners;

public class BranchGraphSynchronizer implements GraphChangeListener
{

	public interface UpdateListener
	{
		public void branchGraphSyncChanged();
	}

	private final GraphListener< ?, ? > bg;

	private final ReadLock lock;

	private final Listeners.List< UpdateListener > listeners;

	private boolean uptodate;

	public BranchGraphSynchronizer( final GraphListener< ?, ? > bg, final ReadLock readLock )
	{
		this.bg = bg;
		this.lock = readLock;
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public void sync()
	{
		lock.lock();
		try
		{
			uptodate = true;
			bg.graphRebuilt();
			notifyListeners();
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void graphChanged()
	{
		uptodate = false;
		notifyListeners();
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : listeners.list )
			l.branchGraphSyncChanged();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return listeners;
	}

	public boolean isUptodate()
	{
		return uptodate;
	}
}
