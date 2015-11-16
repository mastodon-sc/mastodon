package net.trackmate.revised.trackscheme;

/**
 * Interface for classes that offer graph properties.
 * <p>
 * Graph properties store vertex and edge properties distinct from the graph
 * structure itself, and are therefore stored elsewhere.
 * <p>
 * This class only offers read access to properties. They must be set elsewhere.
 * Edge and vertex properties are accessed through the edge and vertex id.
 * 
 * @author Tobias Pietzsch
 */
public interface ModelGraphProperties
{

	/**
	 * Interface for classes that offer edge properties.
	 * <p>
	 * Properties can only be read via this class. Writing property values must
	 * be done elsewhere.
	 * <p>
	 * Edge properties are features defined for individual edges. They are
	 * distinct from the core graph properties of the edges in the graph, such
	 * as source vertex and target vertex. It therefore makes sense not to store
	 * them in the graph. Properties can be numerical, boolean, alphanumerical,
	 * etc.
	 * <p>
	 * This interface is for classes where properties are accessed via the edges
	 * <code>int</code> id.
	 * 
	 * @author Tobias Pietzsch
	 */
	public static interface ModelEdgeProperties
	{
		/**
		 * Returns the selected state of the edge with the specified id.
		 * 
		 * @param id
		 *            the id of the edge.
		 * @return the edge selected state.
		 */
		boolean isSelected( int id );
	}

	/**
	 * Interface for classes that offer vertex properties.
	 * <p>
	 * Properties can only be read via this class. Writing property values must
	 * be done elsewhere.
	 * <p>
	 * Vertex properties are features defined for individual vertices. They are
	 * distinct from the core graph properties of the vertices in the graph,
	 * such as edges, etc. It therefore makes sense not to store them in the
	 * graph. Properties can be numerical, boolean, alphanumerical, etc.
	 * <p>
	 * This interface is for classes where properties are accessed via the
	 * vertices <code>int</code> id.
	 * 
	 * @author Tobias Pietzsch
	 */
	public static interface ModelVertexProperties
	{
		/**
		 * Returns the selected state of the vertex with the specified id.
		 * 
		 * @param id
		 *            the id of the vertex.
		 * @return the vertex selected state.
		 */
		public boolean isSelected( int id );

		/**
		 * Returns the label of the vertex with the specified id.
		 * 
		 * @param id
		 *            the id of the vertex.
		 * @return the vertex label.
		 */
		public String getLabel( int id );
	}

	/**
	 * Returns a new {@link ModelVertexProperties} for the model graph.
	 * 
	 * @return a new ModelVertexProperties.
	 */
	public ModelVertexProperties createVertexProperties();

	/**
	 * Returns a new {@link ModelEdgeProperties} for the model graph.
	 * 
	 * @return a new ModelEdgeProperties.
	 */
	public ModelEdgeProperties createEdgeProperties();
}
