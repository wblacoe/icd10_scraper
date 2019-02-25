package icd10;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;

public class Node {
    
    private String fileName;
    private Document document;
    
    private final String content;
    private final List<Node> children;
    
    private static int lineCount = 0;
    
    public Node(String content, String fileName, Document document) throws IOException{
        this.fileName = fileName;
        this.document = document;
        this.content = content;
        children = new LinkedList<>();
    }
    
    public Node(String content, Node... children){
        this.content = content;
        this.children = Arrays.stream(children).collect(Collectors.toList());
    }
    
    public Node(String content){
        this.content = content;
        children = new LinkedList<>();
    }
    
    public Document getDocument(){
        return document;
    }
    
    public void addChildAtFront(Node child){
        children.add(0, child);
    }
    
    public void addChild(Node child){
        children.add(child);
    }
    
    public List<Node> getChildren(){
        return children;
    }
    
    public void processDocument(Extractor ex) throws IOException{
        ex.extract(document, this);
    }
    
    public String toIndentedString(String indent){
        //String contentString = (content.length() > 20 ? content.substring(0, 20) + "..." : content);
        String s = indent + "[" + content + "]\n";
        
        for(Node child : children){
            s += child.toIndentedString(indent + "  ");
        }
        
        return s;
    }
    
    @Override
    public String toString(){
        return toIndentedString("");
    }
    
    private void exportToBufferedWriter(BufferedWriter out, String indent) throws IOException{
        out.write(indent + "<node>\n");
        if(fileName != null && !fileName.isEmpty()) out.write(indent + "  <url>" + fileName + "</url>\n");
        if(content != null && !content.isEmpty()) out.write(indent + "  <content>" + content + "</content>\n");
        
        for(Node child : children){
            child.exportToBufferedWriter(out, indent + "  ");
        }
        
        out.write(indent + "</node>\n");
    }
    
    public void exportToBufferedWriter(BufferedWriter out) throws IOException{
        exportToBufferedWriter(out, "");
    }
    
    public void exportToFile(File outputFile) throws IOException{
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
        exportToBufferedWriter(out);
        out.close();
    }
    
    public static Node importFromBufferedReader(BufferedReader in) throws IOException{
        String fileName = null, content = null;
        List<Node> children = new LinkedList<>();
        boolean appendLine = false;

        String line = null, newLine;
        while(true){
            newLine = in.readLine();
            if(newLine == null) break;
            if(!ICD10.quiet && ++lineCount % 10000 == 0) System.out.println("" + lineCount + " lines have been read so far...");
            
            if(appendLine){
                if(line == null){
                    line = newLine.trim();
                }else{
                    line += "\n" + newLine.trim();
                }
            }else{
                line = newLine.trim();
            }
            
            appendLine = false;
            int k;
            if(line.startsWith("<url>") && (k = line.indexOf("</url>")) > -1){
                fileName = line.substring(5, k);
            }else if(line.startsWith("<content>") && (k = line.indexOf("</content>")) > -1){
                content = line.substring(9, k);
            }else if(line.startsWith("<node>")){
                children.add(Node.importFromBufferedReader(in));
            }else if(line.equals("</node>")){
                break;
            }else{
                appendLine = true;
            }
            
        }
        
        Node r = new Node(content);
        if(fileName != null) r.fileName = fileName;
        for(Node child : children) r.addChild(child);
            
        return r;
    }
    
    public static Node importFromFile(File inputFile) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        String line = in.readLine();
        if(line == null || !line.trim().startsWith("<node")) return null;
        lineCount = 1;
        if(!ICD10.quiet) System.out.println("Importing tree from " + inputFile.getAbsolutePath() + "...");
        Node node = importFromBufferedReader(in);
        if(!ICD10.quiet) System.out.println("...Finished importing tree from " + inputFile.getAbsolutePath());
        in.close();
        return node;
    }

}