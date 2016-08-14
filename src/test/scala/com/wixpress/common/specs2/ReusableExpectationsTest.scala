package com.wixpress.common.specs2

import org.specs2.mutable.Specification

class ReusableExpectationsTest extends Specification {

  trait MyContext extends CollaboratorExpectations


  // TODO find a way to use pending on a test or assert that test should fail.
//  "Something should break" >> new MyContext {
//    success
//  }
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