package cli

import org.scalatest._

class CliTests extends FunSpec with Matchers with Inside {

  val id = Preprocessor.lines("identity")(identity)
  val reverse = Preprocessor.lines("reverse") { lines =>
    lines.map(_.reverse)
  }

  val failp = Preprocessor.lines("fail") { lines =>
    throw new Throwable("some error!")
  }

  val file = new java.io.File("file")

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
    inside(cli.process(file, source)) {
      case Right(lines) => lines should equal(output)
    }
  }

  it("should leave unscoped lines alone") {
  val preprocessors = Map("identity" -> id)
    val source = List(
      "def bar: Int = 1",
      "def foo: Unit = {}",
      "#+identity",  
      "val x = 1 + 2",
      "val y = 3",
      "val z = x + y",
      "#-identity"
    )
    val output = List(
      "def bar: Int = 1",
      "def foo: Unit = {}",
      "val x = 1 + 2",
      "val y = 3",
      "val z = x + y")
    
    val cli = new Cli(preprocessors)
    inside(cli.process(file, source)) {
      case Right(lines) => lines should contain theSameElementsInOrderAs output
    }
  }

  it("should fail in the case of a unexpected directive") {
    val preprocessors = Map("identity" -> id, "reverse" -> reverse)
    val source = List(
      "#+identity",
      "val x = 1 + 2",
      "val y = 2 + 4",
      "#-reverse",
      "#-identity"
    )
    val cli = new Cli(preprocessors)
    inside(cli.process(file, source)) {
      case Left(Cli.UnexpectedDirective("reverse", "identity", line, _)) =>
        line shouldBe 4
    }
  }

  it("should fail in the case of a unmatched directive") {
    val preprocessors = Map("identity" -> id)
    val source = List(
      "val x = 1 + 2",
      "#-identity"
    )
    val cli = new Cli(preprocessors)
    inside(cli.process(file, source)) {
      case Left(Cli.UnmatchedDirective("identity", line, _)) =>
        line shouldBe 2
    }
  }

  it("should fail if it reaches the EOF with unmatched directives") {
    val preprocessors = Map("identity" -> id, "reverse" -> reverse)
    val source = List(
      "#+identity",
      "#+reverse",
      "val x = 1"
    )
    val cli = new Cli(preprocessors)
    inside(cli.process(file, source)) {
      case Left(Cli.UnclosedDirectives(directives, _)) =>
        directives should contain theSameElementsInOrderAs List("reverse", "identity")
    }
  }

  it("should fail if it encounteres an unknown directive") {
    val cli = new Cli(Map.empty)
    val source = List(
      "val x = 1",
      "#+unknown",
      "val y = 2"
    )
    inside(cli.process(file, source)) {
      case Left(Cli.UnknownDirective("unknown", line, _)) => line shouldBe 2
    }
  }

  it("should fail if a preprocessor throws an exception") {
    val cli = new Cli(Map("fail" ->  failp))
    val source = List(
      "val x = 1",
      "#+fail",
      "val y = 2",
      "#-fail"
    )
    inside(cli.process(file, source)) {
      case Left(Cli.PreprocessorFailed(fail, t, 4, _)) => t.getMessage shouldBe "some error!"
    }
  }

  it("should process directives recursively") {
    val preprocessors = Map("reverse" -> reverse, "reverse2" -> reverse)
    val source = List(
      "#+reverse",
      "#+reverse2",
      "123",
      "456",
      "#-reverse2",
      "#-reverse"
    )
    val output = List(
      "123",
      "456"
    )
    val cli = new Cli(preprocessors)
    inside(cli.process(file, source)) {
      case Right(lines) => lines should contain theSameElementsInOrderAs output
    }

  }

  it("should process an empty file") {
  val preprocessors = Map("identity" -> id)
    val source = List.empty[String]
    val cli = new Cli(preprocessors)
    inside(cli.process(file, source)) {
      case Right(lines) => lines shouldBe empty
    }    
  }
}
