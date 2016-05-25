package net.trackmate.graph;

/**
 * Backup and restore vertex or edge features by storing them in a map with
 * {@code int} keys. This is used for implementing undo/redo (hence the name).
 *
 * @param <O>
 *            object type (an object that has features, i.e., edge or vertex).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
// TODO: move to net.trackmate.graph.undo package?
public interface UndoFeatureMap< O >
{
	/**
	 * Store the feature value of {@code object} with the key {@code undoId}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void store( int undoId, O object );

	/**
	 * Retrieve the feature value stored with key {@code undoId} and set it in
	 * {@code object}. If there is no value associated with {@code undoId},
	 * clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void retrieve( int undoId, O object );

	/**
	 * Store the feature value of {@code object} with the key {@code undoId},
	 * and replace it with the feature value currently stored with key
	 * {@code undoId}. If there is no value currently associated with
	 * {@code undoId}, clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void swap( int undoId, O object );

	/**
	 * Clear the feature value associated with key {@code undoId} (if any).
	 *
	 * @param undoId
	 */
	public void clear( int undoId );
}
