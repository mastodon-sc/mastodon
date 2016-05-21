package net.trackmate.graph;

/**
 * Backup and restore object or edge features by storing them in a map with
 * {@code int} keys. This is used for implementing undo/redo (hence the name).
 *
 * @param <K>
 *            object type (edge or object).
 */
public interface UndoFeatureMap< K >
{
	/**
	 * Store the feature value of {@code object} with the key {@code undoId}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void store( int undoId, K object );

	/**
	 * Retrieve the feature value stored with key {@code undoId} and set it in
	 * {@code object}. If there is no value associated with {@code undoId},
	 * clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void retrieve( int undoId, K object );

	/**
	 * Store the feature value of {@code object} with the key {@code undoId},
	 * and replace it with the feature value currently stored with key
	 * {@code undoId}. If there is no value currently associated with
	 * {@code undoId}, clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void swap( int undoId, K object );

	/**
	 * Clear the feature value associated with key {@code undoId} (if any).
	 *
	 * @param undoId
	 */
	public void clear( int undoId );
}