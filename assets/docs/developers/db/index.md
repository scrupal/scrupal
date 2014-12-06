# Scrupal DB Project

The scrupal-db project provides the interface to MongoDB. MongoDB is a no-SQL document storage engine. It uses a
binary form of JSON, BSON, for its storage format. Scrupal converts BSON into case classes for easier manipulation in
Scala but can also deliver BSON documents wholesale to clients depending on what is needed. All entities stored by
Scrupal are stored as BSON documents of a particular structure, as defined by the BundleType of the entity.

The main classes to be aware of are:

* DataAccessObject - Represents a collection of documents of a particular immutable structure or shape.
* VariantDataAccessObject - Represents a collection of documents that are variants of some class of documents.
* ScrupalDB - Represents a database in which the DAO collections are stored
* Schema - Represents a set of DAO collections that are stored together and have interrelations.
* DBContext - An object for holding the context for database access.

### Based On ReactiveMongo
TBD

### Adding A New Collection
TBD

### Defining A Schema
TBD

### Writing Conversion Handlers
TBD

