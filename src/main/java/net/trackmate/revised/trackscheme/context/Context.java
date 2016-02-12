package net.trackmate.revised.trackscheme.context;

import java.util.concurrent.locks.Lock;

public interface Context< V >
{
	public Lock readLock();

	public Iterable< V > getInsideVertices( final int timepoint );

	// TODO: revise. not clear yet how timepoints and context interact
	public int getTimepoint();
}
