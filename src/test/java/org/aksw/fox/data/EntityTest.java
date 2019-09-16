package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class EntityTest {

  /**
   * Tests sorting
   */
  @Test
  public void comparable() {

    final Entity a = new Entity("Leipzig", EntityTypes.L, 0, "tool", 1);
    final Entity b = new Entity("Leipzig", EntityTypes.L, 0, "tool", 10);
    final Entity c = new Entity("Leipzig", EntityTypes.L, 0, "tool", 100);

    final List<Entity> sorted = Arrays.asList(c, a, b).stream()//
        .sorted().collect(Collectors.toList());

    int lastIndex = -1;
    for (final Entity se : sorted) {
      Assert.assertTrue(lastIndex < se.getBeginIndex());
      lastIndex = se.getBeginIndex();
    }
  }

  /**
   *
   */
  @Test
  public void equals() {

    final Entity leipzig_a = new Entity("Leipzig", EntityTypes.L, 0, "tool", -1);
    final Entity leipzig_b = new Entity("Leipzig", EntityTypes.L, 0, "tool", -1);

    Assert.assertTrue(leipzig_a.equals(leipzig_b));
    final Entity leipzig_c = new Entity("Leipzig", EntityTypes.O, 0, "tool", -1);

    final List<Entity> list = new ArrayList<>();
    list.add(leipzig_a);
    Assert.assertTrue(list.contains(leipzig_b));
    Assert.assertFalse(list.contains(leipzig_c));

    Assert.assertFalse(leipzig_a.equals(leipzig_c));

    final Entity leipzig_d = new Entity("An other", EntityTypes.O, 0, "tool", -1);

    Assert.assertFalse(leipzig_a.equals(leipzig_d));
    Assert.assertFalse(leipzig_c.equals(leipzig_d));

    final Set<Entity> set = new HashSet<>();
    set.add(leipzig_a);
    set.add(leipzig_b);
    Assert.assertTrue(set.size() == 1);

    set.add(leipzig_c);
    set.add(leipzig_c);
    set.add(leipzig_d);
    Assert.assertTrue(set.size() == 3);
  }
}
