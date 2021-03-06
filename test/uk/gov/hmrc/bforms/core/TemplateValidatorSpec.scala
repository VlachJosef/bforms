/*
 * Copyright 2017 HM Revenue & Customs
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
 */

package uk.gov.hmrc.bforms.core

import cats.data.NonEmptyList
import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import play.api.libs.json.{ JsNull, Json, JsNumber }
import uk.gov.hmrc.bforms.exceptions.InvalidState
import uk.gov.hmrc.bforms.models._

class TemplateValidatorSpec extends FlatSpec with Matchers with EitherValues {

  "TemplateValidator.conform" should "validate template against template schema" in {

    val schema =
      """|{
         | "type": "object",
         | "properties": {
         |    "formTypeId": {
         |      "type": "string"
         |    },
         |    "sections": {
         |      "type": "array",
         |      "items": {
         |        "type": "object",
         |        "properties": {
         |          "title": {
         |            "type": "string"
         |          }
         |        },
         |        "required": [
         |          "title"
         |        ]
         |      }
         |    }
         |  },
         |  "required": [
         |    "formTypeId"
         |  ]
         |}""".stripMargin

    val template =
      """|{
         |  "formTypeId": "IPT100",
         |  "sections": [
         |    {
         |      "title": "Your details | eich manylion"
         |    }
         |  ]
         |}""".stripMargin

    val res =
      for {
        schemaRes <- SchemaValidator.conform(Json.parse(schema).as[Schema])
        tr <- schemaRes.conform(Json.parse(template)).toEither
      } yield tr

    res.right.value should be(())
  }

  it should "validate template with nested arrays against template schema" in {

    val schema =
      """|{
         | "type": "object",
         | "properties": {
         |    "formTypeId": {
         |      "type": "string"
         |    },
         |    "sections": {
         |      "type": "array",
         |      "items": {
         |        "type": "object",
         |        "properties": {
         |          "title": {
         |            "type": "string"
         |          },
         |          "fields": {
         |            "type": "array",
         |            "items": {
         |              "type": "object",
         |              "properties": {
         |                "id": {
         |                  "type": "string"
         |                },
         |                "label": {
         |                  "type": "string"
         |                },
         |                "mandatory": {
         |                  "type": "string"
         |                },
         |                "readonly": {
         |                  "type": "string"
         |                }
         |              },
         |              "required": [
         |                "id",
         |                "label"
         |              ]
         |            }
         |          }
         |        },
         |        "required": [
         |          "title"
         |        ]
         |      }
         |    }
         |  },
         |  "required": [
         |    "formTypeId"
         |  ]
         |}""".stripMargin

    val template =
      """|{
         |  "formTypeId": "IPT100",
         |  "sections": [
         |    {
         |      "title": "Your details | eich manylion",
         |      "fields": [
         |        {
         |          "id": "iptRegNum",
         |          "label": "Insurance Premium Tax (IPT) registration number | Treth Premiwm Yswiriant (IPT) rhif cofrestru",
         |          "readonly": "true",
         |          "mandatory": "true"
         |        }
         |      ]
         |    }
         |  ]
         |}""".stripMargin

    val res =
      for {
        schemaRes <- SchemaValidator.conform(Json.parse(schema).as[Schema])
        tr <- schemaRes.conform(Json.parse(template)).toEither
      } yield tr

    res.right.value should be(())
  }

  val businessDetailsSection = Section(
    "Business details",
    List(
      FieldValue(FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, true),
      FieldValue(FieldId("businessAddress"), Address, "Business address", None, None, true)
    )
  )

  val sectionWithDate = Section(
    "Business details",
    List(
      FieldValue(FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, true),
      FieldValue(FieldId("startDate"), Date(AnyDate, Offset(0), None), "Start date", None, None, true)
    )
  )

  val sectionWithCheckbox = Section(
    "Business details",
    List(
      FieldValue(
        FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, true
      ),
      FieldValue(
        FieldId("dutyType"), Choice(Checkbox, NonEmptyList("Natural gas", List("Other gas")), Vertical, List.empty[Int]), "Select the tax type", None, None, true
      )
    )
  )

