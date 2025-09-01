package org.mastodon.app;

public interface MastodonFactory
{

	/**
	 * Returns the name of the command that will use this factory.
	 *
	 * @return the command name.
	 */
	String getCommandName();

	/**
	 * Returns the list of default keystrokes of the command.
	 *
	 * @return the default keystrokes0
	 */
	String[] getCommandKeys();

	/**
	 * Returns the description of the command.
	 *
	 * @return the description.
	 */
	String getCommandDescription();

	/**
	 * Returns the text of the command to appear in menus.
	 *
	 * @return the menu text for the command.
	 */
	String getCommandMenuText();

}
