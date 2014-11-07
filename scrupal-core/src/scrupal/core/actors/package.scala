package scrupal.core

/** Core Actors Package
  *
  * All processing in Scrupal is asynchronous and (hopefully) non-blocking, right down to the database. Consequently,
  * getting anything done involves using Akka actors. In particular this module contains all the logic for processing
  * requests which can happen with a high degree of concurrency.
 */
package object actors {

}
