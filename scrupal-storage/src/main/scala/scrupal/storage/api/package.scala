package scrupal.storage

/** Scrupal Storage API
  *
  * This package defines the abstract API to the Scrupal Storage System. The API is intended to have many
  * implementations. There are just a few abstractions to implement as Scrupal's storage needs are quite basic:
  *
  * - Driver: A top level singleton object that implements the API to provide access to Stores of data
  * - Store: A container of data that is identified with a URI
  * - Schema: A named container of Collections of data that is contained itself within a Store
  * - Collection: A named container of objects potentially with multiple indexes
  * - Storable: The base class of any object that can be store in the Storage System
  *
  * Kryo Serialization is provided for all Storable classes via the Chill package, should the storage implementations
  * need to serialize graphs of objects. However, the details of storing the objects is left to the implementation.
  * Relational databases could just provide tables with columns for the indexes and a BLOB for the serialized data.
  * Document and graph databases might convert the object graph to a document. Implementations of the API should do
  * what is natural, useful and efficient for that type of storage.
  *
  * This package contains one concrete implementation that uses transient memory for storage.
  * See [[scrupal.store.mem]] for details.
  */
package object api {
  type ID = Long
}
