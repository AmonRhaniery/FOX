package org.aksw.fox.nerlearner.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.data.Entity;
import org.aksw.fox.nerlearner.TokenManager;
import org.aksw.fox.utils.FoxTextUtil;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;


/**
 * 
 * Classe que le os conjuntos de treino de input
 * @author NataliaGodot
 * 
 */

public class HaremPtTrainerReader extends ANERReader {

    //Funcao main usada para testar a classe
    public static void main(String[] aa) throws Exception {

        final TrainingInputReader trainingInputReader = new TrainingInputReader();
        trainingInputReader.initFiles("input/4");
    
        LOG.info("input: \n" + trainingInputReader.input());
        LOG.info("oracle: ");
        trainingInputReader.getEntities().entrySet().forEach(LOG::info);
    }


    /**
   *
   * @return
   * @throws IOException
   */
  @Override
  public String input() {
    return input;
  }
    /**
     * 
     * Campos da classe 
     * 
     */

    private Map<String, String> entityClassesOracel = null; 
    protected StringBuffer            taggedInput = new StringBuffer();		//Texto lido dos arquivos de input
    protected String                  input       = "";
    protected HashMap<String, String> entities    = new HashMap<>();		//HashMap com entidade e classificacao

    /**
     * 
     * Metodos da classe 
     * 
     */

    //Construtor com parametro (array com strings que sao os caminhos dos inputs)
    public HaremPtTrainerReader(String[] inputPaths) throws IOException {
        initFiles(inputPaths);
    }

    public HaremPtTrainerReader() { }

    //Inicializa os arquivos de input a partir do caminho do diretorio que os contem
    public void initFiles(String folder) throws IOException {
        List<String> files = new ArrayList<>();

        File file = new File(folder);
        if (!file.exists()) {
            throw new IOException("Can't find directory.");
        } else {
            if (file.isDirectory()) {
                //Le todos os arquivos no diretorio
                for (File fileEntry : file.listFiles()) {
                    if (fileEntry.isFile() && !fileEntry.isHidden()) {
                        files.add(fileEntry.getAbsolutePath());
                    }
                }
            } else {
                throw new IOException("Input isn't a valid directory.");
            }
        }
	
	//Chama o metodo logo abaixo passando uma lista de strings com os caminhos dos arquivos de input
        initFiles(files.toArray(new String[files.size()]));
    }

    @Override
    public void initFiles(final String[] initFiles) throws IOException {
      super.initFiles(initFiles);
  
      readInputFromFiles();
      parse();
    }

    //Usada apenas na funcao main de teste da classe, para logar
    public String getInput() {
/*         // DEBUG
        if (LOG.isDebugEnabled())
            LOG.debug("Input:\n " + input + "\n"); */

        // INFO
        LOG.info("Input length: " + input.length());

        return input;
    }

    //Usado apenas na funcao main de teste da classe, para pegar as entidades nomeadas
    //AINDA NAO ALTERADO!!
    public HashMap<String, String> getEntities() {
        {
/*             // DEBUG
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting getEntities...");
                for (Entry<String, String> e : entities.entrySet())
                    LOG.debug(e.getKey() + " -> " + e.getValue());
            }  */
            // INFO 
            
            LOG.info("Oracle raw size: " + entities.size());
        }

        {
            // remove oracle entities aren't in input //aqui tem todas
            List<Entity> set = new ArrayList<>();

            for (Entry<String, String> oracleEntry : entities.entrySet())
                set.add(new Entity(oracleEntry.getKey(), oracleEntry.getValue(), "oracel"));

            // repair entities (use fox token)
            TokenManager tokenManager = new TokenManager(input);
            tokenManager.repairEntities(set);

            // use
            entities.clear();
            for (Entity e : set)
                entities.put(e.getText(), e.getType());
        }

        {
            // INFO
            LOG.info("oracle cleaned size: " + entities.size());
           
            int l = 0, o = 0, p = 0;
            for (Entry<String, String> e : entities.entrySet()) {
                if (e.getValue().equals(EntityTypes.L))
                    l++;
                if (e.getValue().equals(EntityTypes.O))
                    o++;
                if (e.getValue().equals(EntityTypes.P))
                    p++;
            }
            LOG.info("oracle :");
            LOG.info(l + " LOCs found");
            LOG.info(o + " ORGs found");
            LOG.info(p + " PERs found");

            l = 0;
            o = 0;
            p = 0;
            for (Entry<String, String> e : entities.entrySet()) {
                if (e.getValue().equals(EntityTypes.L))
                    l += e.getKey().split(" ").length;
                if (e.getValue().equals(EntityTypes.O))
                    o += e.getKey().split(" ").length;
                if (e.getValue().equals(EntityTypes.P))
                    p += e.getKey().split(" ").length;
            }
            LOG.info("oracle (token):");
            LOG.info(l + " LOCs found");
            LOG.info(o + " ORGs found");
            LOG.info(p + " PERs found");
            LOG.info(l + o + p + " total found");
        }

        return entities;
    }

