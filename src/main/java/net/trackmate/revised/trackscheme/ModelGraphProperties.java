package net.trackmate.revised.trackscheme;

public interface ModelGraphProperties
{
	public static interface ModelEdgeProperties
	{
		boolean isSelected( int id );
	}

	public static interface ModelVertexProperties
	{
		public boolean isSelected( int id );

		public String getLabel( int id );
	}

	public ModelVertexProperties createVertexProperties();

	public ModelEdgeProperties createEdgeProperties();
}
