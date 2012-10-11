package org.apache.poi.xwpf.converter.pdf.internal.elements;

import static org.apache.poi.xwpf.converter.core.utils.DxaUtil.dxa2points;

import java.io.OutputStream;

import javax.swing.text.Style;

import org.apache.poi.xwpf.converter.core.MasterPageManager;
import org.apache.poi.xwpf.converter.core.PageOrientation;
import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.core.utils.XWPFUtils;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import fr.opensagres.xdocreport.itext.extension.ExtendedDocument;
import fr.opensagres.xdocreport.itext.extension.ExtendedHeaderFooter;
import fr.opensagres.xdocreport.itext.extension.ExtendedPdfPTable;
import fr.opensagres.xdocreport.itext.extension.IITextContainer;
import fr.opensagres.xdocreport.itext.extension.IMasterPage;
import fr.opensagres.xdocreport.itext.extension.IMasterPageHeaderFooter;

public class StylableDocument
    extends ExtendedDocument
{

    private int titleNumber = 1;

    private StylableMasterPage activeMasterPage;

    private boolean masterPageJustChanged;

    private boolean documentEmpty = true;

    private PdfPTable layoutTable;

    private ColumnText text;

    private int colIdx;

    private MasterPageManager masterPageManager;

    public StylableDocument( OutputStream out )
        throws DocumentException
    {
        super( out );
    }

    public void addElement( Element element )
    {
        if ( !super.isOpen() )
        {
            super.open();
        }
        if ( masterPageJustChanged )
        {
            // master page was changed but there was no explicit page break
            pageBreak();
        }
        text.addElement( element );
        StylableDocumentSection.getCell( layoutTable, colIdx ).getColumn().addElement( element );
        simulateText();
        documentEmpty = false;
    }

    public void columnBreak()
    {
        if ( colIdx + 1 < layoutTable.getNumberOfColumns() )
        {
            setColIdx( colIdx + 1 );
            simulateText();
        }
        else
        {
            pageBreak();
        }
    }

    public void pageBreak()
    {
        if ( documentEmpty )
        {
            // no element was added - ignore page break
        }
        else if ( masterPageJustChanged )
        {
            // we are just after master page change
            // move to a new page but do not initialize column layout
            // because it is already done
            masterPageJustChanged = false;
            super.newPage();
        }
        else
        {
            // flush pending content
            flushTable();
            // check if master page change necessary
            // Style nextStyle = setNextActiveMasterPageIfNecessary();
            // document new page
            super.newPage();
            // initialize column layout for new page
            // if ( nextStyle == null )
            // {
            // ordinary page break
            layoutTable = StylableDocumentSection.cloneAndClearTable( layoutTable, false );
            // }
            // else
            // {
            // // page break with new master page activation
            // // style changed so recreate table
            // layoutTable =
            // StylableDocumentSection.createLayoutTable( getPageWidth(), getAdjustedPageHeight(), nextStyle );
            // }
            setColIdx( 0 );
            simulateText();
        }
    }

    @Override
    public boolean newPage()
    {
        throw new XWPFConverterException( "internal error - do not call newPage directly" );
    }

    @Override
    public void close()
    {
        flushTable();
        super.close();
    }

    public float getWidthLimit()
    {
        PdfPCell cell = StylableDocumentSection.getCell( layoutTable, colIdx );
        return cell.getRight() - cell.getPaddingRight() - cell.getLeft() - cell.getPaddingLeft();
    }

    public float getHeightLimit()
    {
        // yLine is negative
        return StylableDocumentSection.getCell( layoutTable, colIdx ).getFixedHeight() + text.getYLine();
    }

    public float getPageWidth()
    {
        return right() - left();
    }

    private float getAdjustedPageHeight()
    {
        // subtract small value from height, otherwise table breaks to new page
        return top() - bottom() - 0.001f;
    }

    private void setColIdx( int idx )
    {
        colIdx = idx;
        PdfPCell cell = StylableDocumentSection.getCell( layoutTable, colIdx );
        text.setSimpleColumn( cell.getLeft() + cell.getPaddingLeft(), -getAdjustedPageHeight(),
                              cell.getRight() - cell.getPaddingRight(), 0.0f );
        cell.setColumn( ColumnText.duplicate( text ) );
    }

    private void simulateText()
    {
        int res = 0;
        try
        {
            res = text.go( true );
        }
        catch ( DocumentException e )
        {
            throw new XWPFConverterException( e );
        }
        if ( ColumnText.hasMoreText( res ) )
        {
            // text does not fit into current column
            // split it to a new column
            columnBreak();
        }
    }

    public StylableParagraph createParagraph( IITextContainer parent )
    {
        return new StylableParagraph( this, parent );
    }

    public Paragraph createParagraph()
    {
        return createParagraph( (IITextContainer) null );
    }

    public Paragraph createParagraph( Paragraph title )
    {
        return new StylableParagraph( this, title, null );
    }

    // public StylablePhrase createPhrase( IITextContainer parent )
    // {
    // return new StylablePhrase( this, parent );
    // }
    //
    // public StylableAnchor createAnchor( IITextContainer parent )
    // {
    // return new StylableAnchor( this, parent );
    // }
    //
    // public StylableList createList( IITextContainer parent )
    // {
    // return new StylableList( this, parent );
    // }
    //
    // public StylableListItem createListItem( IITextContainer parent )
    // {
    // return new StylableListItem( this, parent );
    // }

    public StylableTable createTable( IITextContainer parent, int numColumns )
    {
        return new StylableTable( this, parent, numColumns );
    }

    public StylableTableCell createTableCell( IITextContainer parent )
    {
        return new StylableTableCell( this, parent );
    }

    public StylableTableCell createTableCell( IITextContainer parent, ExtendedPdfPTable table )
    {
        return new StylableTableCell( this, parent, table );
    }

    @Override
    public void setActiveMasterPage( IMasterPage m )
    {
        StylableMasterPage masterPage = (StylableMasterPage) m;
        if ( activeMasterPage != null && XWPFUtils.isContinuousSection( masterPage.getSectPr() ) )
        {
            // ignore section with "continous" section <w:sectPr><w:type w:val="continuous" />
            // because continous section applies changes (ex: modify width/height)
            // for the paragraph and iText cannot support that (a new page must be added to
            // change the width/height of the page).

            // see explanation about "continous" at http://officeopenxml.com/WPsection.php
            return;
        }

        // flush pending content
        flushTable();
        // activate master page in three steps

        // Style style = getStyleMasterPage( masterPage );
        // if ( style != null )
        // {
        // step 1 - apply styles like page dimensions and orientation
        this.applySectPr( masterPage.getSectPr() );
        // }
        // step 2 - set header/footer if any, it needs page dimensions from step 1
        super.setActiveMasterPage( masterPage );
        if ( activeMasterPage != null )
        {
            // set a flag used by addElement/pageBreak
            masterPageJustChanged = true;
        }
        activeMasterPage = masterPage;
        // step 3 - initialize column layout, it needs page dimensions which may be lowered by header/footer in step 2
        layoutTable = StylableDocumentSection.createLayoutTable( getPageWidth(), getAdjustedPageHeight(), (Style) null );
        text = StylableDocumentSection.createColumnText();
        setColIdx( 0 );
    }

    private void applySectPr( CTSectPr sectPr )
    {
        // Set page size
        CTPageSz pageSize = sectPr.getPgSz();
        Rectangle pdfPageSize = new Rectangle( dxa2points( pageSize.getW() ), dxa2points( pageSize.getH() ) );
        super.setPageSize( pdfPageSize );

        // Orientation
        PageOrientation orientation = XWPFUtils.getPageOrientation( pageSize.getOrient() );
        if ( orientation != null )
        {
            switch ( orientation )
            {
                case Landscape:
                    super.setOrientation( fr.opensagres.xdocreport.itext.extension.PageOrientation.Landscape );
                    break;
                case Portrait:
                    super.setOrientation( fr.opensagres.xdocreport.itext.extension.PageOrientation.Portrait );
                    break;
            }
        }

        // Set page margin
        CTPageMar pageMar = sectPr.getPgMar();
        if ( pageMar != null )
        {
            super.setOriginalMargins( dxa2points( pageMar.getLeft() ), dxa2points( pageMar.getRight() ),
                                      dxa2points( pageMar.getTop() ), dxa2points( pageMar.getBottom() ) );
        }

    }

    private void flushTable()
    {
        if ( layoutTable != null )
        {
            // force calculate height because it may be zero
            // and nothing will be flushed
            layoutTable.calculateHeights( true );
            try
            {
                super.add( layoutTable );
            }
            catch ( DocumentException e )
            {
                throw new XWPFConverterException( e );
            }
        }
    }

    @Override
    protected ExtendedHeaderFooter createExtendedHeaderFooter()
    {
        return new ExtendedHeaderFooter( this )
        {
            @Override
            public void onStartPage( PdfWriter writer, Document doc )
            {
                super.onStartPage( writer, doc );
                StylableDocument.this.onStartPage();
            }

            @Override
            protected float getHeaderY( IMasterPageHeaderFooter header )
            {
                Float headerY = ( (StylableHeaderFooter) header ).getY();
                if ( headerY != null )
                {
                    return document.getPageSize().getHeight() - headerY;
                }
                return super.getHeaderY( header );
            }

            @Override
            protected float getFooterY( IMasterPageHeaderFooter footer )
            {
                Float footerY = ( (StylableHeaderFooter) footer ).getY();
                if ( footerY != null )
                {
                    return document.getOriginMarginBottom() + footerY;
                }
                return super.getFooterY( footer );
            }
        };
    }

    protected void onStartPage()
    {
        masterPageManager.onNewPage();
    }

    public void setMasterPageManager( MasterPageManager masterPageManager )
    {
        this.masterPageManager = masterPageManager;
    }
}