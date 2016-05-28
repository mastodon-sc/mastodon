package net.trackmate.graph;

public interface ListenableGraph< V extends Vertex< E >, E extends Edge< V > >
	extends Graph< V, E >, ListenableReadOnlyGraph< V, E >
{

	/**
	 * Send {@link GraphChangeListener#graphChanged() graphChanged} event to all
	 * {@link GraphChangeListener} (if sending events is not currently
	 * {@link #pauseListeners() paused}).
	 */
	public void notifyGraphChanged();

	/**
	 * Resume sending events to {@link GraphListener}s, and send
	 * {@link GraphListener#graphRebuilt()} to all registered listeners. This is
	 * called after large modifications to the graph are made, for example when
	 * the graph is loaded from a file.
	 */
	public void resumeListeners();

	/**
	 * Pause sending events to {@link GraphListener}s. This is called before
	 * large modifications to the graph are made, for example when the graph is
	 * loaded from a file.
	 */
	public void pauseListeners();

	public void notifyEdgeAdded( E edge );

	public void notifyVertexAdded( V vertex );
}
