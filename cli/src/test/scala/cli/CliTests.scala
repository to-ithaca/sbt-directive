package cli

import org.scalatest._

class CliTests extends FunSpec with Matchers {

  val id = Preprocessor.lines("identity")(identity)

  it("should return the same lines given an identity preprocessor") {
    val preprocessors = Map("identity" -> id)
    val source = List(
      "#+identity",
      "val x = 1 + 2",
      "val y = 3",
      "val z = x + y",
      "#-identity"
    )
    val output = List(
      "val x = 1 + 2",
      "val y = 3",
      "val z = x + y")
    
    val cli = new Cli(preprocessors)
    cli.process("code2.scala", source) should equal(output)
  }

  it("should leave unscoped lines alone") {
  val preprocessors = Map("identity" -> id)
    val source = List(
      "def bar: Int = 1", //0
      "def foo: Unit = {}", //0
      "#+identity",  
      "val x = 1 + 2",  //1
      "val y = 3",  //1
      "val z = x + y", //1
      "#-identity"
    )
    val output = List(
      "def bar: Int = 1",
      "def foo: Unit = {}",
      "val x = 1 + 2",
      "val y = 3",
      "val z = x + y")
    
    val cli = new Cli(preprocessors)
    val result = cli.process("code2.scala", source)
    cli.process("code2.scala", source) should contain theSameElementsInOrderAs output
  
  }

  it("output lines should not contain directives")(pending)

  it("should process directives recursively") {

  }

  it("should process an empty file") {
  val preprocessors = Map("identity" -> id)
    val source = List.empty[String]
    val cli = new Cli(preprocessors)
    val result = cli.process("code2.scala", source)
    cli.process("code2.scala", source) shouldBe empty
  }
}
