package org.mastodon.mamut.importer;

import org.mastodon.mamut.model.Model;
import org.mastodon.model.AbstractModelImporter;

public class ModelImporter extends AbstractModelImporter< Model >
{
	private final Model model;

	protected ModelImporter( final Model model )
	{
		super( model );
		this.model = model;
	}

	/**
	 * Pauses listeners attached to graph and tags. Graph and tags can be updated silently.
	 * Unpause with {@link #finishUpdate()}.
	 */
	@Override
	protected void startUpdate()
	{
		super.startUpdate();
		model.getTagSetModel().pauseListeners();
	}

	/**
	 * A counter action to {@link #startUpdate()}.
	 */
	protected void finishUpdate()
	{
	    //this is only an alias...
		finishImport();
	}

	/**
	 * Pauses listeners attached to graph and tags. Graph and tags are cleared allowing
	 * for a rebuilt from scratch (aka import). Unpause with {@link #finishImport()}.
	 */
	@Override
	protected void startImport()
	{
		super.startImport();
		model.getTagSetModel().pauseListeners();
		model.getTagSetModel().clear();
	}

	/**
	 * A counter action to {@link #startImport()}.
	 */
	@Override
	protected void finishImport()
	{
		model.getTagSetModel().resumeListeners();
		super.finishImport();
	}
}
