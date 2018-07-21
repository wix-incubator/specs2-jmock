package com.wixpress.common.specs2

import org.specs2.mutable.Specification

class Example(collaborator: MyCollaborator) {
  def sutMethod(): Unit = {
    collaborator.method(1)
  }
}

trait MyCollaborator {
  def method(arg: Int): Unit
}

class NoExplicitErrorDescriptionTest extends Specification with JMock {

  "example" should {

    "call collaborator with expected input" in {
      val mockCollaborator = mock[MyCollaborator]
      val example = new Example(mockCollaborator)

      checking {
        oneOf(mockCollaborator).method(0)
      }
      example.sutMethod()
    }

  }
}
