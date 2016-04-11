package net.trackmate.revised.ui.util;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

public class InvokeOnEDT
{
	public static void invokeAndWait( final Runnable runnable ) throws InvocationTargetException, InterruptedException
	{
		if ( SwingUtilities.isEventDispatchThread() )
			runnable.run();
		else
			SwingUtilities.invokeAndWait( runnable );
	}
}
