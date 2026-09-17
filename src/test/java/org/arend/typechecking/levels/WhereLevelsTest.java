package org.arend.typechecking.levels;

import org.arend.core.definition.Definition;
import org.arend.typechecking.TypeCheckingTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhereLevelsTest extends TypeCheckingTestCase {
  @Test
  public void whereFuncTest() {
    typeCheckModule("""
      \\func f.{p1,p2} (A : \\Type p1) => A
        \\where \\func g => \\Type p1
      """);
    assertEquals(2, getDefinition("f.g").getLevelParameters().size());
  }

  @Test
  public void whereDataTest() {
    typeCheckModule("""
      \\data D.{p1,p2} | con
        \\where \\func g => \\Type p2
      """);
    assertEquals(2, getDefinition("D.g").getLevelParameters().size());
  }

  @Test
  public void whereClassTest() {
    typeCheckModule("""
      \\record R.{p1,p2}
        \\where \\func g => \\Type p1
      """);
    assertEquals(2, getDefinition("R.g").getLevelParameters().size());
  }

  @Test
  public void useTest() {
    typeCheckModule(
      "\\data D.{p1,p2} (A : \\Type p2) | con Nat\n" +
      "  \\where \\use \\coerce test {A : \\Type p1} (n : Nat) : D A => con n", -1);
    Definition def = getDefinition("D.test");
    System.err.println("USE LEVELS: " + (def == null ? "null" : def.getLevelParameters().size()));
    System.err.println("USE ERRORS: " + getAllErrors());
  }

  @Test
  public void useOwnLevels() {
    resolveNamesModule(
      "\\data D.{p1,p2} (A : \\Type p2) | con Nat\n" +
      "  \\where \\use \\coerce test.{p3,p4} {A : \\Type p1} (n : Nat) : D A => con n", 1);
  }

  @Test
  public void nestedTest() {
    typeCheckModule("""
      \\func f.{p1,p2} => 0
        \\where \\func g => 0
          \\where \\func h => \\Type p2
      """);
    assertEquals(2, getDefinition("f.g.h").getLevelParameters().size());
  }

  @Test
  public void ownLevelsTest() {
    typeCheckModule("""
      \\func f.{p1,p2} => 0
        \\where \\func g.{q} => \\Type q
      """);
    assertEquals(1, getDefinition("f.g").getLevelParameters().size());
  }

  @Test
  public void shadowTest() {
    typeCheckModule("""
      \\func f.{p} => 0
        \\where \\func g.{p} => \\Type p
      """);
    assertEquals(1, getDefinition("f.g").getLevelParameters().size());
  }

  @Test
  public void dynamicWhereTest() {
    typeCheckModule("""
      \\record R.{p} (A : \\Type p) {
        \\func f => 0
          \\where \\func g => \\Type p
      }
      """);
    assertEquals(1, getDefinition("R.f.g").getLevelParameters().size());
  }

  @Test
  public void callTest() {
    typeCheckModule("""
      \\func f.{p1,p2} (A : \\Type p1) : \\Type p1 => g A
        \\where \\func g (B : \\Type p1) : \\Type p1 => B
      """, -1);
    System.err.println("CALL ERRORS: " + getAllErrors());
  }
}
