package icd10;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public final class ICD10 {
    
    public static boolean quiet = false, demo = false;
    private static int fileCount = 0;
    
    private String urlFolder;
    private File outputFolder;
    private final Node root;
    
    public ICD10(Node root){
        this.root = root;
    }
    
    private String getFileName(String urlString){
        return urlString.substring(urlString.lastIndexOf('/') + 1);
    }
    
    private Document getDocument(String fileName) throws IOException{
        if(!quiet && ++fileCount % 100 == 0) System.out.println("" + fileCount + " documents have been read...");
        
        File outputFile = new File(outputFolder, fileName.replaceAll("\\*", "star"));
        
        if(outputFile.exists()){
            //if(!quiet) System.out.println("Loading " + outputFile.getAbsolutePath() + " from hard drive...");
            return Jsoup.parse(outputFile, "UTF-8");
            
        }else{
            String urlString = urlFolder + fileName;
            //if(!quiet) System.out.println("Downloading and saving " + urlString + "...");
            
            BufferedReader in = new BufferedReader(new InputStreamReader((new URL(urlString)).openStream()));
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
            
            String line, documentAsString = "";
            while((line = in.readLine()) != null){
                out.write(line + "\n");
                documentAsString += line + "\n";
            }
            
            in.close();
            out.close();
            
            return Jsoup.parse(documentAsString);
        }
    }
    
    public ICD10(String urlString, File outputFolder) throws IOException{
        urlFolder = urlString.substring(0, urlString.lastIndexOf('/') + 1);
        this.outputFolder = outputFolder;        
        fileCount = 0;
        
        //if(!quiet) System.out.println("Reading tree from documents...");
        
        Extractor extractor1 = new Extractor(){
            @Override
            public void extract(Document document, Node node) throws IOException{
                int i=0;
                String childFileName;
                for(Element e : document.getElementsContainingText("Kapitel")){
                    if(e.tagName().toLowerCase().equals("tr")){
                        node.addChild(new Node(
                            e.child(1).text(),
                            (childFileName = e.child(1).child(0).attr("href")),
                            getDocument(childFileName)
                        ));
                        if(demo && ++i >= 3) break;
                    }
                }
            }
        };
        
        Extractor extractor2 = new Extractor(){
            @Override
            public void extract(Document document, Node node) throws IOException {
                Elements tablebodies, tables;
                Element table;
                if(document != null && !(tablebodies = document.getElementsByClass("tablebody")).isEmpty() && (tables = tablebodies.get(0).getElementsByTag("table")).size() > 1){
                    table = tables.get(1);
                }else{
                    return;
                }

                Node child = new Node("titel");
                for(TextNode textNode : table.child(0).child(0).child(1).textNodes()){
                    child.addChild(new Node(textNode.text()));
                }
                node.addChild(child);

                for(Element tr : table.child(0).children()){
                    switch(tr.child(1).text()){
                        case "Inkl.:":
                            node.addChild(new Node(
                                "inklusive",
                                new Node(tr.child(2).text())
                            ));
                            break;
                        case "Exkl.:":
                            node.addChild(new Node(
                                "exklusive",
                                new Node(tr.child(2).text())
                            ));
                            break;
                        case "Info:":
                            int i=0;
                            String childFileName;
                            for(Element a : tr.child(2).getElementsByTag("a")){
                                child = new Node(
                                    a.text(),
                                    (childFileName = a.attr("href")),
                                        getDocument(childFileName)
                                );
                                node.addChild(child);
                                if(demo && ++i >= 3) break;
                            }
                            break;
                    }
                }
            }
        };

        Extractor extractor3 = new Extractor() {
            @Override
            public void extract(Document document, Node node) throws IOException {
                Elements tablebodies, tables;
                Element table;
                if(document != null && !(tablebodies = document.getElementsByClass("tablebody")).isEmpty() && (tables = tablebodies.get(0).getElementsByTag("table")).size() > 1){
                    table = tables.get(1);
                }else{
                    return;
                }
                
                Elements tableRows = table.child(0).children();
                int i;
                for(i=0; i<tableRows.size(); i++){
                    Element tr = tableRows.get(i);
                    if(!tr.child(0).getElementsByClass("code").isEmpty()){
                        node.addChild(new Node(
                            "titel",
                            new Node(tr.child(1).text())
                        ));
                        break;
                    }
                }
                
                Node subNode = null;
                for(int j=i+1; j<tableRows.size(); j++){
                    Element tr = tableRows.get(j);
                    
                    if(tr.child(0).text().isEmpty()){
                        if(tr.child(1).text().equals("Info:")){
                            (subNode == null ? node : subNode).addChild(new Node(
                                "info",
                                Arrays.stream(tr.child(2).html().split("<br/?>")).map(s -> new Node(s.replaceAll("<[^<]+>", ""))).toArray(Node[]::new)
                            ));
                        }else if(tr.child(1).text().equals("Inkl.:")){
                            (subNode == null ? node : subNode).addChild(new Node(
                                "inklusive",
                                Arrays.stream(tr.child(2).html().split("<br/?>")).map(s -> new Node(s.replaceAll("<[^<]+>", ""))).toArray(Node[]::new)
                            ));
                        }else if(tr.child(1).text().equals("Exkl.:")){
                            (subNode == null ? node : subNode).addChild(new Node(
                                "exklusive",
                                Arrays.stream(tr.child(2).html().split("<br/?>")).map(s -> new Node(s.replaceAll("<[^<]+>", ""))).toArray(Node[]::new)
                            ));
                        }
                        
                    }else if(!tr.child(0).text().isEmpty()){
                        subNode = new Node(
                            tr.child(0).text(),
                            new Node(
                                "titel",
                                new Node(tr.child(1).text())
                            )
                        );
                        node.addChild(subNode);
                    }
                }
                
            }
        };
        
        String rootFileName = getFileName(urlString);
        root = new Node(
            rootFileName.substring(0, rootFileName.length() - 5),
            rootFileName,
            getDocument(rootFileName)
        );
        root.processDocument(extractor1);
        
        for(Node n1 : root.getChildren()){
            n1.processDocument(extractor2);
            
            for(Node n2 : n1.getChildren()){
                n2.processDocument(extractor2);
                
                for(Node n3 : n2.getChildren()){
                    n3.processDocument(extractor3);
                }
            }
        }
        
        if(!quiet) System.out.println("...Finished reading " + fileCount + " documents");
    }    
    
    @Override
    public String toString(){
        return root.toString();
    }
    
    public void exportToFile(File outputFile) throws IOException{
        root.exportToFile(outputFile);
    }
    
    public static ICD10 importFromFile(File inputFile) throws IOException{
        Node root = Node.importFromFile(inputFile);
        ICD10 icd = new ICD10(root);
        return icd;
    }
    
    

    public static void main(String[] args) {
        String urlString = "http://www.icd-code.de/icd/code/ICD-10-GM.html";
        
        String outputFolderString = "./icd10";
        for(int i=0; i<args.length; i++){
            String arg = args[i];
            if(arg.equals("-d")){
                demo = true;
            }else if(arg.equals("-q")){
                quiet = true;
            }else if(arg.equals("-o") && args.length >= i + 2){
                outputFolderString = args[i + 1];
            }
        }
        File outputFolder = new File(outputFolderString);
        if(!quiet) System.out.println("Using " + outputFolderString + " as output folder");
    
        try{
            if(!outputFolder.exists()) outputFolder.mkdir();
            
            ICD10 icd;
            File xmlFile = new File(outputFolder, "icd10.xml");
            
            if(xmlFile.exists()){
                if(!quiet) System.out.println("Importing ICD 10 from " + xmlFile.getAbsolutePath() + "...");
                icd = ICD10.importFromFile(xmlFile);
                if(!quiet) System.out.println("...Finised importing ICD 10 from " + xmlFile.getAbsolutePath());
                
            }else{
                if(!quiet) System.out.println("Downloading ICD 10 from " + urlString + "...");
                File htmlFolder = new File(outputFolder, "html");
                if(!htmlFolder.exists()) htmlFolder.mkdir();
                icd = new ICD10(urlString, htmlFolder);
                if(!quiet) System.out.println("...Finished downloading ICD 10 from " + urlString);
                
                if(!quiet) System.out.println("Exporting ICD 10 to " + xmlFile.getAbsolutePath() + "...");
                icd.exportToFile(xmlFile);
                if(!quiet) System.out.println("...Finised exporting ICD 10 to " + xmlFile.getAbsolutePath());
            }
            
            System.out.println("\nICD 10 as XML:\n\n" + icd);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
}