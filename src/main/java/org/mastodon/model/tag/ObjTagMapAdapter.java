/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.model.tag;

import java.util.Collection;

import org.mastodon.adapter.CollectionAdapter;
import org.mastodon.adapter.RefBimap;

/**
 * Adapts a {@code ObjTagMap<O, T>} as a {@code ObjTagMap<WO, T>}. The mapping
 * between source objects ({@code O}) and wrapped objects ({@code WI}) is
 * established by a {@link RefBimap}.
 *
 * @param <O> object type of source {@code ObjTagMap}.
 *
 * @param <WO>
 *            object type of this wrapped {@code ObjTagMap}.
 * @param <T>
 *            the type of tags.
 *
 * @author Tobias Pietzsch
 */
public class ObjTagMapAdapter< O, WO, T > implements ObjTagMap< WO, T >
{
	private final ObjTagMap< O, T > objTagMap;

	private final RefBimap< O, WO > refmap;

	public ObjTagMapAdapter( final ObjTagMap< O, T > objTagMap, final RefBimap< O, WO > refmap )
	{
		this.objTagMap = objTagMap;
		this.refmap = refmap;
	}

	@Override
	public void set( final WO object, final T tag )
	{
		objTagMap.set( refmap.getLeft( object ), tag );
	}

	@Override
	public void remove( final WO object )
	{
		objTagMap.remove( refmap.getLeft( object ) );
	}

	@Override
	public T get( final WO object )
	{
		return objTagMap.get( refmap.getLeft( object ) );
	}

	@Override
	public Collection< WO > getTaggedWith( final TagSetStructure.Tag tag )
	{
		return new CollectionAdapter<>( objTagMap.getTaggedWith( tag ), refmap );
	}
}
