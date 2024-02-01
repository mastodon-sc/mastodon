/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.io.yaml;

import java.util.Map;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

public abstract class WorkaroundRepresent implements Represent
{
	private final WorkaroundRepresenter r;

	private final Tag tag;

	private final Class< ? > clazz;

	public WorkaroundRepresent( final WorkaroundRepresenter r, final Tag tag, final Class< ? > clazz )
	{
		this.r = r;
		this.tag = tag;
		this.clazz = clazz;
	}

	protected Node representSequence( final Tag tag, final Iterable< ? > sequence, final FlowStyle flowStyle )
	{
		return r.representSequence( tag, sequence, flowStyle );
	}

	protected Node representMapping( final Tag tag, final Map< ?, ? > mapping, final FlowStyle flowStyle )
	{
		return r.representMapping( tag, mapping, flowStyle );
	}

	protected FlowStyle getDefaultFlowStyle()
	{
		return r.getDefaultFlowStyle();
	}

	protected Tag getTag()
	{
		return tag;
	}

	protected Class< ? > getRepresentedClass()
	{
		return clazz;
	}
}
