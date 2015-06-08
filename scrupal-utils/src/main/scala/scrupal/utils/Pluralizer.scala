/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                  *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                  *
  *      http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

import java.util.regex.{ Matcher, Pattern }

/** Competently find the plural of most common English words.
  *
  * In dealing with logged messages, error messages, and other output that people see,
  * it is always nice to have the correct plural form of nouns be used. This class attempts to get pretty good
  * coverage for the english language based on a variety of pluralization rules. This is also used in URL
  * path matching to distinguish between operations that act on an instance (singular) or a collection of instances
  * (plural)
  *
  * Inspired by: [[https://github.com/atteo/evo-inflector/blob/master/src/main/java/org/atteo/evo/inflector/TwoFormInflector.java]]
  * Rules from: [[http://www.barstow.edu/lrc/tutorserv/handouts/015%20Irregular%20Plural%20Nouns.pd]]
  * Oxford Rules: [[http://oxforddictionaries.com/words/plurals-of-nouns]]
  */
abstract class Pluralizer {

  /** A rule is very simple, it maps a pattern to match with the substitution to make for that pattern.
    * @param singular A regular expression with a substitution group to be replaced by `plural`
    * @param plural The substitution for the group in `singular`
    */
  case class Rule(singular : Pattern, plural : String) {}

  val rules : scala.collection.mutable.ListBuffer[Rule] = new scala.collection.mutable.ListBuffer[Rule]

  /** The main interface to this class.
    * Call pluralize to pluralize any word and return its plural form.
    * @param word The word to be pluralized
    * @return The plural form of word
    */
  def pluralize(word : String) : String =
    {
      rules.map { rule ⇒
        val matcher : Matcher = rule.singular.matcher(word)
        if (matcher.find())
          return matcher.replaceFirst(rule.plural)
      }
      // Without a matching pluralized word, just return the word.
      word
    }

  /** Declaration of a non-plural word.
    * Subclasses can register a word here that does not have a plural form. The plural word will be the same
    * as the singular word.
    * @param word The word whose plural form is the same as its singular form
    */
  protected def noplural(word : String) : Unit =
    {
      rules += new Rule(Pattern.compile("(?i)(" + word + ")$"), "$1")
    }

  /** Declaration of a list of non-plural words.
    * Subclasses can register a list of words that do not have plural forms. The plural words will each be the
    * same as their singular word counterpart.
    * @param word_list A list of worlds whose plural form is the same as its singular form.
    */
  protected def noplural(word_list : List[String]) : Unit =
    {
      val builder : StringBuilder = new StringBuilder()
      builder.append("(?i)(").append(word_list.head)
      word_list.tail.foreach { word : String ⇒
        builder.append("|").append(word)
      }
      builder.append(")$")
      rules += new Rule(Pattern.compile(builder.toString()), "$1")
    }

  /** Declaration of an irregular wwhose plural just doesn't match a rule
    * Subclasses can register a pair of words that have an irregular plural form. That is, it is not easy to
    * convert the singular to the plural so both must be specified.
    * @param singular The singular form of the word
    * @param plural The plural form of the word
    */
  protected def irregular(singular : String, plural : String) : Unit = {
    if (singular.charAt(0) == plural.charAt(0)) {
      rules += new Rule(Pattern.compile("(?i)(" + singular.charAt(0) + ")" + singular.substring(1)
        + "$"), "$1" + plural.substring(1))
    } else {
      rules += new Rule(Pattern.compile(Character.toUpperCase(singular.charAt(0)) + "(?i)"
        + singular.substring(1) + "$"), Character.toUpperCase(plural.charAt(0))
        + plural.substring(1))
      rules += new Rule(Pattern.compile(Character.toLowerCase(singular.charAt(0)) + "(?i)"
        + singular.substring(1) + "$"), Character.toLowerCase(plural.charAt(0))
        + plural.substring(1))
    }
  }

  protected def irregular(pair : (String, String)) : Unit = irregular(pair._1, pair._2)

  protected def irregular(pairs : List[(String, String)]) : Unit = pairs.foreach { pair ⇒ irregular(pair) }

  protected def standard(singular : String, plural : String) : Unit =
    rules += new Rule(Pattern.compile(singular, Pattern.CASE_INSENSITIVE), plural)

  protected def standard(pair : (String, String)) : Unit = standard(pair._1, pair._2)

  protected def standard(pairs : List[(String, String)]) : Unit = pairs.foreach { pair ⇒ standard(pair) }

  protected def category(word_list : List[String], pattern : String, plural : String) : Unit =
    {
      val builder : StringBuilder = new StringBuilder()

      builder.append("^(?=").append(word_list.head)
      word_list.tail.foreach { word ⇒ builder.append("|").append(word) }
      builder.append(")")
      builder.append(pattern)
      rules += new Rule(Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE), plural)
    }
}

/** Interface to Pluralizer
  * This object allows us to write {{{Pluralizer("word")}}} to obtain the plural form or
  * {{{Pluralizer("word",count}}} if we want it to be based on the value of count.
  * This class also calls all the base class methods to install the rules.
  */
