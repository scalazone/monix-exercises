package scalazone.monix

import monix.execution.schedulers.TestScheduler
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

abstract class BaseTestSuite extends AnyFunSuite with Matchers
