package org.mastodon.views.trackscheme.display;

import bdv.viewer.TransformListener;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.scijava.listeners.Listeners;

public class ScreenTransformState
{
	private final Listeners.List< TransformListener< ScreenTransform > > listeners;

	private final ScreenTransform transform;

	public ScreenTransformState( final ScreenTransform transform )
	{
		this.transform = new ScreenTransform( transform );
		listeners = new Listeners.List<>();
	}

	public ScreenTransformState()
	{
		this( new ScreenTransform() );
	}

	/**
	 * Get the current transform.
	 *
	 * @param transform
	 *     is set to the current transform
	 */
	public synchronized void get( ScreenTransform transform )
	{
		transform.set( this.transform );
	}

	/**
	 * Get the current transform.
	 *
	 * @return a copy of the current transform
	 */
	public synchronized ScreenTransform get()
	{
		return new ScreenTransform( this.transform );
	}

	/**
	 * Set the transform.
	 */
	public synchronized void set( ScreenTransform transform )
	{
		if ( !this.transform.equals( transform ) )
		{
			this.transform.set( transform );
			listeners.list.forEach( l -> l.transformChanged( this.transform ) );
		}
	}

	/**
	 * {@code TransformListener<ScreenTransform>} can be added/removed here.
	 */
	public Listeners< TransformListener< ScreenTransform > > listeners()
	{
		return listeners;
	}
}
