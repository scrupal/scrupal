package scrupal.fakes

import scrupal.core.BundleType
import scrupal.core.api.Entity
import scrupal.utils.OSSLicense

/**
 * Created by reidspencer on 11/11/14.
 */
case class FakeEntity(name: String, instanceType: BundleType) extends Entity {

  def path: String = name
  def id: Symbol = Symbol(name)

  final val kind: Symbol = 'FakeEntity


  val author: String = "author"

  val copyright: String = "copyright"

  val license: OSSLicense = OSSLicense.GPLv3

  val description: String = name
}
