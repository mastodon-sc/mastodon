/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2015 BigDataViewer authors
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
package net.trackmate.revised.bdv;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import bdv.tools.transformation.ManualSourceTransforms;
import bdv.tools.transformation.TransformedSource;
import bdv.tools.transformation.XmlIoTransformedSources;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.realtransform.AffineTransform3D;

//TODO Should be modified in bigdataviewer-core.
public class ManualTransformation
{
	private final XmlIoTransformedSources io;

	private final List< SourceAndConverter< ? > > sources;

	public ManualTransformation( final List< SourceAndConverter< ? > > sources )
	{
		this.sources = sources;
		io = new XmlIoTransformedSources();
	}

	public Element toXml()
	{
		final List< TransformedSource< ? > > sources = getTransformedSources();
		final ArrayList< AffineTransform3D > transforms = new ArrayList< AffineTransform3D >( sources.size() );
		for ( final TransformedSource< ? > s : sources )
		{
			final AffineTransform3D t = new AffineTransform3D();
			s.getFixedTransform( t );
			transforms.add( t );
		}
		return io.toXml( new ManualSourceTransforms( transforms ) );
	}

	public void restoreFromXml( final Element parent )
	{
		final Element elem = parent.getChild( io.getTagName() );
		final List< TransformedSource< ? > > sources = getTransformedSources();
		final List< AffineTransform3D > transforms = io.fromXml( elem ).getTransforms();
		if ( sources.size() != transforms.size() )
			System.err.println( "failed to load <" + io.getTagName() + "> source and transform count mismatch" );
		else
			for ( int i = 0; i < sources.size(); ++i )
				sources.get( i ).setFixedTransform( transforms.get( i ) );
	}

	private ArrayList< TransformedSource< ? > > getTransformedSources()
	{
		final ArrayList< TransformedSource< ? > > list = new ArrayList< TransformedSource< ? > >();
		for ( final SourceAndConverter< ? > soc : sources )
		{
			final Source< ? > source = soc.getSpimSource();
			if ( TransformedSource.class.isInstance( source ) )
				list.add( (TransformedSource< ? > ) source );
		}
		return list;
	}
}
