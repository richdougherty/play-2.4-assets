# Play project with assets in subproject

This project demonstrates how assets are served from subprojects in Play 2.4. The documentation in Play is quite brief, so an example can be useful. Here are the docs that this project expands on: https://www.playframework.com/documentation/2.4.x/SBTSubProjects

First we create a project with a subproject. The project is named `frontend` and lives in a subfolder with the same name.

In `build.sbt`:
```scala
def commonSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .aggregate(frontend)
  .dependsOn(frontend)

lazy val frontend = project
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
```

Next we create some assets and look where they end up. The first asset is in the main project, the other assets are in the `frontend` subproject.

```
$ echo root > public/file.txt
$ echo dist > frontend/public/dist/file.txt
$ echo dev > frontend/public/dev/file.txt
$ echo test > frontend/public/test/file.txt
```

When we `package` the subproject wwe can see that the files get put in a JAR. All the assets are placed in a folder labelled with the subproject name and version (`frontend/0.1-SNAPSHOT`). This makes it possible for Play to choose the right assets, even if there are multiple assets with the same name.

```
[frontend] $ package
[info] Updating {file:/<path>/example/}frontend...
[info] Resolving jline#jline;2.12.1 ...
[info] Done updating.
[info] Packaging /<path>/example/frontend/target/scala-2.11/frontend_2.11-0.1-SNAPSHOT.jar ...
[info] Done packaging.
```

```
$ jar tf /<path>/example/frontend/target/scala-2.11/frontend_2.11-0.1-SNAPSHOT.jar
META-INF/MANIFEST.MF
META-INF/
META-INF/resources/
META-INF/resources/webjars/
META-INF/resources/webjars/frontend/
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/dev/
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/dist/
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/test/
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/dist/file.txt
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/test/file.txt
META-INF/resources/webjars/frontend/0.1-SNAPSHOT/dev/file.txt
```

When Play runs the application it puts them on the classpath at the location `/public/lib/frontend` (notice that the version is removed).

Thus with normal Assets controller routing that maps `/assets/*` to `/public/*`…

```
GET     /assets/*file               controllers.Assets.versioned(path="/public", file: Asset)
```

…we see the location of the assets. The main project asset is under `/public/*`, the `frontend` subproject assets are under `/public/lib/frontend/*`.

```
http://localhost:9000/assets/file.txt
http://localhost:9000/assets/lib/frontend/dist/file.txt
http://localhost:9000/assets/lib/frontend/dev/file.txt
http://localhost:9000/assets/lib/frontend/test/file.txt
```

## Serving assets by project mode

If you want to be really clever you can make a router that serves assets based on the project mode. Here's a controller that makes this possible. It uses dependency injection to pick up the project mode. Each project mode has a different path.

It delegates to the `Assets` controller to do the actual serving. We're using the `Assets.versioned` method instead of `Assets.at`. The `versioned` method is better for caching, but you can use the `at` method if you prefer.

```scala
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
```

If we make a new route for this controller then we will automatically map paths from `/frontend/*` to `/public/lib/frontend/<mode>/*`.

```
GET     /frontend/*file             controllers.FrontendAssets.versioned(file: Asset)
```

The following URL will now work and serve the file from the `frontend` project in the `public/dev` folder.

```
http://localhost:9000/frontend/file.txt
```
