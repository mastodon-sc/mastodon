package org.mastodon.graph;

public interface ListenableGraph< V extends Vertex< E >, E extends Edge< V > >
	extends Graph< V, E >, ListenableReadOnlyGraph< V, E >
{}
