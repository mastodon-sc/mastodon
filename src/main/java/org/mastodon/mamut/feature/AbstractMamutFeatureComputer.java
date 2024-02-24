package org.mastodon.mamut.feature;

import org.scijava.command.ContextCommand;

/**
 * Convenience mother class for Mamut feature computers that are:
 * <ul>
 * <li>cancelable,</li>
 * <li>and have a Context.</li>
 * </ul>
 */
public abstract class AbstractMamutFeatureComputer extends ContextCommand implements MamutFeatureComputer
{}
