package net.trackmate.model.abstractmodel;

import java.util.Iterator;

public interface Edges< E > extends Iterable< E >
{
	public int size();

	public E get( final int i );

	// garbage-free version
	public E get( int i, final E edge );

	public Iterator< E > safe_iterator();
}
