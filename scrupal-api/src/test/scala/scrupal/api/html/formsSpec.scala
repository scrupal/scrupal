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

package scrupal.api.html

import java.time.Instant

import scrupal.api.html.Forms._
import scrupal.test.ScrupalSpecification

import scalatags.Text.all._

/** Test Cases For HTML pages */
class formsSpec extends ScrupalSpecification("forms") {

  s"$specName" should {
    "generate a checked attribute" in {
      Forms.with_checked(true) must contain(checked := "checked")
      Forms.with_checked(false) must not contain((checked := "checked").asInstanceOf[AttrPair])
    }
    "handle all the kinds of form fields" in {
      import Forms._
      val f = form("/testForm", "test",
        checkbox("checkbox", is_checked=true),
        color("color", Some("blue")),
        date("date", Some(Instant.ofEpochMilli(42))),
        datetime("datetime", Some(Instant.ofEpochMilli(42))),
        datetime_local("datetime_local", Some(Instant.ofEpochMilli(42))),
        email("email", Some("foo@nowhere.org")),
        file("file", Some("/path/to/file")),
        hidden("hidden", Some("can't see me")),
        image("image", href := "http://example.com/img.jpg"),
        month("month", Some(Instant.ofEpochMilli(42))),
        number("number", Some(42.0), 0.0, 100.0),
        password("password", Some("password")),
        radio("radio", Some("value"), is_checked=false),
        range("range", Some(42.0), 0.0, 100.0),
        reset("reset", None),
        search("search", Some("search")),
        submit("submit","submitted"),
        telephone("telephone", Some("972-555-1212")),
        text("text", Some("text")),
        time("time", Some(Instant.ofEpochMilli(42))),
        url("url", Some("http://nowhere.org/")),
        week("week", Some("tuesday")),
        list("list", Some("item"), datalist="datalist"),
        datalist("datalist", Seq("item", "for", "listing")),
        select("select", Some("value"), Map("option" → "choice", "value" → "value")),
        multiselect("multiselect", Seq("value1", "value2"), Map("option" → "choice", "value" → "value")),
        button("button", None),
        reset_button("reset_button", None),
        submit_button("submit_button", None),
        label("label", "for_field"),
        output("output", "for_attr")
      )
      f.render must beEqualTo("""<form action="/testForm" method="POST" name="test" enctype="application/x-www-form-urlencoded"><input type="checkbox" name="checkbox" checked="checked" value="checkbox" /><input type="color" name="color" value="blue" /><input type="date" name="date" value="1970-01-01T00:00:00.042Z" /><input type="datetime" name="datetime" value="1970-01-01T00:00:00.042Z" /><input type="datetime-local" name="datetime_local" value="1970-01-01T00:00:00.042Z" /><input type="email" name="email" value="foo@nowhere.org" /><input type="file" name="file" value="/path/to/file" /><input type="hidden" name="hidden" value="can't see me" /><input type="image" name="image" href="http://example.com/img.jpg" /><input type="month" name="month" value="1970-01-01T00:00:00.042Z" /><input type="number" name="number" min="0.0" max="100.0" value="42.0" /><input type="password" name="password" value="password" /><input type="radio" name="radio" value="value" /><input type="range" name="range" min="0.0" max="100.0" value="42.0" /><input type="reset" name="reset" /><input type="search" name="search" value="search" /><input type="submit" name="submit" value="submitted" /><input type="tel" name="telephone" value="972-555-1212" /><input type="text" name="text" value="text" /><input type="time" name="time" value="1970-01-01T00:00:00.042Z" /><input type="url" name="url" value="http://nowhere.org/" /><input type="week" name="week" value="tuesday" /><input type="list" name="list" id="list" list="datalist" value="item" /><datalist id="datalist"></datalist><select name="select" value="value"><option value="choice">option</option><option value="value">value</option></select><select name="multiselect[]" id="multiselect" multiple="multiple"></select><button type="button">button</button><button type="reset">reset_button</button><button type="submit">submit_button</button><label for="for_field">label</label><output name="output" for="for_attr"></output></form>""")
    }
  }
}
