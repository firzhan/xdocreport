/**
 * Copyright (C) 2011 The XDocReport Team <xdocreport@googlegroups.com>
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.odftoolkit.odfdom.converter.pdf.internal.stylable;

import java.awt.Color;
import java.util.ArrayList;

import org.odftoolkit.odfdom.converter.pdf.internal.styles.Style;
import org.odftoolkit.odfdom.converter.pdf.internal.styles.StyleBorder;
import org.odftoolkit.odfdom.converter.pdf.internal.styles.StyleBreak;
import org.odftoolkit.odfdom.converter.pdf.internal.styles.StyleLineHeight;
import org.odftoolkit.odfdom.converter.pdf.internal.styles.StyleParagraphProperties;
import org.odftoolkit.odfdom.converter.pdf.internal.styles.StyleTextProperties;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;

import fr.opensagres.xdocreport.itext.extension.ExtendedParagraph;

/**
 * fixes for paragraph pdf conversion by Leszek Piotrowicz <leszekp@safe-mail.net>
 */
public class StylableParagraph
    extends ExtendedParagraph
    implements IStylableContainer
{
    private static final long serialVersionUID = 664309269352903329L;

    private static final float DEFAULT_LINE_HEIGHT = 1.0f;

    private final StylableDocument ownerDocument;

    private IStylableContainer parent;

    private Style lastStyleApplied = null;

    private boolean elementPostProcessed = false;

    public StylableParagraph( StylableDocument ownerDocument, IStylableContainer parent )
    {
        super();
        this.ownerDocument = ownerDocument;
        this.parent = parent;
        super.setMultipliedLeading( DEFAULT_LINE_HEIGHT );
    }

    public StylableParagraph( StylableDocument ownerDocument, Paragraph title, IStylableContainer parent )
    {
        super( title );
        this.ownerDocument = ownerDocument;
        this.parent = parent;
        super.setMultipliedLeading( DEFAULT_LINE_HEIGHT );
    }

    public void addElement( Element element )
    {
        super.add( element );
    }

    public void applyStyles( Style style )
    {
        this.lastStyleApplied = style;

        StyleTextProperties textProperties = style.getTextProperties();
        if ( textProperties != null )
        {
            // Font
            Font font = textProperties.getFont();
            if ( font != null )
            {
                super.setFont( font );
            }
        }

        StyleParagraphProperties paragraphProperties = style.getParagraphProperties();
        if ( paragraphProperties != null )
        {
            // break-before
            StyleBreak breakBefore = paragraphProperties.getBreakBefore();
            if ( breakBefore != null )
            {
                handleBreak( breakBefore );
            }

            // alignment
            int alignment = paragraphProperties.getAlignment();
            if ( alignment != Element.ALIGN_UNDEFINED )
            {
                super.setAlignment( alignment );
            }

            // paragraph indentation
            Float margin = paragraphProperties.getMargin();
            if ( margin != null )
            {
                super.setIndentationLeft( margin );
                super.setIndentationRight( margin );
                super.setSpacingBefore( margin );
                super.setSpacingAfter( margin );
            }
            Float marginLeft = paragraphProperties.getMarginLeft();
            if ( marginLeft != null )
            {
                super.setIndentationLeft( marginLeft );
            }
            Float marginRight = paragraphProperties.getMarginRight();
            if ( marginRight != null )
            {
                super.setIndentationRight( marginRight );
            }
            Float marginTop = paragraphProperties.getMarginTop();
            if ( marginTop != null )
            {
                super.setSpacingBefore( marginTop );
            }
            Float marginBottom = paragraphProperties.getMarginBottom();
            if ( marginBottom != null )
            {
                super.setSpacingAfter( marginBottom );
            }

            // first line indentation
            Boolean autoTextIndent = paragraphProperties.getAutoTextIndent();
            if ( Boolean.TRUE.equals( autoTextIndent ) )
            {
                float fontSize = font != null ? font.getCalculatedSize() : Font.DEFAULTSIZE;
                super.setFirstLineIndent( 1.3f * fontSize );
            }
            else
            {
                Float textIndent = paragraphProperties.getTextIndent();
                if ( textIndent != null )
                {
                    super.setFirstLineIndent( textIndent );
                }
            }

            // line height
            StyleLineHeight lineHeight = paragraphProperties.getLineHeight();
            if ( lineHeight != null && lineHeight.getLineHeight() != null )
            {
                if ( lineHeight.isLineHeightProportional() )
                {
                    super.setMultipliedLeading( lineHeight.getLineHeight() );
                }
                else
                {
                    super.setLeading( lineHeight.getLineHeight() );
                }
            }

            // keep together on the same page
            Boolean keepTogether = paragraphProperties.getKeepTogether();
            if ( keepTogether != null )
            {
                super.setKeepTogether( keepTogether );
            }

            // background color
            Color backgroundColor = paragraphProperties.getBackgroundColor();
            if ( backgroundColor != null && !Color.WHITE.equals( backgroundColor ) )
            {
                super.setBackgroundColor( backgroundColor );
            }

            // border
            StyleBorder border = paragraphProperties.getBorder();
            if ( border != null && !border.isNoBorder() )
            {
                StyleUtils.applyStyles( border, getWrapperCell() );
            }

            // border-left
            StyleBorder borderLeft = paragraphProperties.getBorderLeft();
            if ( borderLeft != null && !borderLeft.isNoBorder() )
            {
                StyleUtils.applyStyles( borderLeft, getWrapperCell() );
            }

            // border-right
            StyleBorder borderRight = paragraphProperties.getBorderRight();
            if ( borderRight != null && !borderRight.isNoBorder() )
            {
                StyleUtils.applyStyles( borderRight, getWrapperCell() );
            }

            // border-top
            StyleBorder borderTop = paragraphProperties.getBorderTop();
            if ( borderTop != null && !borderTop.isNoBorder() )
            {
                StyleUtils.applyStyles( borderTop, getWrapperCell() );
            }

            // border-bottom
            StyleBorder borderBottom = paragraphProperties.getBorderBottom();
            if ( borderBottom != null && !borderBottom.isNoBorder() )
            {
                StyleUtils.applyStyles( borderBottom, getWrapperCell() );
            }
        }
    }

    private void handleBreak( StyleBreak styleBreak )
    {
        if ( styleBreak.isColumnBreak() || styleBreak.isPageBreak() )
        {
            IBreakHandlingContainer b = StylableDocumentSection.getIBreakHandlingContainer( parent );
            if ( b != null )
            {
                if ( styleBreak.isColumnBreak() )
                {
                    b.columnBreak();
                }
                else if ( styleBreak.isPageBreak() )
                {
                    b.pageBreak();
                }
            }
        }
    }

    public Style getLastStyleApplied()
    {
        return lastStyleApplied;
    }

    public IStylableContainer getParent()
    {
        return parent;
    }

    public StylableDocument getOwnerDocument()
    {
        return ownerDocument;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Element getElement()
    {
        if ( !elementPostProcessed )
        {
            elementPostProcessed = true;

            // add space if this paragraph is empty
            // otherwise it's height will be zero
            boolean empty = true;
            ArrayList<Chunk> chunks = getChunks();
            for ( Chunk chunk : chunks )
            {
                if ( chunk.getImage() == null && chunk.getContent() != null && chunk.getContent().length() > 0 )
                {
                    empty = false;
                    break;
                }
            }
            if ( empty )
            {
                super.add( new Chunk( "\u00A0" ) ); // non breaking space
            }

            // adjust line height and baseline
            if ( font != null && font.getBaseFont() != null )
            {
                // iText and open office computes proportional line height differently
                // [iText] line height = coefficient * font size
                // [open office] line height = coefficient * (font ascender + font descender + font extra margin)
                // we have to increase paragraph line height to generate pdf similar to open office document
                // this algorithm may be inaccurate if fonts with different multipliers are used in this paragraph
                float size = font.getSize();
                float ascender = font.getBaseFont().getFontDescriptor( BaseFont.AWT_ASCENT, size );
                float descender = -font.getBaseFont().getFontDescriptor( BaseFont.AWT_DESCENT, size ); // negative value
                float margin = font.getBaseFont().getFontDescriptor( BaseFont.AWT_LEADING, size );
                float multiplier = ( ascender + descender + margin ) / size;
                if ( multipliedLeading > 0.0f )
                {
                    setMultipliedLeading( getMultipliedLeading() * multiplier );
                }

                // iText seems to output text with baseline lower than open office
                // we raise all paragraph text by some amount
                // again this may be inaccurate if fonts with different size are used in this paragraph
                float itextdescender = -font.getBaseFont().getFontDescriptor( BaseFont.DESCENT, size ); // negative
                float textRise = itextdescender + getTotalLeading() - font.getSize() * multiplier;
                chunks = getChunks();
                for ( Chunk chunk : chunks )
                {
                    Font f = chunk.getFont();
                    if ( f != null )
                    {
                        // have to raise underline and strikethru as well
                        float s = f.getSize();
                        if ( f.isUnderlined() )
                        {
                            f.setStyle( f.getStyle() & ~Font.UNDERLINE );
                            chunk.setUnderline( s * 1 / 17, s * -1 / 7 + textRise );
                        }
                        if ( f.isStrikethru() )
                        {
                            f.setStyle( f.getStyle() & ~Font.STRIKETHRU );
                            chunk.setUnderline( s * 1 / 17, s * 1 / 4 + textRise );
                        }
                    }
                    chunk.setTextRise( chunk.getTextRise() + textRise );
                }
            }
        }
        return super.getElement();
    }

}