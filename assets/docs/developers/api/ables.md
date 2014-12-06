<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright © 2014 Reactific Software LLC                                                                           ~
  ~                                                                                                                   ~
  ~ This file is part of Scrupal, an Opinionated Web Application Framework.                                           ~
  ~                                                                                                                   ~
  ~ Scrupal is free software: you can redistribute it and/or modify it under the terms                                ~
  ~ of the GNU General Public License as published by the Free Software Foundation,                                   ~
  ~ either version 3 of the License, or (at your option) any later version.                                           ~
  ~                                                                                                                   ~
  ~ Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                              ~
  ~ without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         ~
  ~ See the GNU General Public License for more details.                                                              ~
  ~                                                                                                                   ~
  ~ You should have received a copy of the GNU General Public License along with Scrupal.                             ~
  ~ If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                        ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->

# Ables
The `scrupal/api/Ables.scala` file is so named because the names of everything it defines end in "able". These are
the essential traits that are mixed in to various classes, and together, to add various combinations of simple data
elements. The primary things to know about this are:

- Identifiable takes a type parameter which provides the type by which the thing is identified. We use two basic
kinds: NumericIdentifier (Long) and SymbolicIdentifier (Symbol). Both of these can be used as primary keys in the
database. We use NumericIdentifier for things that have no memory cache (e.g. Instances) and we use
SymbolicIdentifier for things that have a memory cache (e.g. Modules and Types).
- There are really just a few essential traits: Creatable (has a creation timestamp), Modifiable (has a modification
time stamp), Nameable (has a name in addition to its identifier), Describable (has a description field). All the
  remaining traits are just combinations of these essential ones.