object Pluralizer extends Pluralizer {
  def apply(word : String, count : Int = 2) = pluralize(word, count)
  def apply(word : Symbol) = pluralize(word.name)

  def pluralize(word : Symbol) : String = pluralize(word.name)

  def pluralize(word : String, count : Int = 2) : String =
    {
      if (count == 1 || count == -1) {
        word
      } else {
        pluralize(word)
      }
    }

  /** Construct the Pluralizer by using the super class's methods to install the rules
    */

  // Some words are the same with plural and singular. We list these first so they don't
  // attempt to match other patterns below.
  noplural(
    List(
      "bison", "bream", "breeches", "britches",
      "carp", "chassis", "clippers", "cod", "contretemps", "corps",
      "debris", "deer", "diabetes", "djinn",
      "eland", "elk",
      "fish", "flounder",
      "gallows", "graffiti",
      "headquarters", "herpes", "high-jinks", "homework",
      "innings", "itis",
      "jackanapes",
      "mackerel", "measles", "mews", "moose", "mumps",
      "news",
      "ois",
      "pincers", "pliers", "pox", "proceedings",
      "rabies",
      "salmon", "scissors", "sea-bass", "series", "shears", "sheep", "species", "sugar", "swine",
      "trout", "tuna",
      "wildebeest", "whiting"
    )
  )

  //  Some words defy categorization so we do a direct mapping for these ones.
  irregular(
    List(
      ("beef", "beefs"),
      ("brother", "brothers"),
      ("child", "children"),
      ("cow", "cows"),
      ("die", "dice"),
      ("ephemeris", "ephemerides"),
      ("genie", "genies"),
      ("genus", "genera"),
      ("money", "monies"),
      ("mongoose", "mongoose"),
      ("octopus", "octopi"),
      ("opus", "opuses"),
      ("ox", "oxen"),
      ("person", "people"),
      ("quiz", "quizzes"),
      ("runner-up", "runners-up"),
      ("soliloquy", "soliloquies"),
      ("son-in-law", "sons-in-law"),
      ("trilby", "trilbys")
    )
  )

  // Certain nouns ending in "man" get pluralized as "mans" not "men", list those before we do the
  // "man->men" endings.
  category(List[String] (
    "human", "Alabaman", "Bahaman", "Burman", "German", "Hiroshiman", "Liman", "Nakayaman", "Oklahoman",
    "Panaman", "Selman", "Sonaman", "Tacoman", "Yakiman", "Yokohaman", "Yuman"
  ),
    "(.*)$", "$1s")

  // Certain irregular plurals have standard suffix inflections that we can count on.
  standard(
    List(
      ("man$", "men"),
      ("([lm])ouse$", "$1ice"),
      ("tooth$", "teeth"),
      ("goose$", "geese"),
      ("foot$", "feet"),
      ("zoon$", "zoa"),
      ("([csx])is$", "$1es")
    )
  )

  // Some words ending in -ex become -ices
  category(
    List (
      "apex",
      "codex", "cortex",
      "index",
      "latex",
      "murex",
      "pontifex",
      "silex", "simplex",
      "vertex", "vortex"
    ),
    "(.*)ex$", "$1ices"
  )

  // Some words ending in -ix become -ices
  category(
    List (
      "appendix",
      "crucifix",
      "helix",
      "matrix",
      "radix"
    ),
    "(.*)ix$", "$1ices"
  )

  // Some words ending in -um become -a
  category(
    List(
      "addendum",
      "agendum",
      "aquarium",
      "bacterium",
      "candelabrum",
      "curriculum",
      "datum",
      "desideratum",
      "erratum",
      "extremum",
      "memorandum",
      "ovum",
      "stratum"
    ), "(.*)um$", "$1a")

  // Some words ending in -on become -a
  category(
    List(
      "aphelion",
      "asyndeton",
      "criterion",
      "hyperbaton",
      "noumenon",
      "organon",
      "perihelion",
      "phenomenon",
      "prolegomenon"
    ), "(.*)on$", "$1a")

  // Some words ending in -a become -ae
  category(
    List(
      "alga",
      "alumna",
      "persona",
      "vertebra"
    ),
    "(.*)a$", "$1ae"
  )

  // Some words that end in -f become -ves
  category(
    List(
      "hoof",
      "loaf",
      "meatloaf",
      "oaf",
      "roof",
      "sugarloaf",
      "thief"
    ), "(.*)f", "$1ves"
  )

  // Some words ending in -en become -ina
  category(
    List(
      "foramen",
      "lumen",
      "stamen"
    ),
    "(.*)en$", "$1ina"
  )

  // Some words ending in -ma become -mata
  category(
    List (
      "anathema",
      "bema",
      "carcinoma", "charisma",
      "diploma", "dogma", "drama",
      "edema", "enema", "enigma",
      "gumma",
      "lemma", "lymphoma",
      "magma", "melisma", "miasma",
      "oedema",
      "sarcoma", "schema", "soma", "stigma", "stoma",
      "trauma"
    ),
    "(.*)a$", "$1ata"
  )

