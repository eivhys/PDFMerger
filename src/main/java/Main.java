import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        try {
            System.out.println(merge(readFromFile()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Merges Base64 encoded content to a single PDF file and returns it Base64 encoded
     * @param content base64 strings of images / pdfs
     * @throws IOException occurs when the writing location is in use
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private static String merge(String[] content) throws IOException {
        ArrayList<PDDocument> documents = new ArrayList<>();
        for (String item:content) {
            // JVBER means the file is a PDF
            if (item.substring(0, 5).equals("JVBER")) {
                InputStream targetStream = new ByteArrayInputStream((item.getBytes()));
                InputStream decoded = java.util.Base64.getDecoder().wrap(targetStream);
                PDDocument doc = PDDocument.load(decoded);
                documents.add(doc);
            } else {
                // Pictures PNG/JPG

                // New document
                PDDocument document = new PDDocument();

                // New page
                PDPage page = new PDPage(PDRectangle.A4);

                document.addPage(page);
                // Loads image
                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                imageByte = decoder.decodeBuffer(item);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                BufferedImage image = ImageIO.read(bis);
                bis.close();
                PDImageXObject  pdImageXObject = LosslessFactory.createFromImage(document, image);
                PDPageContentStream contentStream = new PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        false
                );

                // Img ratio to make the img scale to fit page
                float ratio = image.getHeight() > image.getWidth() ?
                         page.getMediaBox().getHeight() / image.getHeight() :
                         page.getMediaBox().getWidth() / image.getWidth();

                // 1/20 margin to create some breathing room
                float marginX = image.getWidth() / 20;
                float marginY = image.getHeight() / 20;

                // Draw image with margin to make it look nice and ratio to make it scale to fill page
                //noinspection IntegerDivisionInFloatingPointContext
                contentStream.drawImage(pdImageXObject,
                        image.getWidth() > 200 + marginX / 2  ? marginX / 2 : 200 + marginX / 2 - image.getWidth() / 2,
                        image.getHeight() > 300 + marginY / 2  ? marginY / 2 : 300 + marginY / 2 - image.getHeight() / 2,
                        image.getWidth() * ratio - marginX,
                        image.getHeight() * ratio - marginY);
                contentStream.close();

                // Creates Byte Array OutputStream and adds it to document collection
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                document.save(baos);
                documents.add(document);
            }
        }

        // Merges all pages of documents
        PDDocument allDocs = new PDDocument();
        for (PDDocument doc : documents) {
            for (int p = 0; p < doc.getPages().getCount(); p++) {
                allDocs.addPage(doc.getPage(p));
            }
        }

        // Saves locally
        allDocs.save("c:\\test\\merged2.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        allDocs.save(baos);

        // Returns base64 string of merged documents
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static String[] readFromFile() {
        String fileName = "C:\\Users\\eivindh\\IdeaProjects\\PDFMerger\\docs.txt";
        Scanner sc = null;

        try {
            sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<String> lines = new ArrayList<>();

        //noinspection ConstantConditions
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            lines.add(line);
            System.out.println(line);
        }

        System.out.println("Finished reading lines from file");

        return lines.toArray(new String[0]);
    }
}