  val sectionWithRadio = Section(
    "Business details",
    List(
      FieldValue(
        FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, true
      ),
      FieldValue(
        FieldId("dutyType"), Choice(Radio, NonEmptyList("Natural gas", List("Other gas")), Vertical, List.empty[Int]), "Select the tax type", None, None, true
      )
    )
  )

  val sectionWithYesNo = Section(
    "Business details",
    List(
      FieldValue(
        FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, true
      ),
      FieldValue(
        FieldId("taxType"), Choice(YesNo, NonEmptyList.of("Yes", "No"), Horizontal, List.empty[Int]), "Gas tax type?", None, None, true
      )
    )
  )

  "TemplateValidator.getMatchingSection" should "find matching section containing address component" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("businessAddress.street1"), "street1"),
      FormField(FieldId("businessAddress.street2"), "street2"),
      FormField(FieldId("businessAddress.street3"), "street3"),
      FormField(FieldId("businessAddress.town"), "town"),
      FormField(FieldId("businessAddress.county"), "county"),
      FormField(FieldId("businessAddress.postcode"), "postcode"),
      FormField(FieldId("businessAddress.country"), "country")
    )
    val sections = List(businessDetailsSection)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "succeed to find matching section containing address component when optional fields are not present" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("businessAddress.street1"), "street1"),
      FormField(FieldId("businessAddress.town"), "town"),
      FormField(FieldId("businessAddress.postcode"), "postcode")
    )
    val sections = List(businessDetailsSection)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "fail to find matching section containing address component when mandatory fields are not present" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("businessAddress.town"), "town"),
      FormField(FieldId("businessAddress.county"), "county"),
      FormField(FieldId("businessAddress.postcode"), "postcode")
    )
    val sections = List(businessDetailsSection)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('left)
  }

  it should "fail to find matching section containing address component when field not in form template is present" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("businessAddress.street1"), "street1"),
      FormField(FieldId("businessAddress.town"), "town"),
      FormField(FieldId("businessAddress.county"), "county"),
      FormField(FieldId("businessAddress.postcode"), "postcode"),
      FormField(FieldId("attacker.injected.field"), "); drop all tables;")
    )
    val sections = List(businessDetailsSection)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('left)
  }

  it should "find matching section containing date component" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("startDate.day"), "1"),
      FormField(FieldId("startDate.month"), "12"),
      FormField(FieldId("startDate.year"), "2000")
    )
    val sections = List(sectionWithDate)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "fail to find matching section containing date component when mandatory fields are not present" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("startDate.month"), "12"),
      FormField(FieldId("startDate.year"), "2000")
    )
    val sections = List(sectionWithDate)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('left)
  }

  it should "fail to find matching section containing date component when field not in form template is present" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("startDate.day"), "1"),
      FormField(FieldId("startDate.month"), "12"),
      FormField(FieldId("startDate.year"), "2000"),
      FormField(FieldId("attacker.injected.field"), "); drop all tables;")
    )
    val sections = List(sectionWithDate)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('left)
  }

  it should "succeed to find matching section containing only text field which is not mandatory" in {

    val section = Section(
      "Business details",
      List(
        FieldValue(FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, false)
      )
    )

    val formFields = List() // Nothing submitted

    val sections = List(section)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "succeed to find matching section containing only text field which is not mandatory 2" in {

    val section = Section(
      "Business details",
      List(
        FieldValue(FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, false)
      )
    )

    val formFields = List() // Nothing submitted

    val sections = List(section)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "fail to find matching section containing only text field which is mandatory" in {

    val section = Section(
      "Business details",
      List(
        FieldValue(FieldId("nameOfBusiness"), Text(Constant("")), "Name of business", None, None, true)
      )
    )

    val formFields = List() // Nothing submitted

    val sections = List(section)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('left)
  }

  it should "find matching section containing Checkbox component" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("dutyType"), "0,1")
    )
    val sections = List(sectionWithCheckbox)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "find matching section containing Radio component" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("dutyType"), "0")
    )
    val sections = List(sectionWithRadio)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }

  it should "find matching section containing YesNo component" in {

    val formFields = List(
      FormField(FieldId("nameOfBusiness"), "Apple inc."),
      FormField(FieldId("taxType"), "0")
    )
    val sections = List(sectionWithYesNo)
    val res = TemplateValidator.getMatchingSection(formFields, sections)

    res should be('right)
  }
}
