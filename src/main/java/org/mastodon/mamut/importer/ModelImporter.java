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

	@Override
	protected void startImport()
	{
		super.startImport();
		model.getTagSetModel().pauseListeners();
		model.getTagSetModel().clear();
	}

	@Override
	protected void finishImport()
	{
		model.getTagSetModel().resumeListeners();
		super.finishImport();
	}
}
