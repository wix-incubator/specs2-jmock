package com.wixpress.common.specs2

import org.specs2.mutable.Specification

class ReusableExpectationsTest extends Specification {

  trait MyContext extends CollaboratorExpectations

  "Something should break" >> new MyContext {
    success
  }.pendingUntilFixed("This test should fail")
}

trait CollaboratorExpectations extends scope.JMock {
  val collaborator = mock[Collaborator]

  checking {
    oneOf(collaborator).doIt()
  }
}

trait Collaborator {
  def doIt(): Unit
}