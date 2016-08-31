package org.mastodon.undo.attributes;

public interface AttributeChangeListener< O >
{
	public void beforeAttributeChange( final Attribute< O > attribute, final O object );
}
