package org.aksw.fox.tools.ner.pt;

import java.util.Properties;

import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.tools.ner.common.StanfordCommon;

/**
 * @author Stefan Heid
 */
public class StanfordPT extends StanfordCommon {

    private static Properties props = new Properties();

    static {
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("ner.model", "/home/amon/PFC/fox230amon/FOX230/data/stanford/models/pt-ner-model-tolerance_1e-3.ser.gz");
    }

    public StanfordPT() {
        super(props);
        entityClasses.put("Organizacao", EntityTypes.O);
        entityClasses.put("Localizacao", EntityTypes.L);
        entityClasses.put("Pessoa", EntityTypes.P);
        entityClasses.put("O", BILOUEncoding.O);
    }
}
