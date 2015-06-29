package com.staticbloc.events;

import com.google.common.io.Resources;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 1:16 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JUnit4.class)
public class EventHandlerProcessorTest {
  @Test
  public void testStuff() {
//    JavaFileObject file = JavaFileObjects.forSourceLines("test.Test",
//        "package test;",
//        "",
//        "import com.staticbloc.events.*;",
//        "",
//        "public class Test {",
//        "  public static class E implements Event {}",
//        "",
//        "  public static class EE extends E {}",
//        "",
//        "  public static class EEE extends EE {}",
//        "",
//        "  public static class E2 implements Event {}",
//        "",
//        "  public static class E3 extends EE {}",
//        "",
//        "  public static class E4 extends E {}",
//        "",
//        "  @EventHandler() public void test(E3 t) {}",
//        "  @EventHandler() public void test(EE t) {}",
//        "  @EventHandler() public void test(E t) {}",
//        "  @EventHandler() public void test(EEE t) {}",
//        "  @EventHandler() public void test(E2 t) {}",
//        "  @EventHandler() public void test(E4 t) {}",
//        "}");

    assert_().about(javaSource()).that(JavaFileObjects.forResource("SimpleTest.java"))
        .processedWith(new EventHandlerProcessor())
        .compilesWithoutError();
  }
}