    //Le o PREAMBLE ou o conteudo da tag TEXT para taggedInput
    //OBS: Para o nosso leitor tudo se encaixa como TEXT mas nao tem tag de abertura ou fechamento
    protected void readInputFromFiles() throws IOException {
/*         if (LOG.isDebugEnabled())
            LOG.debug("Starting readInputFromFiles..."); */

        for (File file : inputFiles) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            //boolean includeLine = false;
            while ((line = br.readLine()) != null) {
		/*
                // open
                if (line.contains("<PREAMBLE>")) {
                    includeLine = true;
                    line = line.substring(line.indexOf("<PREAMBLE>") + "<PREAMBLE>".length());
                } else if (line.contains("<TEXT>")) {
                    includeLine = true;
                    line = line.substring(line.indexOf("<TEXT>") + "<TEXT>".length());
                }
                // close
                if (includeLine) {
                    if (line.contains("</PREAMBLE>")) {
                        includeLine = false;
                        if (line.indexOf("</PREAMBLE>") > 0)
                            taggedInput.append(line.substring(0, line.indexOf("</PREAMBLE>")) + "\n");

                    } else if (line.contains("</TEXT>")) {
                        includeLine = false;
                        if (line.indexOf("</TEXT>") > 0)
                            taggedInput.append(line.substring(0, line.indexOf("</TEXT>")) + "\n");

                    } else {
                        taggedInput.append(line + "\n");
                    }
                }
		*/
		taggedInput.append(line + "\n");
            }
            br.close();
        }
    }

    /**
   * Gets the entity class for a oracel entity type/class.
   */
  public String oracel(final String tag) {
    if (entityClassesOracel == null) {
      entityClassesOracel = new HashMap<>();
      entityClassesOracel.put("ORGANIZACAO", EntityTypes.O);
      entityClassesOracel.put("LOCAL", EntityTypes.L);
      entityClassesOracel.put("PESSOA", EntityTypes.P);
    }
    final String t = entityClassesOracel.get(tag);
    return t == null ? BILOUEncoding.O : t;
  }

    //Le as entidades a partir do taggedInput
    //Principal funcao a ser alterada para o nosso leitor!
    protected String parse() {
/*         if (LOG.isDebugEnabled())
            LOG.debug("Starting parse ..."); */

        //input = taggedInput.toString().replaceAll("<p>|</p>", "");

    input = taggedInput.toString();
    input = input.replaceAll("<adv>", "");
    input = input.replaceAll("<adj>", "");
    input = input.replaceAll("<art>", "");
    input = input.replaceAll("<conj-c>", "");
    input = input.replaceAll("<conj-s>", "");
    input = input.replaceAll("<n>", "");
    input = input.replaceAll("<num>", "");
    input = input.replaceAll("<pp>", "");
    input = input.replaceAll("<prop>", "");
    input = input.replaceAll("<pron-det>", "");
    input = input.replaceAll("<pron-indp>", "");
    input = input.replaceAll("<pron-pers>", "");
    input = input.replaceAll("<prp>", "");
    input = input.replaceAll("<punc>", "");
    input = input.replaceAll("<v-fin>", "");
    input = input.replaceAll("<v-ger>", "");
    input = input.replaceAll("<v-inf>", "");
    input = input.replaceAll("<v-pcp>", "");
    
    //LOG.info("Input é "+input);

        while (true) {

            //int openTagStartIndex = input.indexOf("<ENAMEX");
            int openTagStartIndex = input.indexOf("<EM");
            if (openTagStartIndex == -1)
                break;
            else {
		int openTagCategStartIndex = input.indexOf("CATEG=\"", openTagStartIndex);
		int openTagCategCloseIndex = input.indexOf("\"", openTagCategStartIndex+"CATEG=\"".length()+1);
                int openTagCloseIndex = input.indexOf(">", openTagStartIndex);
                int closeTagIndex = input.indexOf("</EM>");

                try {
		    //String taggedWords pega todo o conteudo de uma tag <EM> ate seu fechamento em </EM>
                    String taggedWords = input.substring(openTagCloseIndex + 1, closeTagIndex);
                    //LOG.info("taggedWords "+ taggedWords);
                    //LOG.info("substring start indice "+openTagCategStartIndex+" "+input.substring(openTagCategStartIndex));
                    //LOG.info("substring stop indice"+openTagCategCloseIndex);
                    String categoriesString = input.substring(openTagCategStartIndex + "CATEG=\"".length(), openTagCategCloseIndex);
                    
                    //LOG.info("categoriesString "+ categoriesString);
                    String[] types = categoriesString.split("\\|");

                    //Relação entre as categorias dispostas no HAREM para as da ferramenta FOX
                    for (String type : types) {
                        if (!oracel(type).equals(BILOUEncoding.O)) {

                            String[] token = FoxTextUtil.getSentenceToken(taggedWords + ".");
                            String word = "";
                            for (String t : token) {

                                if (!word.isEmpty() && t.isEmpty()) {
                                    put(word, type);
                                    word = "";
                                } else
                                    word += t + " ";
                            }
                            if (!word.isEmpty())
                                put(word, type);
                        }
                    }
                    //limpar CATEG="..."
                    String escapedCategoriesString = "";
                    for (String type : types)
                        escapedCategoriesString += type + "|";

                    escapedCategoriesString = escapedCategoriesString.substring(0, escapedCategoriesString.length() - 1);
                    input = input.replaceFirst("CATEG=\"" + escapedCategoriesString + "\"", "");
                    input = input.replaceFirst("<EM ", "");
                    input = input.replaceFirst("</EM>", "");
                    //LOG.info("escapedCategoriesString: "+escapedCategoriesString);

                } catch (Exception e) {
                    LOG.error("\n", e);
                    //break;
                }
            }
        }
        //LOG.info("input after parse... "+input);
        LOG.info("Limpando tipo...");
        //limpar TIPO="..." >
        while (true) {
            int openTagStartIndex = input.indexOf("TIPO=\"");
            if (openTagStartIndex == -1) {
                break;
            } else {
                int openTagCloseIndex = input.indexOf("\"  >", openTagStartIndex+"TIPO=\"".length()+1);
                String tipo = input.substring(openTagStartIndex + "TIPO=\"".length(), openTagCloseIndex);
                //LOG.info("substituir: "+input.substring(openTagStartIndex -7, openTagCloseIndex));
                input = input.replaceFirst("TIPO=\"" + tipo + "\"  >", ""); 
            }
        } 
         //limpar ID="..."
        LOG.info("Limpando id...");
        while (true) {
            int openTagStartIndex = input.indexOf("ID=\"");
            if (openTagStartIndex == -1) {
                break;
            } else {
                int openTagCloseIndex = input.indexOf("\"", openTagStartIndex+"ID=\"".length()+1);
                String id = input.substring(openTagStartIndex + "ID=\"".length(), openTagCloseIndex);
                //LOG.info("id: "+id);
                input = input.replaceFirst("ID=\"" + id + "\"", "");
            }
        }
 
        
        /*while (true) {
            int openTagStartIndex = input.indexOf("<TIMEX");
            if (openTagStartIndex == -1) {
                break;
            } else {
                int openTagCloseIndex = input.indexOf(">", openTagStartIndex);
                String category = input.substring(openTagStartIndex + "<TIMEX TYPE=\"".length(), openTagCloseIndex - 1);
                input = input.replaceFirst("<TIMEX TYPE=\"" + category + "\">", "");
                input = input.replaceFirst("</TIMEX>", "");
            }
        }*/
        //LOG.info("Input final: "+input);
        input = input.trim();
        // input = input.replaceAll("``|''", "");
        // input = input.replaceAll("\\p{Blank}+", " ");
        // input = input.replaceAll("\n ", "\n");
        // input = input.replaceAll("\n+", "\n");
        // input = input.replaceAll("[.]+", ".");
        
        return input;
    }

    protected void put(String word, String classs) {
        word = word.trim();
        if (!word.isEmpty()) {
            if (entities.get(word) != null) {
                if (!entities.get(word).equals(classs) && !entities.get(word).equals(BILOUEncoding.O)) {
/*                     LOG.debug("Oracle with a token with diff. annos. No disamb. for now. Ignore token.");
                    LOG.debug(word + " : " + classs + " | " + entities.get(word));   */
                    entities.put(word, BILOUEncoding.O);
                }
            } else
                entities.put(word, classs);
        }
    }

}

