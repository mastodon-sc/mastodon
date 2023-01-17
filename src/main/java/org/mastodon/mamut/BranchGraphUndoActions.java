/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;

public class BranchGraphUndoActions
{
	public static final String UNDO = "undo";

	public static final String REDO = "redo";

	static final String[] UNDO_KEYS = new String[] { "meta Z", "ctrl Z" };

	static final String[] REDO_KEYS = new String[] { "meta shift Z", "ctrl shift Z" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( UNDO, UNDO_KEYS, "Undo last edit and sync branch graph." );
			descriptions.add( REDO, REDO_KEYS, "Redo last undone edit and sync branch graph." );
		}
	}

	/**
	 * Create Undo/Redo actions and install them in the specified
	 * {@link Actions}.
	 *  @param actions
	 *            Actions are added here.
	 * @param model
	 *            Actions are targeted at this {@link Model}s {@code undo()} and
	 *            {@code redo()} methods.
	 * @param branchGraphSync
	 */
	public static void install( final Actions actions, final Model model, BranchGraphSynchronizer branchGraphSync )
	{
		actions.runnableAction( () -> {
			model.undo();
			branchGraphSync.sync();
		}, UNDO, UNDO_KEYS );
		actions.runnableAction( () -> {
			model.redo();
			branchGraphSync.sync();
		}, REDO, REDO_KEYS );
	}
}
