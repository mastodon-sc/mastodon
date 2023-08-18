package org.mastodon.mamut.views;

import org.mastodon.app.ui.HasFrame;
import org.mastodon.grouping.GroupHandle;

/**
 * Interface for the views specific to the Mamut application. They have at least
 * a frame, a group handle and a onClose() method.
 */
public interface MamutViewI extends HasFrame
{

	public void onClose( final Runnable runnable );

	public GroupHandle getGroupHandle();

}
