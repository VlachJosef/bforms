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

package uk.gov.hmrc.bforms.controllers

import play.api.mvc.Action
import scala.concurrent.Future
import uk.gov.hmrc.play.microservice.controller.BaseController

class Forms() extends BaseController {
  def all() = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def save() = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def allById(formTypeId: String) = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def getByIdAndVersion(formTypeId: String, version: String) = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def get(formTypeId: String, version: String, id: String) = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def delete(formTypeId: String, version: String, id: String) = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def submission(formTypeId: String, formId: String) = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

  def submissionStatus(formTypeId: String, formId: String) = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }
}