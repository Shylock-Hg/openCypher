/*
 * Copyright (c) 2015-2020 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools.tck.inspection.browser.web

import org.opencypher.tools.tck.api.Pickle
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.OrderGroupsDepthFirst
import scalatags.Text
import scalatags.Text.all._

case class BrowserPages(browserModel: BrowserModel, browserRoutes: BrowserRoutes) extends PageBasic {

  def browserReportPage(): Text.TypedTag[String] = {
    page(
      pageTitle("Browse"),
      div(code(browserModel.path)),
      sectionTitle("Counts"),
      browserCountsFrag(browserModel.counts)
    )
  }

  def browserCountsFrag(counts: Map[Group, Seq[Scenario]]): Text.TypedTag[String] = {
    val groupSequence = OrderGroupsDepthFirst(counts.keySet)
    val totalCount = counts.get(groupSequence.head).map(_.size).getOrElse(Int.MaxValue)

    val tableRows = groupSequence.map( group =>
      tr(
        td(textIndent:=group.indent.em)(
          group.name
        ),
        td(textAlign.right)(
          counts.get(group).map(col =>
            a(href:=browserRoutes.listScenariosURL(this, group))(col.size)
          ).getOrElse("-")
        ),
        td(textAlign.right)(
          counts.get(group).map(col => {
            val pct = (col.size * 100).toDouble / totalCount
            frag(f"$pct%3.1f %%")
          }).getOrElse("-")
        )
      )
    )

    //output header
    val header =
      tr(
        th("Group"),
        th("Count"),
        th("of Total"),
      )

    table(CSS.hoverTable)(header +: tableRows)
  }

  def scenarioPage(scenario: Scenario, withLocation: Boolean = true): Text.TypedTag[String] = {
    page(
      // location
      if(withLocation)
        frag(
          div(CSS.locationLine)(scenarioLocationFrag(scenario)),
          blankLink(browserRoutes.openScenarioInEditorURL(this, scenario),
            div(CSS.fileLocation)(
              scenario.sourceFile.toAbsolutePath.toString + ":" + Pickle(scenario.source, withLocation = true).location.map(_.line).getOrElse(0)
            )
          )
        )
      else
        frag(),
      // title
      div(CSS.scenarioTitleBox, CSS.scenarioTitleBig)(scenarioTitle(scenario)),
      // tags
      if(scenario.tags.isEmpty)
        frag()
      else
        div(CSS.tagLine)(
          div("Tags:"),
          scenario.tags.toSeq.sorted.map(tag => div(CSS.tag)(tag))
        ),
      // steps
      scenario.steps.map(stepFrag)
    )
  }
}