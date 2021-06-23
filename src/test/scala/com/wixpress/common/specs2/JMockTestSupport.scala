package com.wixpress.common.specs2

import org.specs2.execute.AsResult

trait JMockTestSupport {

  def scalaVersionAtLeast(ver: String) = {
    val scalaVer = util.Properties.versionNumberString
    def majorVersionDecimal(fullVer: String) = BigDecimal(fullVer.split('.').take(2).mkString("."))
    majorVersionDecimal(scalaVer) >= majorVersionDecimal(ver)
  }

  def withJMock[R: AsResult](f: JMockDsl => R): R = {
    val jmock = new JMockDsl {
      var successful: Boolean = false
    }
    try {
      val res = f(jmock)
      jmock.successful = implicitly[AsResult[R]].asResult(res).isSuccess
      res
    }
    finally {
      if(!jmock.successful) {
        try
          jmock.throwIfAssertUnsatisfied()
        catch {
          case e: Throwable => e.printStackTrace()
        }
      }
    }
  }
}

object JMockTestSupport extends JMockTestSupport
