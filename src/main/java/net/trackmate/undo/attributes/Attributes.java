package net.trackmate.undo.attributes;

public interface Attributes< O >
{
	public Attribute< O > getAttributeById( final int id );

	public boolean addAttributeChangeListener( final AttributeChangeListener< O > listener );

	public boolean removeAttributeChangeListener( final AttributeChangeListener< O > listener );
}
