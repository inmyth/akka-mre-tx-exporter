name := "akka-mre-tx-exporter"

version := "0.1"

scalaVersion := "2.12.4"

lazy val akkaHttpVersion = "10.1.0-RC2"
lazy val akkaVersion = "2.5.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "com.typesafe.play" %% "play-json"            % "2.6.8",
  "javax.inject" % "javax.inject" % "1",
  "com.neovisionaries" % "nv-websocket-client" % "2.3",
  "com.typesafe.play" %% "play-functional" % "2.6.8",
  "com.beachape.filemanagement" %% "schwatcher" % "0.3.2",
  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
  "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
)

excludeDependencies += "commons-logging" % "commons-logging"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
//libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "0.17"
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "3.2.1",
  "mysql" % "mysql-connector-java"         % "8.0.9-rc",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "3.2.1",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "commons-pool" % "commons-pool" % "1.6",
  "commons-dbcp" % "commons-dbcp" % "1.4"


)

libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.21.0.1"
libraryDependencies += "org.json" % "json" % "20180130"