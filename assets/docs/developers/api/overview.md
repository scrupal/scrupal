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
# Scrupal API Project

The scrupal-api project is where all the essential classes and traits for Scrupal are defined. It is also where the
processing engine of Scrupal resides. The core abstractions in Scrupal form an acronym, MANIFESTO, which stands for:

- **[M: Module](module.md):** A container of functionality that defines the other types of api objects.

- **[A: Action](action.md):** A functor that produces a Result; the primary object of behavior in Scrupal.

- **[N: Node](node.md):** A content generation function that converts a Context into a Result

- **[I: Instance](instance.md):** An instance of an entity (essentially a document)

- **[F: Facet](facet.md):** Something to add on to an instance's main payload; a mechanism of extension

- **[E: Entity](entity.md):** A type of instance with definitions the actions that can be performed on it

- **[S: Site](site.md):** A site definition as a collection of enabled capabilities.

- **[T: Type](type.md):** A fundamental data type used for validating BSONValues (Instances and Node results)

- **[O: Others](others.md):** Other objects of lesser importance.
