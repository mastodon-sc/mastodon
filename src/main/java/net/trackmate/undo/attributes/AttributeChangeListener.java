package net.trackmate.undo.attributes;

public interface AttributeChangeListener< O >
{
	public void beforeAttributeChange( final Attribute< O > attribute, final O object );
}
