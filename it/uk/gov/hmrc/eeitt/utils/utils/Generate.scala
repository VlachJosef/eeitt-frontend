package uk.gov.hmrc.eeitt.utils.utils

/**
  * Created by harrison on 03/10/16.
  */

import java.util.UUID

import uk.gov.hmrc.domain.SaUtr

import scala.util.Random

object Generate {
  private val random = new Random()

  def utr = SaUtr(UUID.randomUUID.toString)

  def email() = s"${UUID.randomUUID()}@TEST.com"
}
