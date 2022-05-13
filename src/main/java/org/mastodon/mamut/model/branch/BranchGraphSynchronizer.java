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
