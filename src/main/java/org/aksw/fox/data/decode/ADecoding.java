package org.aksw.fox.data.decode;

import java.util.List;

import org.aksw.fox.data.Entity;

public abstract class ADecoding {

  protected boolean isTokenbasedBILOU(final List<Entity> tokenBasedBILOU) {

    // tokenBasedBILOU.forEach(LOG::info);

    // TODO: check input encoding and chunking
    // TODO: add nothing tag between indices? TO makes thing really clear?

    /**
     * <code>
     final Set<Integer> add = new HashSet<>();
     int lastIndexEnd = -1;
     int listindex = 0;
     for (final Entity entity : tokenBasedBILOU) {
       if (lastIndexEnd < 0) {
         lastIndexEnd = Entity.getIndex(entity) + entity.getText().length();
       } else {
         final int currentIndex = Entity.getIndex(entity);
         if (currentIndex - lastIndexEnd > 1) {
           add.add(listindex);
         }
         lastIndexEnd = currentIndex + entity.getText().length();
       }
       listindex++;
     }
     int offset = 0;
     for (final Integer index : add) {
       if (index < tokenBasedBILOU.size()) {
         tokenBasedBILOU.add(index + offset, new Entity(",", BILOUEncoding.O,index + offset));
         offset++;
       }
     }
     // check if text contains space
     // check if type starts with BILOU tag
     // check if has single index
     // check if index is sorted
    </code>
     */
    // tokenBasedBILOU.forEach(LOG::info);
    return true;
  }
}
