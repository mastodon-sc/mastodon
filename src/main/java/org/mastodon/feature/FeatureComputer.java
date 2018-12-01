package org.mastodon.feature;

import org.scijava.ItemIO;
import org.scijava.command.Command;

/**
 * Interface for classes that can compute a feature on a model.
 * <p>
 * A computer must generate exactly one single feature as output, annotated with
 * {@link ItemIO#OUTPUT}, and that must be of type {@link Feature}.
 */
public interface FeatureComputer extends Command
{
	void createOutput();
}
