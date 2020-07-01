package org.mastodon.model;

/**
 * Provides access to {@link AbstractModelGraph} methods that we don't want to
 * be {@code public} but that are needed by importers.
 *
 * @param <M>
 *            the type of model to import.
 *
 * @author Tobias Pietzsch
 */
public abstract class AbstractModelImporter< M extends AbstractModel< ?, ?, ? > >
{
	private final M model;

	protected AbstractModelImporter( final M model )
	{
		this.model = model;
	}

	protected void startImport()
	{
		model.modelGraph.pauseListeners();
		model.modelGraph.clear();
	}

	protected void finishImport()
	{
		model.modelGraph.resumeListeners();
		model.modelGraph.notifyGraphChanged();
	}
}
