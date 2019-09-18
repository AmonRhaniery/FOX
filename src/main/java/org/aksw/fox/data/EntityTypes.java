package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.aksw.fox.data.encode.BILOUEncoding;

/**
 *
 * @author rspeck
 *
 */
public class EntityTypes {

  public static final String L = "LOCAL";
  public static final String O = "ORGANIZACAO";
  public static final String P = "PESSOA";
  public static final String NA = BILOUEncoding.O;

  public static final List<String> AllTypesList = new ArrayList<>(//
      new TreeSet<>(Arrays.asList(L, O, P, NA))//
  );
}
