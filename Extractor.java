package icd10;

import java.io.IOException;
import org.jsoup.nodes.Document;

public abstract class Extractor {
    
    public abstract void extract(Document document, Node node) throws IOException;

}
