package com.wixpress.common.specs2

package object impostisers {
  /**
   * change this to true for debugging errors in fallback scenarios (e.g. when using a jdk that doesn't support `invokespecial` on private methods)
   */
  val debugErrors = false

  def traceError(msg: String, e: Throwable = null) = {
    if (debugErrors) {
      new Throwable(s"[**** ERROR] $msg", e).printStackTrace()
    }
  }
}
