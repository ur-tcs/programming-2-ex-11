// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
package examples

import munit.*

class SucceedFailTest extends FunSuite {
  test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    assertEquals(obtained, expected)
  }

  test("exampe test that fails"){
    assertEquals(2+4,8)
    assert(false)
    assert(2*2 == 4)
  }
}

class ExampleTests extends FunSuite {
  test("pathologicalCases"){
    assertEquals(tailfac(1),1)
    assertEquals(tailfac(0),1)
    assertEquals(tailfac(-1),1)
  }

  test("samples"){
  def fac(n: Int): Int = 
    if n <= 1 then 1 else n*fac(n-1)
  for i <- 1 to 10 yield assertEquals(tailfac(i),fac(i))
  }
}