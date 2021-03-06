package controllers



import play.api.libs.json.Json
import security.UserAuthAction
import play.api.mvc.{Action, Controller}
import services.{ReadService, TagEventProducer}


class TagController(tagEventProducer: TagEventProducer,
    userAuthAction: UserAuthAction, readService: ReadService) extends Controller {

  def createTag() = userAuthAction { implicit request =>
    createTagForm.bindFromRequest.fold(
      formWithErrors => BadRequest,
      data => {
        tagEventProducer.createTag(data.text, request.user.userId)
        Ok
      }
    )
  }

  def deleteTag() = userAuthAction { implicit request =>
    deleteTagFrom.bindFromRequest.fold(
      formWithErrors => BadRequest,
      data => {
        tagEventProducer.deleteTag(data.id, request.user.userId)
        Ok
      }
    )
  }

  import scala.util.{Failure, Success}
  import play.api.mvc.Action
  def getTags = Action { implicit request =>
    val tagsT = readService.getAllTags
    tagsT match {
      case Failure(th) => InternalServerError
      case Success(tags) => Ok(Json.toJson(tags))
    }
  }

  import play.api.data.Form
  import play.api.data.Forms._
  val createTagForm = Form {
    mapping(
      "text" -> nonEmptyText
    )(CreateTagData.apply)(CreateTagData.unapply)
  }

  val deleteTagFrom = Form {
    mapping(
      "id" -> uuid
    )(DeleteTagData.apply)(DeleteTagData.unapply)
  }

  import java.util.UUID
  case class CreateTagData(text: String)
  case class DeleteTagData(id: UUID)
}
