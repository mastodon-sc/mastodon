package net.trackmate.graph;

/**
 * Listens to {@link #graphChanged()} event.
 *
 * <p>
 * In contrast to {@link GraphListener} events, {@link #graphChanged()} is very
 * coarse-grained. It is used to signal that a batch of graph changes is
 * complete. As a consequence, UI elements should be updated etc.
 *
 * <p>
 * {@link #graphChanged()} carries no details about the change that occurred.
 * {@link GraphChangeListener}s may either just recompute their data from the
 * full graph, or they should also implement {@link GraphListener} to collect
 * details about the changes.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface GraphChangeListener
{
	/**
	 * Called when a batch of graph changes is complete.
	 * <p>
	 * In contrast to {@link GraphListener} events, {@link #graphChanged()} is
	 * very coarse-grained. It is used to signal that a batch of graph changes
	 * is complete. As a consequence, UI elements should be updated etc.
	 *
	 * <p>
	 * {@link #graphChanged()} carries no details about the change that
	 * occurred. {@link GraphChangeListener}s may either just recompute their
	 * data from the full graph, or they should also implement
	 * {@link GraphListener} to collect details about the changes.
	 */
	public void graphChanged();
}
