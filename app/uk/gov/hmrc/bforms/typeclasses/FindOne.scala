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

package uk.gov.hmrc.bforms.typeclasses

import play.api.libs.json.{ JsObject, Json }
import scala.concurrent.{ ExecutionContext, Future }
import uk.gov.hmrc.bforms.model.Schema
import uk.gov.hmrc.bforms.repositories.SchemaRepository

trait FindOne[T] {
  def apply(selector: JsObject)(implicit ex: ExecutionContext): Future[Option[T]]
}

object FindOne {
  implicit def schema(implicit repo: SchemaRepository) = new FindOne[Schema] {
    def apply(selector: JsObject)(implicit ex: ExecutionContext): Future[Option[Schema]] = {
      repo.findOne(selector, Json.obj())
    }
  }
}