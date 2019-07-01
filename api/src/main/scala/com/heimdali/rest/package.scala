package com.heimdali

import io.circe.Printer
import org.http4s.circe.CirceInstances

package object rest extends CirceInstances {

  override val defaultPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

}
