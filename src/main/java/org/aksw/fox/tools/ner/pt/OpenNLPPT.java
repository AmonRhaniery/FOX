package org.aksw.fox.tools.ner.pt;


import org.aksw.fox.tools.ner.common.OpenNLPCommon;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;


public class OpenNLPPT extends OpenNLPCommon {

    static final String[] modelPath = {"data/openNLP/pt-ner.bin"};

    public OpenNLPPT() {
        super(modelPath);
        entityClasses.put("Organizacao", EntityTypes.O);
        entityClasses.put("Localizacao", EntityTypes.L);
        entityClasses.put("Pessoa", EntityTypes.P);
        entityClasses.put("O", BILOUEncoding.O);
    }

}