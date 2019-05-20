import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            merge(readFromFile());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Merges Base64 encoded content to a single PDF file
     * @param content
     * @throws IOException
     */
    public static void merge(String[] content) throws IOException {
        // Merger for merging PDF files
        PDFMergerUtility merger = new PDFMergerUtility();

        for (String item:content) {
            InputStream decoded = null;
            InputStream targetStream = new ByteArrayInputStream((item.getBytes()));
            // JVBER means the file is a PDF
            if (item.substring(0, 5).equals("JVBER")) {
                decoded = java.util.Base64.getDecoder().wrap(targetStream);
                merger.addSource(decoded);
            } else {
                // Pictures PNG/JPG
                // New document
                PDDocument document = new PDDocument();
                // New page
                PDPage page = new PDPage();
                document.addPage(page);
                // Adds image to page
                BufferedImage image = null;
                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                imageByte = decoder.decodeBuffer(item);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                image = ImageIO.read(bis);
                bis.close();
                PDImageXObject  pdImageXObject = LosslessFactory.createFromImage(document, image);
                PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
                contentStream.drawImage(pdImageXObject, 200, 300, image.getWidth() / 2, image.getHeight() / 2);
                contentStream.close();

                // Creates inputstream readable by the merger
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                document.save(baos);
                merger.addSource(new ByteArrayInputStream(baos.toByteArray()));
            }
        }
        // Merges and saves to file location
        merger.setDestinationFileName("c:\\test\\merged.pdf");
        try {
            merger.mergeDocuments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] readFromFile() {
        String fileName = "C:\\Users\\eivindh\\IdeaProjects\\PDFMerger\\docs.txt";
        Scanner sc = null;
        try {
            sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> lines = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            lines.add(line);
            System.out.println(line);
        }

        String[] arr = lines.toArray(new String[0]);
        return arr;
    }
}