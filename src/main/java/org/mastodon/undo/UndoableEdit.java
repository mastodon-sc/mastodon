package org.mastodon.undo;

public interface UndoableEdit
{
	public void undo();

	public void redo();

	public boolean isUndoPoint();

	public void setUndoPoint( boolean isUndoPoint );

	/**
	 * Clear resources associated with thie UndoableEdit. Currently, this is
	 * only implemented in {@code UndoableEditList.Other}, where it is used to remove non-ref
	 * edits from {@link UndoableEditList#nonRefEdits}.
	 */
	public default void clear()
	{}
}
