package docspell.common

case class Banner(
    component: String,
    version: String,
    gitHash: Option[String],
    jdbcUrl: LenientUri,
    configFile: Option[String],
    appId: Ident,
    baseUrl: LenientUri
) {

  private val banner =
    """______                          _ _
      ||  _  \                        | | |
      || | | |___   ___ ___ _ __   ___| | |
      || | | / _ \ / __/ __| '_ \ / _ \ | |
      || |/ / (_) | (__\__ \ |_) |  __/ | |
      ||___/ \___/ \___|___/ .__/ \___|_|_|
      |                    | |
      |""".stripMargin +
      s"""                    |_| v$version (#${gitHash.map(_.take(8)).getOrElse("")})"""

  def render(prefix: String): String = {
    val text = banner.split('\n').toList ++ List(
      s"<< $component >>",
      s"Id:       ${appId.id}",
      s"Base-Url: ${baseUrl.asString}",
      s"Database: ${jdbcUrl.asString}",
      s"Config:   ${configFile.getOrElse("")}",
      ""
    )

    text.map(line => s"$prefix  $line").mkString("\n")
  }
}
