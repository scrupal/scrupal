/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.utils

import org.specs2.mutable._

/** One line sentence description here.
  * Further description here.
  */
class PluralizerSpec extends Specification {
  "Pluralizer" should {
    "pluralize irregular nouns" >> {
      val word_pairs = List[(String, String)](
        ("abyss", "abysses"),
        ("alga", "algae"),
        ("alumnus", "alumni"),
        ("analysis", "analyses"),
        ("appendix", "appendices"),
        ("aquarium", "aquaria"),
        ("arch", "arches"),
        ("atlas", "atlases"),
        ("axe", "axes"),
        ("baby", "babies"),
        ("bacterium", "bacteria"),
        ("batch", "batches"),
        ("beach", "beaches"),
        ("brush", "brushes"),
        ("butterfly", "butterflies"),
        ("bus", "buses"),
        ("cactus", "cacti"),
        ("calf", "calves"),
        ("chateau", "chateaux"),
        ("cherry", "cherries"),
        ("child", "children"),
        ("church", "churches"),
        ("circus", "circuses"),
        ("city", "cities"),
        ("cod", "cod"),
        ("copy", "copies"),
        ("corpus", "corpa"),
        ("crisis", "crises"),
        ("criterion", "criteria"),
        ("curriculum", "curricula"),
        ("deer", "deer"),
        ("diagnosis", "diagnoses"),
        ("dictionary", "dictionaries"),
        ("die", "dice"),
        ("domino", "dominoes"),
        ("dwarf", "dwarves"),
        ("echo", "echoes"),
        ("elf", "elves"),
        ("emphasis", "emphases"),
        ("epoch", "epochs"),
        ("family", "families"),
        ("fax", "faxes"),
        ("fireman", "firemen"),
        ("fish", "fish"),
        ("flash", "flashes"),
        ("flush", "flushes"),
        ("fly", "flies"),
        ("focus", "foci"),
        ("foot", "feet"),
        ("formula", "formulae"),
        ("fungus", "fungi"),
        ("half", "halves"),
        ("hero", "heroes"),
        ("hippopotamus", "hippopotami"),
        ("hoax", "hoaxes"),
        ("hoof", "hooves"),
        ("index", "indices"),
        ("iris", "irides"),
        ("kiss", "kisses"),
        ("knife", "knives"),
        ("lady", "ladies"),
        ("leaf", "leaves"),
        ("life", "lives"),
        ("loaf", "loaves"),
        ("man", "men"),
        ("mango", "mangoes"),
        ("memorandum", "memoranda"),
        ("mess", "messes"),
        ("moose", "moose"),
        ("motto", "mottoes"),
        ("mouse", "mice"),
        ("nanny", "nannies"),
        ("neurosis", "neuroses"),
        ("nucleus", "nuclei"),
        ("oasis", "oases"),
        ("octopus", "octopi"),
        ("ox", "oxen"),
        ("osprey", "ospreys"),
        ("party", "parties"),
        ("pass", "passes"),
        ("penny", "pennies"),
        ("person", "people"),
        ("phenomenon", "phenomena"),
        ("piano", "pianos"),
        ("plateau", "plateaux"),
        ("poppy", "poppies"),
        ("potato", "potatoes"),
        ("quiz", "quizzes"),
        ("radius", "radii"),
        ("reflex", "reflexes"),
        ("runner-up", "runners-up"),
        ("scarf", "scarves"),
        ("schema", "schemata"),
        ("scratch", "scratches"),
        ("seraph", "seraphim"),
        ("series", "series"),
        ("sheaf", "sheaves"),
        ("sheep", "sheep"),
        ("shelf", "shelves"),
        ("son-in-law", "sons-in-law"),
        ("species", "species"),
        ("splash", "splashes"),
        ("spy", "spies"),
        ("stitch", "stitches"),
        ("story", "stories"),
        ("stratum", "strata"),
        ("studio", "studios"),
        ("syllabus", "syllabi"),
        ("tableau", "tableaux"),
        ("tax", "taxes"),
        ("thesis", "theses"),
        ("thief", "thieves"),
        ("tomato", "tomatoes"),
        ("tooth", "teeth"),
        ("tornado", "tornadoes"),
        ("try", "tries"),
        ("vertebra", "vertebrae"),
        ("volcano", "volcanoes"),
        ("waltz", "waltzes"),
        ("wash", "washes"),
        ("watch", "watches"),
        ("wharf", "wharves"),
        ("wolf", "wolves"),
        ("wife", "wives"),
        ("woman", "women"),
        ("yourself", "yourselves")
      )
      word_pairs.foreach{ pair ⇒
        Pluralizer.pluralize(pair._1) must equalTo(pair._2)
      }
      success
    }

    "pluralize regular nouns" in {
      val word_pairs = List[(String, String)](
        ("word", "words")
      )
      word_pairs.foreach{ pair ⇒
        Pluralizer.pluralize(pair._1) must equalTo(pair._2)
      }
      success
    }

    "pluralize classical english, greek and latin nouns" in {
      val word_pairs = List[(String, String)](
        ("ephemeris", "ephemerides")
      )
      word_pairs.foreach{ pair ⇒
        Pluralizer.pluralize(pair._1) must equalTo(pair._2)
      }
      success
    }

    "not pluralize bogus pairings" in {
      val word_pairs = List[(String, String)] (
        ("prius", "gay")
      )
      word_pairs.foreach{ pair ⇒
        Pluralizer.pluralize(pair._1) must not equalTo pair._2
      }
      success
    }
  }
}
