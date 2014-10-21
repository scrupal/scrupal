package scrupal

/** Scrupal HTTP Interface Project
  * The purpose of scrupal-http is to interface the core abstractions of Scrupal with the web. Because Scrupal uses
  * a very regular information structure, we don't need a fully generalized web processing mechanism. Instead, this
  * module adapts itself to the sites, entities, and modules that have been defined by the user and dynamically
  * arranges for the corresponding web interface to be constructed. Note that this library provides mechanism but
  * not content. This is where the request routing is performed and the vagaries of http processing are hidden.
  * Users of this library simply register the Scrupal entities and provide the responses necessary.
 */
package object http {

}
