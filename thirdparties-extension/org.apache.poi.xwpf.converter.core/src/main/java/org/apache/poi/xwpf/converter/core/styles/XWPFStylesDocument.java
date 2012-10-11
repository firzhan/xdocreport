package org.apache.poi.xwpf.converter.core.styles;

import java.awt.Color;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphAlignmentValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphBackgroundColorValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphBorderBottomValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphBorderLeftValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphBorderRightValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphBorderTopValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphIndentationFirstLineValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphIndentationLeftValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphIndentationRightValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphSpacingAfterValueProvider;
import org.apache.poi.xwpf.converter.core.styles.pargraph.ParagraphSpacingBeforeValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunBackgroundColorValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunFontColorValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunFontFamilyValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunFontSizeValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunFontStyleBoldValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunFontStyleItalicValueProvider;
import org.apache.poi.xwpf.converter.core.styles.run.RunUnderlineValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableAlignmentValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableBordersValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableIndentationValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableMarginBottomValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableMarginLeftValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableMarginRightValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableMarginTopValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.TableWidthValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellBackgroundColorValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellBordersValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellGridSpanValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellMarginBottomValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellMarginLeftValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellMarginRightValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellMarginTopValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellTextDirectionValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellVerticalAlignmentValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.cell.TableCellWidthValueProvider;
import org.apache.poi.xwpf.converter.core.styles.table.row.TableRowHeightValueProvider;
import org.apache.poi.xwpf.converter.core.utils.TableHeight;
import org.apache.poi.xwpf.converter.core.utils.TableWidth;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocDefaults;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPrBase;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblStylePr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTextDirection;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc.Enum;

public class XWPFStylesDocument
{

    public static final Object EMPTY_VALUE = new Object();

    private final XWPFDocument document;

    private final Map<String, CTStyle> stylesByStyleId;

    private CTStyle defaultParagraphStyle;

    private CTStyle defaultTableStyle;

    private final Map<String, Object> values;

    private CTStyle defaultCharacterStyle;

    private CTStyle defaultNumberingStyle;

    private Map<XWPFTable, TableInfo> tableInfos;

    public XWPFStylesDocument( XWPFDocument document )
        throws XmlException, IOException
    {
        this( document, true );
    }

    public XWPFStylesDocument( XWPFDocument document, boolean lazyInitialization )
        throws XmlException, IOException
    {
        this.document = document;
        this.stylesByStyleId = new HashMap<String, CTStyle>();
        this.values = new HashMap<String, Object>();
        if ( lazyInitialization )
        {
            initialize();
        }
    }

    protected void initialize()
        throws XmlException, IOException
    {
        List<CTStyle> styles = document.getStyle().getStyleList();
        for ( CTStyle style : styles )
        {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff.Enum isDefault = style.getDefault();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.Enum type = style.getType();

            boolean isDefaultStyle =
                ( isDefault != null && isDefault.intValue() == org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff.INT_X_1 );
            if ( isDefaultStyle )
            {
                // default
                if ( type != null )
                {
                    switch ( type.intValue() )
                    {
                        case org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.INT_CHARACTER:
                            defaultCharacterStyle = style;
                            break;
                        case org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.INT_NUMBERING:
                            defaultNumberingStyle = style;
                            break;
                        case org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.INT_PARAGRAPH:
                            defaultParagraphStyle = style;
                            break;
                        case org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.INT_TABLE:
                            defaultTableStyle = style;
                            break;
                    }
                }

            }
            visitStyle( style, isDefaultStyle );
            stylesByStyleId.put( style.getStyleId(), style );
        }
    }

    protected void visitStyle( CTStyle style, boolean defaultStyle )
    {

    }

    public CTStyle getDefaultParagraphStyle()
    {
        return defaultParagraphStyle;
    }

    public CTStyle getStyle( String styleId )
    {
        return stylesByStyleId.get( styleId );
    }

