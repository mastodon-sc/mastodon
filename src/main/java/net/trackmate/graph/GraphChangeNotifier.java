package net.trackmate.graph;

/**
 * Send {@link GraphChangeListener#graphChanged()} events.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface GraphChangeNotifier
{
	/**
	 * Trigger a {@link GraphChangeListener#graphChanged()} event.
	 */
	public void notifyGraphChanged();
}
