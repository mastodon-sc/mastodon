package net.trackmate.trackscheme;

import javax.swing.KeyStroke;

/**
 * Named actions that also ship a default KeyStroke.
 */
public abstract class AbstractNamedDefaultKeyStrokeAction extends AbstractNamedAction
{
	private static final long serialVersionUID = 1L;

	private final KeyStroke defaultKeyStroke;

	public AbstractNamedDefaultKeyStrokeAction( final String name, final KeyStroke defaultKeyStroke )
	{
		super( name );
		this.defaultKeyStroke = defaultKeyStroke;
	}

	public KeyStroke getDefaultKeyStroke()
	{
		return defaultKeyStroke;
	}
}
