package org.mastodon.ui;

/*
 * Not called NavigationBehavior, because we might want to use this keyword in
 * the configurable-keys framework.
 */
/**
 * Enum listing the configurable kind of behaviors that can be chosen when
 * centering.
 *
 * @author Jean-Yves Tinevez
 */
public enum NavigationEtiquette
{
	CENTERING,
	CENTER_IF_INVISIBLE,
	MINIMAL;
}
