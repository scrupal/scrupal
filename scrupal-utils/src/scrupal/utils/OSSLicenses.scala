package scrupal.utils

/** A Registration of the Open Source Software Licenses
 * Created by reid on 11/11/14.
 */
case class OSSLicense(name: String, description: String, url: String) {

}

object OSSLicense {

  val GPLv3 = OSSLicense("GPLv3", "Gnu General Public License version 3", "http://www.gnu.org/copyleft/gpl.html")
}
