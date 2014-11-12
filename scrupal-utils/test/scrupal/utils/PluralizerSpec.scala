/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.utils

import org.specs2.mutable._


/**
 * One line sentence description here.
 * Further description here.
 */
class PluralizerSpec extends Specification
{
	"Pluralizer" should {
		"pluralize irregular nouns" >> {
			val word_pairs = List[(String,String)](
				("abyss","abysses"),
				("alga", "algae"),
				("alumnus","alumni"),
				("analysis","analyses"),
				("appendix","appendices"),
			  ("aquarium","aquaria"),
				("arch", "arches"),
				("atlas","atlases"),
				("axe","axes"),
				("baby","babies"),
				("bacterium","bacteria"),
				("batch","batches"),
				("beach","beaches"),
				("brush","brushes"),
				("butterfly", "butterflies"),
				("bus","buses"),
				("cactus", "cacti"),
				("calf","calves"),
				("chateau","chateaux"),
				("cherry","cherries"),
				("child","children"),
				("church","churches"),
				("circus","circuses"),
				("city","cities"),
				("cod","cod"),
				("copy","copies"),
				("corpus", "corpa"),
				("crisis","crises"),
				("criterion","criteria"),
				("curriculum","curricula"),
				("deer","deer"),
				("diagnosis","diagnoses"),
				("dictionary","dictionaries"),
				("die","dice"),
				("domino","dominoes"),
				("dwarf","dwarves"),
				("echo","echoes"),
				("elf","elves"),
				("emphasis","emphases"),
				("epoch", "epochs"),
				("family","families"),
				("fax","faxes"),
				("fireman","firemen"),
				("fish","fish"),
				("flash", "flashes"),
				("flush","flushes"),
				("fly","flies"),
				("focus","foci"),
				("foot","feet"),
				("formula", "formulae"),
				("fungus","fungi"),
				("half","halves"),
				("hero","heroes"),
				("hippopotamus","hippopotami"),
				("hoax","hoaxes"),
				("hoof","hooves"),
				("index","indices"),
				("iris","irides"),
				("kiss","kisses"),
				("knife","knives"),
				("lady","ladies"),
				("leaf","leaves"),
				("life","lives"),
				("loaf","loaves"),
				("man","men"),
				("mango","mangoes"),
				("memorandum","memoranda"),
				("mess","messes"),
				("moose","moose"),
				("motto","mottoes"),
				("mouse","mice"),
				("nanny","nannies"),
				("neurosis","neuroses"),
				("nucleus","nuclei"),
				("oasis","oases"),
				("octopus","octopi"),
				("ox","oxen"),
				("osprey", "ospreys"),
				("party","parties"),
				("pass","passes"),
				("penny","pennies"),
				("person","people"),
				("phenomenon", "phenomena"),
				("piano", "pianos"),
				("plateau","plateaux"),
				("poppy","poppies"),
				("potato","potatoes"),
				("quiz","quizzes"),
				("radius","radii"),
				("reflex","reflexes"),
				("runner-up","runners-up"),
				("scarf","scarves"),
				("schema","schemata"),
				("scratch","scratches"),
				("seraph","seraphim"),
				("series","series"),
				("sheaf","sheaves"),
				("sheep","sheep"),
				("shelf","shelves"),
				("son-in-law","sons-in-law"),
				("species","species"),
				("splash","splashes"),
				("spy","spies"),
				("stitch","stitches"),
				("story","stories"),
				("stratum","strata"),
				("studio", "studios"),
				("syllabus","syllabi"),
				("tableau","tableaux"),
				("tax","taxes"),
				("thesis","theses"),
				("thief","thieves"),
				("tomato","tomatoes"),
				("tooth","teeth"),
				("tornado","tornadoes"),
				("try","tries"),
				("vertebra", "vertebrae"),
				("volcano","volcanoes"),
				("waltz","waltzes"),
				("wash","washes"),
				("watch","watches"),
				("wharf","wharves"),
				("wolf", "wolves"),
				("wife","wives"),
				("woman","women"),
				("yourself", "yourselves")
			)
			word_pairs.foreach{ pair =>
				Pluralizer.pluralize(pair._1) must equalTo(pair._2)
			}
			success
		}

		"pluralize regular nouns" in {
			val word_pairs = List[(String,String)](
				("word", "words")
			)
			word_pairs.foreach{ pair =>
				Pluralizer.pluralize(pair._1) must equalTo(pair._2)
			}
			success
		}

		"pluralize classical english, greek and latin nouns" in {
			val word_pairs = List[(String,String)](
				("ephemeris", "ephemerides")
			)
			word_pairs.foreach{ pair =>
				Pluralizer.pluralize(pair._1) must equalTo(pair._2)
			}
			success
		}

		"not pluralize bogus pairings" in {
			val word_pairs = List[(String,String)] (
				("prius", "gay")
			)
			word_pairs.foreach{ pair =>
				Pluralizer.pluralize(pair._1) must not equalTo pair._2
			}
			success
		}
	}
}