    public CTDocDefaults getDocDefaults()
    {
        try
        {
            return document.getStyle().getDocDefaults();
        }
        catch ( XmlException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // -------------------- Paragraph

    /**
     * @param docxParagraph
     * @return
     */
    public Float getSpacingBefore( XWPFParagraph docxParagraph )
    {
        return ParagraphSpacingBeforeValueProvider.INSTANCE.getValue( docxParagraph, this );
    }

    public Float getSpacingBefore( CTPPr pPr )
    {
        return ParagraphSpacingBeforeValueProvider.INSTANCE.getValue( pPr );
    }

    public Float getSpacingAfter( XWPFParagraph docxParagraph )
    {
        return ParagraphSpacingAfterValueProvider.INSTANCE.getValue( docxParagraph, this );
    }

    public Float getSpacingAfter( CTPPr pPr )
    {
        return ParagraphSpacingAfterValueProvider.INSTANCE.getValue( pPr );
    }

    public Float getIndentationLeft( XWPFParagraph paragraph )
    {
        return ParagraphIndentationLeftValueProvider.INSTANCE.getValue( paragraph, this );
    }

    public Float getIndentationLeft( CTPPr pPr )
    {
        return ParagraphIndentationLeftValueProvider.INSTANCE.getValue( pPr );
    }

    public Float getIndentationRight( XWPFParagraph paragraph )
    {
        return ParagraphIndentationRightValueProvider.INSTANCE.getValue( paragraph, this );
    }

    public Float getIndentationRight( CTPPr pPr )
    {
        return ParagraphIndentationRightValueProvider.INSTANCE.getValue( pPr );
    }

    public Float getIndentationFirstLine( XWPFParagraph paragraph )
    {
        return ParagraphIndentationFirstLineValueProvider.INSTANCE.getValue( paragraph, this );
    }

    public Float getIndentationFirstLine( CTPPr pPr )
    {
        return ParagraphIndentationFirstLineValueProvider.INSTANCE.getValue( pPr );
    }

    public Color getBackgroundColor( XWPFParagraph paragraph )
    {
        return ParagraphBackgroundColorValueProvider.INSTANCE.getValue( paragraph, this );
    }

    public Color getBackgroundColor( CTPPr pPr )
    {
        return ParagraphBackgroundColorValueProvider.INSTANCE.getValue( pPr );
    }

    /**
     * @param paragraph
     * @return
     */
    public ParagraphAlignment getParagraphAlignment( XWPFParagraph paragraph )
    {
        return ParagraphAlignmentValueProvider.INSTANCE.getValue( paragraph, this );
    }

    public ParagraphAlignment getParagraphAlignment( CTPPr pPr )
    {
        return ParagraphAlignmentValueProvider.INSTANCE.getValue( pPr );
    }

    public CTBorder getBorderTop( XWPFParagraph docxParagraph )
    {
        return ParagraphBorderTopValueProvider.INSTANCE.getValue( docxParagraph, this );
    }

    public CTBorder getBorderTop( CTPPr pPr )
    {
        return ParagraphBorderTopValueProvider.INSTANCE.getValue( pPr );
    }

    public CTBorder getBorderBottom( XWPFParagraph docxParagraph )
    {
        return ParagraphBorderBottomValueProvider.INSTANCE.getValue( docxParagraph, this );
    }

    public CTBorder getBorderBottom( CTPPr pPr )
    {
        return ParagraphBorderBottomValueProvider.INSTANCE.getValue( pPr );
    }

    public CTBorder getBorderLeft( XWPFParagraph docxParagraph )
    {
        return ParagraphBorderLeftValueProvider.INSTANCE.getValue( docxParagraph, this );
    }

    public CTBorder getBorderLeft( CTPPr pPr )
    {
        return ParagraphBorderLeftValueProvider.INSTANCE.getValue( pPr );
    }

    public CTBorder getBorderRight( XWPFParagraph docxParagraph )
    {
        return ParagraphBorderRightValueProvider.INSTANCE.getValue( docxParagraph, this );
    }

    public CTBorder getBorderRight( CTPPr pPr )
    {
        return ParagraphBorderRightValueProvider.INSTANCE.getValue( pPr );
    }

    // -------------------- Run

    /**
     * @param run
     * @return
     */
    public String getFontFamily( XWPFRun run )
    {
        return RunFontFamilyValueProvider.INSTANCE.getValue( run, this );
    }

    public String getFontFamily( CTRPr rPr )
    {
        return RunFontFamilyValueProvider.INSTANCE.getValue( rPr );
    }

    public Float getFontSize( XWPFRun run )
    {
        return RunFontSizeValueProvider.INSTANCE.getValue( run, this );
    }

    public Float getFontSize( CTRPr rPr )
    {
        return RunFontSizeValueProvider.INSTANCE.getValue( rPr );
    }

    public Boolean getFontStyleBold( XWPFRun run )
    {
        return RunFontStyleBoldValueProvider.INSTANCE.getValue( run, this );
    }

    public Boolean getFontStyleBold( CTRPr rPr )
    {
        return RunFontStyleBoldValueProvider.INSTANCE.getValue( rPr );
    }

    public Boolean getFontStyleItalic( XWPFRun run )
    {
        return RunFontStyleItalicValueProvider.INSTANCE.getValue( run, this );
    }

    public Boolean getFontStyleItalic( CTRPr rPr )
    {
        return RunFontStyleItalicValueProvider.INSTANCE.getValue( rPr );
    }

    public Color getFontColor( XWPFRun run )
    {
        return RunFontColorValueProvider.INSTANCE.getValue( run, this );
    }

    public Color getFontColor( CTRPr rPr )
    {
        return RunFontColorValueProvider.INSTANCE.getValue( rPr );
    }

    public UnderlinePatterns getUnderline( CTRPr rPr )
    {
        return RunUnderlineValueProvider.INSTANCE.getValue( rPr );
    }

    public UnderlinePatterns getUnderline( XWPFRun run )
    {
        return RunUnderlineValueProvider.INSTANCE.getValue( run, this );
    }

    public Color getBackgroundColor( XWPFRun run )
    {
        return RunBackgroundColorValueProvider.INSTANCE.getValue( run, this );
    }

    public Color getBackgroundColor( CTRPr rPr )
    {
        return RunBackgroundColorValueProvider.INSTANCE.getValue( rPr );
    }

    // ------------------------ Table

    /**
     * @param table
     * @return
     */
    public TableWidth getTableWidth( XWPFTable table )
    {
        return TableWidthValueProvider.INSTANCE.getValue( table, this );
    }

    public TableWidth getTableWidth( CTTblPr tblPr )
    {
        return TableWidthValueProvider.INSTANCE.getValue( tblPr );
    }

    public TableWidth getTableWidth( CTTblPrBase tblPr )
    {
        return TableWidthValueProvider.INSTANCE.getValue( tblPr );
    }

    public ParagraphAlignment getTableAlignment( XWPFTable table )
    {
        return TableAlignmentValueProvider.INSTANCE.getValue( table, this );
    }

    public ParagraphAlignment getTableAlignment( CTTblPr tblPr )
    {
        return TableAlignmentValueProvider.INSTANCE.getValue( tblPr );
    }

    public ParagraphAlignment getTableAlignment( CTTblPrBase tblPr )
    {
        return TableAlignmentValueProvider.INSTANCE.getValue( tblPr );
    }

    public Float getTableIndentation( XWPFTable table )
    {
        return TableIndentationValueProvider.INSTANCE.getValue( table, this );
    }

    public Float getTableIndentation( CTTblPr tblPr )
    {
        return TableIndentationValueProvider.INSTANCE.getValue( tblPr );
    }

    public Float getTableIndentation( CTTblPrBase tblPr )
    {
        return TableIndentationValueProvider.INSTANCE.getValue( tblPr );
    }

    public CTTblBorders getTableBorders( XWPFTable table )
    {
        return TableBordersValueProvider.INSTANCE.getValue( table, this );
    }

    public CTTblBorders getTableBorders( CTTblPr tblPr )
    {
        return TableBordersValueProvider.INSTANCE.getValue( tblPr );
    }

    public CTTblBorders getTableBorders( CTTblPrBase tblPr )
    {
        return TableBordersValueProvider.INSTANCE.getValue( tblPr );
    }

    public Float getTableMarginTop( XWPFTable table )
    {
        return TableMarginTopValueProvider.INSTANCE.getValue( table, this );
    }

    public Float getTableMarginTop( CTTblPrBase tcPr )
    {
        return TableMarginTopValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableMarginBottom( XWPFTable table )
    {
        return TableMarginBottomValueProvider.INSTANCE.getValue( table, this );
    }

    public Float getTableMarginBottom( CTTblPrBase tcPr )
    {
        return TableMarginBottomValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableMarginLeft( XWPFTable table )
    {
        return TableMarginLeftValueProvider.INSTANCE.getValue( table, this );
    }

    public Float getTableMarginLeft( CTTblPrBase tcPr )
    {
        return TableMarginLeftValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableMarginRight( XWPFTable table )
    {
        return TableMarginRightValueProvider.INSTANCE.getValue( table, this );
    }

    public Float getTableMarginRight( CTTblPrBase tcPr )
    {
        return TableMarginRightValueProvider.INSTANCE.getValue( tcPr );
    }

    // ------------------------ Table row

    /**
     * @param row
     * @return
     */
    public TableHeight getTableRowHeight( XWPFTableRow row )
    {
        return TableRowHeightValueProvider.INSTANCE.getValue( row, this );
    }

    /**
     * @param cell
     * @return
     */
    public TableHeight getTableRowHeight( CTTrPr trPr )
    {
        return TableRowHeightValueProvider.INSTANCE.getValue( trPr );
    }

    // ------------------------ Table cell

    /**
     * @param cell
     * @return
     */
    public Enum getTableCellVerticalAlignment( XWPFTableCell cell )
    {
        return TableCellVerticalAlignmentValueProvider.INSTANCE.getValue( cell, this );
    }

    public Color getTableCellBackgroundColor( XWPFTableCell cell )
    {
        return TableCellBackgroundColorValueProvider.INSTANCE.getValue( cell, this );
    }

    public Color getTableCellBackgroundColor( CTTcPr tcPr )
    {
        return TableCellBackgroundColorValueProvider.INSTANCE.getValue( tcPr );
    }

    public BigInteger getTableCellGridSpan( XWPFTableCell cell )
    {
        return TableCellGridSpanValueProvider.INSTANCE.getValue( cell, this );
    }

    public BigInteger getTableCellGridSpan( CTTcPr tcPr )
    {
        return TableCellGridSpanValueProvider.INSTANCE.getValue( tcPr );
    }

    public TableWidth getTableCellWith( XWPFTableCell cell )
    {
        return TableCellWidthValueProvider.INSTANCE.getValue( cell, this );
    }

    public TableWidth getTableCellWith( CTTcPr tcPr )
    {
        return TableCellWidthValueProvider.INSTANCE.getValue( tcPr );
    }

    public CTTextDirection getTextDirection( XWPFTableCell cell )
    {
        return TableCellTextDirectionValueProvider.INSTANCE.getValue( cell, this );
    }

    public CTTextDirection getTextDirection( CTTcPr tcPr )
    {
        return TableCellTextDirectionValueProvider.INSTANCE.getValue( tcPr );
    }

    public CTTcBorders getTableCellBorders( XWPFTableCell cell )
    {
        return TableCellBordersValueProvider.INSTANCE.getValue( cell, this );
    }

    public CTTcBorders getTableCellBorders( CTTcPr tcPr )
    {
        return TableCellBordersValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableCellMarginTop( XWPFTableCell cell )
    {
        return TableCellMarginTopValueProvider.INSTANCE.getValue( cell, this );
    }

    public Float getTableCellMarginTop( CTTcPr tcPr )
    {
        return TableCellMarginTopValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableCellMarginBottom( XWPFTableCell cell )
    {
        return TableCellMarginBottomValueProvider.INSTANCE.getValue( cell, this );
    }

    public Float getTableCellMarginBottom( CTTcPr tcPr )
    {
        return TableCellMarginBottomValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableCellMarginLeft( XWPFTableCell cell )
    {
        return TableCellMarginLeftValueProvider.INSTANCE.getValue( cell, this );
    }

    public Float getTableCellMarginLeft( CTTcPr tcPr )
    {
        return TableCellMarginLeftValueProvider.INSTANCE.getValue( tcPr );
    }

    public Float getTableCellMarginRight( XWPFTableCell cell )
    {
        return TableCellMarginRightValueProvider.INSTANCE.getValue( cell, this );
    }

    public Float getTableCellMarginRight( CTTcPr tcPr )
    {
        return TableCellMarginRightValueProvider.INSTANCE.getValue( tcPr );
    }

    public CTStyle getDefaultCharacterStyle()
    {
        return defaultCharacterStyle;
    }

    public CTStyle getDefaultNumberingStyle()
    {
        return defaultNumberingStyle;
    }

    public CTStyle getDefaultTableStyle()
    {
        return defaultTableStyle;
    }

    public CTStyle getStyle( CTString basedOn )
    {
        if ( basedOn == null )
        {
            return null;
        }
        return getStyle( basedOn.getVal() );
    }

    public <T> T getValue( String key )
    {
        return (T) values.get( key );
    }

    public <T> void setValue( String key, T value )
    {
        values.put( key, value );
    }

    public TableCellInfo getTableCellInfo( XWPFTableCell cell )
    {
        XWPFTable table = cell.getTableRow().getTable();
        return getTableInfo( table ).getCellInfo( cell );
    }

    public TableInfo getTableInfo( XWPFTable table )
    {
        if ( tableInfos == null )
        {
            tableInfos = new HashMap<XWPFTable, TableInfo>();
        }
        TableInfo tableInfo = tableInfos.get( table );
        if ( tableInfo == null )
        {
            tableInfo = new TableInfo( table, this );
            tableInfos.put( table, tableInfo );
        }
        return tableInfo;
    }

    public CTTblStylePr getTableStyle( String tableStyleID,
                                       org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblStyleOverrideType.Enum type )
    {
        CTStyle style = getStyle( tableStyleID );
        if ( style == null )
        {
            return null;
        }
        // TODO cache it
        List<CTTblStylePr> tblStylePrs = style.getTblStylePrList();
        for ( CTTblStylePr tblStylePr : tblStylePrs )
        {
            if ( type.equals( tblStylePr.getType() ) )
            {
                return tblStylePr;
            }
        }
        return null;
    }

}