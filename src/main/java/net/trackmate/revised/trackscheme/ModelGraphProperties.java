package net.trackmate.revised.trackscheme;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.Ref;

/*
 * TODO: revise javadoc. not true anymore that ModelGraphProperties are read-only.
 */

/**
 * Interface for classes that access model graph properties.
 * <p>
 * To make {@link TrackSchemeGraph} adaptable to various model graph type
 * without requiring the graph to implement specific interfaces, we access
 * properties of model vertices and edges (for example the label of a vertex)
 * through {@link ModelGraphProperties}.
 * <p>
 * This class only offers read access to properties. They must be set elsewhere.
 * Edge and vertex properties are accessed through the edge and vertex ID (see
 * {@link GraphIdBimap}).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface ModelGraphProperties
{

	/**
	 * Interface for accessor classes that offer edge properties.
	 * <p>
	 * It is safe to assume that there will never be concurrent calls to a
	 * single {@link ModelEdgeProperties} instance. Therefore, for example the
	 * instance can re-use a single {@link Ref} instance instead of creating and
	 * releasing {@link Ref}s in each {@link #isSelected(int)} call.
	 * <p>
	 * Properties can only be read via this interface. Writing property values
	 * must be done elsewhere.
	 * <p>
	 * Edge properties are features defined for individual edges. They are
	 * distinct from the core graph properties of the edges in the graph, such
	 * as source vertex and target vertex. They may be stored in the graph
	 * objects themselves (for example labels) or separately (for example
	 * {@code selected} state).
	 */
	public static interface ModelEdgeProperties
	{
		/**
		 * Returns the selected state of the model edge with the specified id
		 * (see {@link GraphIdBimap}).
		 *
		 * @param id
		 *            the id of the edge.
		 * @return whether the edge is selected.
		 */
		boolean isSelected( int id );
	}

	/**
	 * Interface for accessor classes that offer vertex properties.
	 * <p>
	 * It is safe to assume that there will never be concurrent calls to a
	 * single {@link ModelVertexProperties} instance. Therefore, for example the
	 * instance can re-use a single {@link Ref} instance instead of creating and
	 * releasing {@link Ref}s in each {@link #isSelected(int)} call.
	 * <p>
	 * Properties can only be read via this interface. Writing property values
	 * must be done elsewhere.
	 * <p>
	 * Vertex properties are features defined for individual vertices. They are
	 * distinct from the core graph properties of the vertices in the graph,
	 * such as edges, etc. They may be stored in the graph objects themselves
	 * (for example labels) or separately (for example {@code selected} state).
	 */
	public static interface ModelVertexProperties
	{
		/**
		 * Returns the selected state of the vertex with the specified id (see
		 * {@link GraphIdBimap}).
		 *
		 * @param id
		 *            the id of the vertex.
		 * @return whether the vertex is selected.
		 */
		public boolean isSelected( int id );

		/**
		 * Returns the label of the vertex with the specified id (see
		 * {@link GraphIdBimap}).
		 *
		 * @param id
		 *            the id of the vertex.
		 * @return the vertex label.
		 */
		public String getLabel( int id );

		/**
		 * Set the label of the vertex with the specified id (see
		 * {@link GraphIdBimap}).
		 *
		 * @param id
		 *            the id of the vertex.
		 * @param label
		 *            the label to set.
		 */
		public void setLabel( int id, String label );
	}

	/**
	 * Returns a new {@link ModelVertexProperties} for the model graph.
	 *
	 * @return a new ModelVertexProperties instance.
	 */
	public ModelVertexProperties createVertexProperties();

	/**
	 * Returns a new {@link ModelEdgeProperties} for the model graph.
	 *
	 * @return a new ModelEdgeProperties instance.
	 */
	public ModelEdgeProperties createEdgeProperties();
}
