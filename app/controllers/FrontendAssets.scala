package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class FrontendAssets @Inject() (env: Environment) extends Controller {

  def versioned(asset: Assets.Asset): Action[AnyContent] = {
    val modeFolder = env.mode match {
      case Mode.Prod => "dist"
      case Mode.Dev => "dev"
      case Mode.Test => "test"
    }
    // Return Action created by Assets.versioned
    Assets.versioned(s"/public/lib/frontend/$modeFolder", asset)
  }

}
