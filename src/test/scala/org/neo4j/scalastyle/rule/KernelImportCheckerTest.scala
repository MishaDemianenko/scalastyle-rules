package org.neo4j.scalastyle.rule

import org.junit.Test
import org.scalastyle.Checker
import org.scalastyle.file.CheckerTest
import org.scalatest.Spec
import org.scalatest.junit.AssertionsForJUnit

class KernelImportCheckerTest extends Spec with CheckerTest with AssertionsForJUnit {

  override protected val key: String = "illegal.imports.kernel"
  override protected val classUnderTest: Class[_ <: Checker[_]] = classOf[KernelImportChecker]

  @Test def checkCorrectFile() {
    val source =
          """
    package org.neo4j.scala

    import java.util.Date

    object Cypher {
      val counter = 1
    }
    """

    assertErrors(Nil, source)
  }

  @Test
  def detectIncorrectImport(): Unit = {
    val source =
      """
    package org.neo4j.scala

    import org.neo4j.kernel.SomeClass

    object Cypher {
      val counter = 1
    }
    """

    assertErrors(List(columnError(4, 4)), source)
  }

  @Test
  def useCustomKernelImportPrefix(): Unit = {
    val source =
      """
    package org.neo4j.scala

    import org.neo4j.kernel.SomeClass

    object Cypher {
      val counter = 1
    }
    """

    assertErrors(Nil, source, Map("kernelImportPrefix" -> "java.util"))
  }

  @Test
  def detectIllegalImportWithCustomKernelImportPrefix(): Unit = {
    val source =
      """
    package org.neo4j.scala

    import org.jboss.Server
    import java.util.Date


    object Cypher {
      val counter = 1
    }
    """

    assertErrors(List(columnError(5, 4)), source, Map("kernelImportPrefix" -> "java.util"))
  }

  @Test
  def skipValidationInExcludedPackages(): Unit = {
    val source =
      """
    package org.neo4j.cypher.internal

    import org.neo4j.kernel.SomeClass

    object Cypher {
      val counter = 1
    }
    """

    assertErrors(Nil, source, Map("excludedPackages" -> "org.neo4j.cypher.internal"))
  }

  @Test
  def skipValidationInExcludedClasses(): Unit = {
    val source =
      """
    package org.neo4j.cypher
    import org.neo4j.kernel.SomeClass

    class NonValidClass {
    }
    """

    assertErrors(Nil, source, Map("excludedClasses" -> "org.neo4j.cypher.NonValidClass"))
  }




}
