import org.scalatest.junit.JUnitSuite
import org.junit.{BeforeClass, After, Before, Test}

/**
 * Unittest im JUnit-Stil
 * @author Christoph Knabe
 */
class MyTest extends JUnitSuite {

  /**Deletes test Subscriptions and test Users before each test method.*/
  @Before
  def beforeMethod(): Unit = println("beforeMethod")

  @Test def vergleich {
    assertResult("Fachbereich I"){"Fachbereich I"}
  }
  
}// class FacultyTest

object FacultyTest {

  @BeforeClass
  def beforeClass(): Unit = {
    println("beforeClass")
  }

}
