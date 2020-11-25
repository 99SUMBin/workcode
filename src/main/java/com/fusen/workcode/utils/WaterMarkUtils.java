package com.fusen.workcode.utils;

import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author: WBin
 * @create: 2020-08-21 14:51
 * @description: 给PDF文件添加水印的工具类
 */
public class WaterMarkUtils {
    private static Logger log = LoggerFactory.getLogger(WaterMarkUtils.class);
    //水印起点位置
    private static int interval = 10;


    public static void textWaterMark(String inputFile, String outputFile, String waterMarkName) {
        PdfStamper stamper = null;
        PdfReader reader = null;
        try {
            reader = new PdfReader(inputFile);
            stamper = new PdfStamper(reader, new FileOutputStream(outputFile));

            BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);

            Rectangle pageRect = null;
            PdfGState gs = new PdfGState();
            //设置水印文字的透明度,数值越小越透明
            gs.setFillOpacity(0.3f);
//            gs.setStrokeOpacity(0.8f);
            int total = reader.getNumberOfPages() + 1;

            JLabel label = new JLabel();
            FontMetrics metrics;
            int textH = 0;
            int textW = 0;
            label.setText(waterMarkName);
            metrics = label.getFontMetrics(label.getFont());
            //水印内容高度
            textH = metrics.getHeight();
            //水印内容宽度
            textW = metrics.stringWidth(label.getText());
            System.out.println("文字高度=" + textH + ",文字宽度=" + textW);
            PdfContentByte under;
            for (int i = 1; i < total; i++) {
                pageRect = reader.getPageSizeWithRotation(i);
                //在内容上方添加
//                under = stamper.getOverContent(i);
                // 在内容下方加水印
                under = stamper.getUnderContent(i);
                //水印颜色(默认是灰色)
//                under.setRGBColorFill(220, 20, 60);
                under.saveState();
                under.setGState(gs);
                under.beginText();
                //设置水印的大小
                under.setFontAndSize(base, 40);
                float pageH = pageRect.getHeight();
                float pageW = pageRect.getWidth();
                System.out.println("页面高度=" + pageH + ",页面宽度=" + pageW);
                // 水印文字成30度角倾斜
                /*for (int height = interval + textH; height < pageH; height = height + textH * 4) {
                    for (int width = interval + textW; width < pageW; width = width + textW * 2) {
                        //从左到右,从下到上设置多行水印
                        under.showTextAligned(Element.ALIGN_LEFT, waterMarkName, width - textW, height - textH, 30);
//                        under.showTextAligned(Element.ALIGN_CENTER, waterMarkName, width - textW, height - textH, 30);
                    }
                }*/
                int w = 0;
                for (int height = interval+textH; height < pageH; height = height + (int)pageH/3) {
                    sign:for (int width = interval+textW/2; width < pageW; width = width + (int)pageW/3) {
                        //从左到右,从下到上设置多行水印
                        if (w>=width){
                            continue;
                        }
                        System.out.println(height+","+width);
                        under.showTextAligned(Element.ALIGN_LEFT, waterMarkName, width, height, 30);
                        w = width;
                        break sign;
//                        under.showTextAligned(Element.ALIGN_CENTER, waterMarkName, width - textW, height - textH, 30);
                    }
                }
                // 添加水印文字
                under.endText();
            }

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(),e);
            }
        } finally {
            try {
                if (stamper!=null) {
                    stamper.close();
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(),e);
                }
            }
            if (reader!=null) {
                reader.close();
            }
        }
    }



    /**
     * 给pdf设置图片水印
     * @param pdfFileUrl 文件下载地址
     * @param pdfFileName 生成文件的文件名(需要后缀)
     * @param imagePath 图片水印地址
     * @param uploadUrl 上传文件服务器地址
     * @return
     */
    public static String imageWatermark(String pdfFileUrl, String pdfFileName, String imagePath, String uploadUrl) {
        String response = null;
        PdfStamper stamper = null;
        PdfReader reader = null;
        HttpURLConnection conn = null;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            conn = (HttpURLConnection) new URL(pdfFileUrl).openConnection();
            inputStream = conn.getInputStream();
            reader = new PdfReader(inputStream);
            File file = new File(pdfFileName);
            fileOutputStream = new FileOutputStream(file);
            stamper = new PdfStamper(reader, fileOutputStream);

            com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(new URL(imagePath));

            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++) {
                com.itextpdf.text.Rectangle page = reader.getPageSizeWithRotation(i);
                float pageW = page.getWidth();
                //在内容上方
//                PdfContentByte pdfContentByte = stamper.getOverContent(i);
                //在内容下方
                PdfContentByte pdfContentByte = stamper.getUnderContent(i);
//                设置了透明度
                PdfGState gs1 = new PdfGState();
                gs1.setFillOpacity(1f);
                pdfContentByte.setGState(gs1);
                //缩放图片
                image.scalePercent(50);
                float textWidth = image.getPlainWidth();
                //设置图片的位置(x轴数值越大越靠右,y轴数值越大越靠上)
                image.setAbsolutePosition(pageW - textWidth, 0);
                pdfContentByte.addImage(image);

            }
            //stamper必须要先关闭,否则上传到文件服务器的文件会异常
            try {
                if (stamper != null) {
                    stamper.close();
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(),e);
                }
            }
            //上传文件
            response =  HttpClientUtils.uploadFile(uploadUrl, file);
            if (file.exists()){
                file.delete();
            }
            return response;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(),e);
            }
            return "服务异常";
        } finally {
            try {
                if (stamper != null) {
                    stamper.close();
                }
            }catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(),e);
                }
            }
            if (reader!=null) {
                reader.close();
            }
            try {
                if (inputStream!=null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(),e);
                }
            }
            if (conn!=null){
                conn.disconnect();
            }
        }
    }





    /*public static void setExcelWaterMark(String inputPath, String outPath, String markStr) throws Exception {
        //读取excel文件
        Workbook wb = null;

        wb = new XSSFWorkbook(new FileInputStream(inputPath));

        //设置水印图片路径
        String imgPath = "C:/Users/JKH/Desktop/model/picture/" + UUID.randomUUID().toString().replace("-", "") + ".png";
        try {
            createWaterMarkImage(markStr, imgPath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //获取excel sheet个数
        int sheets = wb.getNumberOfSheets();
        //循环sheet给每个sheet添加水印
        for (int i = 0; i < sheets; i++) {
            Sheet sheet = wb.getSheetAt(i);
            //excel加密只读
//            sheet.protectSheet(UUID.randomUUID().toString());
            //获取excel实际所占行
            int row = 0;
            row = sheet.getFirstRowNum() + sheet.getLastRowNum();
            //获取excel实际所占列
            int cell = 0;
            if(null != sheet.getRow(sheet.getFirstRowNum())) {
                cell = sheet.getRow(sheet.getFirstRowNum()).getLastCellNum() + 1;
            }
            System.out.println("sheet"+(i+1)+"的实际行数="+row+",实际列数="+cell);

            //根据行与列计算实际所需多少水印
            try {
                putWaterRemarkToExcel(wb, sheet, imgPath, 0, 0, 5, 5, cell / 5 + 1, row / 5 + 1, 0, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            wb.write(os);
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] content = os.toByteArray();
        // Excel文件生成后存储的位置。
        File file = new File(outPath);
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(content);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File imageTempFile = new File(imgPath);
        if(imageTempFile.exists()) {
            imageTempFile.delete();
        }
    }*/

    //添加水印(图片格式)到excel
    /*public static void putWaterRemarkToExcel(Workbook wb, Sheet sheet, String waterRemarkPath, int startXCol,
                                             int startYRow, int betweenXCol, int betweenYRow, int XCount, int YCount, int waterRemarkWidth,
                                             int waterRemarkHeight) throws IOException {

        // 校验传入的水印图片格式
        if (!waterRemarkPath.endsWith("png") && !waterRemarkPath.endsWith("PNG")) {
            throw new RuntimeException("向Excel上面打印水印，目前支持png格式的图片。");
        }

        // 加载图片
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        InputStream imageIn = new FileInputStream(waterRemarkPath);
        //InputStream imageIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(waterRemarkPath);
        if (null == imageIn || imageIn.available() < 1) {
            throw new RuntimeException("向Excel上面打印水印，读取水印图片失败(1)。");
        }
        BufferedImage bufferImg = ImageIO.read(imageIn);
        if (null == bufferImg) {
            throw new RuntimeException("向Excel上面打印水印，读取水印图片失败(2)。");
        }
        ImageIO.write(bufferImg, "png", byteArrayOut);

        // 开始打水印
        Drawing drawing = sheet.createDrawingPatriarch();

        // 按照共需打印多少行水印进行循环
        for (int yCount = 0; yCount < YCount; yCount++) {
            // 按照每行需要打印多少个水印进行循环
            for (int xCount = 0; xCount < XCount; xCount++) {
                // 创建水印图片位置
                int xIndexInteger = startXCol + (xCount * waterRemarkWidth) + (xCount * betweenXCol);
                int yIndexInteger = startYRow + (yCount * waterRemarkHeight) + (yCount * betweenYRow);
                *//*
                 * 参数定义： 第一个参数是（x轴的开始节点）； 第二个参数是（是y轴的开始节点）； 第三个参数是（是x轴的结束节点）；
                 * 第四个参数是（是y轴的结束节点）； 第五个参数是（是从Excel的第几列开始插入图片，从0开始计数）；
                 * 第六个参数是（是从excel的第几行开始插入图片，从0开始计数）； 第七个参数是（图片宽度，共多少列）；
                 * 第8个参数是（图片高度，共多少行）；
                 *//*
                ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, xIndexInteger,
                        yIndexInteger, xIndexInteger + waterRemarkWidth, yIndexInteger + waterRemarkHeight);

                Picture pic = drawing.createPicture(anchor,
                        wb.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_PNG));
                pic.resize();
            }
        }
    }*/

    //根据文字生成图片水印
    /*public static void createWaterMarkImage(String content, String path) throws IOException {
        Integer width = 200;
        Integer height = 120;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);// 获取bufferedImage对象
        String fontType = "宋体";
        Integer fontStyle = Font.PLAIN;
        Integer fontSize = 30;
        Font font = new Font(fontType, fontStyle, fontSize);
        Graphics2D g2d = image.createGraphics(); // 获取Graphics2d对象
        image = g2d.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = image.createGraphics();
        //设置字体颜色和透明度,最后一个参数是透明度,0-255,数值越小越透明
        g2d.setColor(new Color(175, 175, 175, 150));
        g2d.setStroke(new BasicStroke(1)); // 设置字体
        g2d.setFont(font); // 设置字体类型  加粗 大小
        //设置倾斜度
        g2d.rotate(Math.toRadians(-30), (double) image.getWidth() / 2, (double) image.getHeight() / 2);
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(content, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;
        // 写入水印文字原定高度过小，所以累计写水印，增加高度
        g2d.drawString(content, (int) x, (int) baseY);
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        // 释放对象
        g2d.dispose();
        ImageIO.write(image, "png", new File(path));
    }*/





    /*public static void setWordWaterMark(String inputPath, String outPath, String markStr, String fileType) throws Exception {
        if ("docx".equals(fileType)) {
            File inputFile = new File(inputPath);
            XWPFDocument doc = null;
            try {
                doc = new XWPFDocument(new FileInputStream(inputFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            XWPFParagraph paragraph = doc.createParagraph();
            //XWPFRun run=paragraph.createRun();
            //run.setText("The Body:");
            // create header-footer
            XWPFHeaderFooterPolicy headerFooterPolicy = doc.getHeaderFooterPolicy();
            if (headerFooterPolicy == null) headerFooterPolicy = doc.createHeaderFooterPolicy();
            // create default Watermark - fill color black and not rotated
            headerFooterPolicy.createWatermark(markStr);
            // get the default header
            // Note: createWatermark also sets FIRST and EVEN headers
            // but this code does not updating those other headers
            XWPFHeader header = headerFooterPolicy.getHeader(XWPFHeaderFooterPolicy.DEFAULT);
            paragraph = header.getParagraphArray(0);
            System.out.println(paragraph.getCTP().getRArray(0));
            System.out.println(paragraph.getCTP().getRArray(0).getPictArray(0));
            // get com.microsoft.schemas.vml.CTShape where fill color and rotation is set
            org.apache.xmlbeans.XmlObject[] xmlobjects = paragraph.getCTP().getRArray(0).getPictArray(0).selectChildren(
                    new javax.xml.namespace.QName("urn:schemas-microsoft-com:vml", "shape"));
            if (xmlobjects.length > 0) {
                com.microsoft.schemas.vml.CTShape ctshape = (com.microsoft.schemas.vml.CTShape) xmlobjects[0];
                // set fill color
                //ctshape.setFillcolor("#d8d8d8");
                ctshape.setFillcolor("#CC00FF");
                // set rotation
                ctshape.setStyle(ctshape.getStyle() + ";rotation:315");
                //System.out.println(ctshape);
            }
            File file = new File(outPath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                doc.write(new FileOutputStream(file));
                doc.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("doc".equals(fileType)) {

        }
    }*/

}
