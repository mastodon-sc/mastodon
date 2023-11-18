/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.loader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.janelia.saalfeldlab.n5.N5URI;
import org.jdom2.Element;

import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

import static mpicbg.spim.data.XmlKeys.IMGLOADER_FORMAT_ATTRIBUTE_NAME;

@ImgLoaderIo( format = "bdv.n5.universe", type = N5UniverseImgLoader.class )
public class XmlIoN5UniverseImgLoader implements XmlIoBasicImgLoader< N5UniverseImgLoader >
{
    public static final String URL = "Url";

    public static final String DATASET = "Dataset";

    @Override
    public Element toXml( N5UniverseImgLoader imgLoader, File basePath )
    {
        final Element elem = new Element( "ImageLoader" );
        elem.setAttribute( IMGLOADER_FORMAT_ATTRIBUTE_NAME, "bdv.n5.universe" );
        elem.setAttribute( "version", "1.0" );
        String url = imgLoader.getUrl();
        if ( hasSchema( url ) )
        {
            elem.addContent( XmlHelpers.pathElement( URL, new File( url ), basePath ) );
        }
        else
        {
            elem.addContent( XmlHelpers.textElement( URL, imgLoader.getUrl() ) );
        }
        elem.addContent( XmlHelpers.textElement( DATASET, imgLoader.getDataset() ) );
        return elem;
    }

    @Override
    public N5UniverseImgLoader fromXml( Element elem, File basePath, AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
    {
        String url = XmlHelpers.getText( elem, URL );
        if ( !hasSchema( url ) )
        {
            url = XmlHelpers.loadPath( elem, URL, basePath ).toString();
        }
        final String dataset = XmlHelpers.getText( elem, DATASET );
        return new N5UniverseImgLoader( url, dataset, sequenceDescription );
    }

    private static boolean hasSchema( final String url )
    {
        String scheme = null;
        try
        {
            final URI encodedUri = N5URI.encodeAsUri( url );
            scheme = encodedUri.getScheme();

        }
        catch ( URISyntaxException ignored )
        {}
        return scheme != null;
    }

}