  // Some words ending in -la become -lae
  category(
    List(
      "formula"
    ),
    "(.*)a$", "$1ae"
  )

  // Clasically, a few words ending in -is become -ides
  category(
    List (
      "iris", "clitoris"
    ),
    "(.*)is$", "$1ides"
  )

  // Some words ending in -us become -uses instead of the more usual -i ending
  category(
    List (
      "apparatus",
      "cantus",
      "coitus",
      "hiatus",
      "impetus",
      "plexus",
      "prospectus",
      "nexus",
      "sinus",
      "status"
    ),
    "(.*)us$", "$1uses"
  )

  // A few words ending in -us become -a instead of -uses or -i
  category(
    List (
      "corpus"
    ),
    "(.*)us$", "$1a"
  )

  // Now that the -us exceptions are handled we can specify th words for which -us becomes -i
  category(
    List(
      "alumnus", "alveolus",
      "bacillus", "bronchus",
      "cactus",
      "focus", "fungus",
      "hippopotamus",
      "locus",
      "meniscus",
      "nucleus",
      "radius",
      "stimulus",
      "syllabus",
      "thesaurus"
    ), "(.*)us$", "$1i"
  )

  // Classically words ending in -o can become -i but many words aren't used that way any more
  category(
    List (
      "tempo",
      "virtuoso"
    ), "(.*)o$", "$1i"
  )

  // Some words ending in -o become -os (including ones preceded by a vowel)
  category(
    List (
      "albino", "alto", "archipelago", "armadillo", "auto",
      "basso",
      "canto", "casino", "commando", "contralto", "crescendo",
      "fiasco",
      "ditto", "dynamo",
      "embryo",
      "generalissimo", "ghetto", "guano",
      "inferno",
      "jumbo",
      "lingo", "lumbago",
      "macro",
      "magneto", "manifesto", "medico",
      "octavo",
      "photo", "piano", "pro",
      "quarto",
      "rhino",
      "solo", "soprano", "stylo",
      "zero"
    ),
    "(.*)o$", "$1os"
  )

  // A few words just get -i appended, generally they end in t preceded by a vowel
  category(
    List(
      "afreet", "afrit", "efreet"
    ), "(.*)$", "$1i"
  )

  // A few words just get -im appended
  category(
    List(
      "cherub", "goy", "seraph"
    ),
    "(.*)$", "$1im"
  )

  // several words that might otherwise match pattersn get the -es suffix
  category(
    List(
      "acropolis", "aegis", "asbestos", "alias", "atlas",
      "bathos", "bias", "bus",
      "caddis", "cannabis", "canvas", "chaos", "circus", "cosmos",
      "dais", "digitalis",
      "epidermis", "ethos",
      "gas", "glottis",
      "ibis",
      "lens",
      "mantis", "marquis",
      "metropolis",
      "pathos", "pelvis", "polis",
      "rhinoceros",
      "sassafras",
      "trellis"
    ),
    "(.*)$", "$1es"
  )

  // Words ending in ch where it sounds like "k" get s simple -s suffix, not -es
  category(
    List(
      "stomach",
      "epoch"
    ),
    "(.*)ch$", "$1chs"
  )

  // The preceding irrelgular, category and noplural rules are processed first so now we can handle some standard
  // rules without regard to specific words, just word ending patterns
  standard(
    List(
      ("trix$", "trices"), // trix at end of word becomes trices (matrix, index, etc.)
      ("eau$", "eaux"), // eau at end of word becomes eaux (tableau)
      ("ieu$", "ieux"), // ieu at end of word become ieux (millieu)
      ("(..[iay])nx$", "$1nges") // words ending in ynx, anx, or inx with at least two letters prior become nges
    )
  )

  // Now, a standard rule: words ending in -ch -sh -z or -x become -es at the end
  standard("([cs]h|[zx])$", "$1es")

  // The suffixes -ch, -sh, and -ss all take -es in the plural (churches, classes, etc)...
  standard(
    List(
      ("([cs])h$", "$1hes"),
      ("ss$", "sses")
    )
  )

  // Certain words ending in -f or -fe take -ves in the plural (lives, wolves, etc)...
  standard(
    List (
      ("([aeo]l)f$", "$1ves"), // elf -> elves
      ("([^d]ea)f$", "$1ves"), // leaf -> leaves
      ("(ar)f$", "$1ves"), // scarf -> scarves
      ("([nlw]i)fe$", "$1ves") // wife -> wives
    )
  )

  // Words ending in -y become -ys
  standard(
    List(
      ("([aeiou])y$", "$1ys"), // boy -> boys
      ("y$", "ies") // ply -> plies
    )
  )

  // Vowels followed by -o end in -os
  standard("([aeiou])o$", "$1os")

  // Anything else followed by -o end in -oes
  standard ("o$", "oes")

  // Otherwise, assume that the plural just adds -s
  standard("(.*)$", "$1s")
}

