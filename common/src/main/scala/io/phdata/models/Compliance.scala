package io.phdata.models

import io.circe.{Decoder, Encoder}

case class Compliance(phiData: Boolean, pciData: Boolean, piiData: Boolean, id: Option[Long] = None)

object Compliance {

  val empty = Compliance(phiData = false, pciData = false, piiData = false)

  implicit final val encoder: Encoder[Compliance] =
    Encoder.forProduct3("phi_data", "pci_data", "pii_data")(c => (c.phiData, c.pciData, c.piiData))

  implicit final val decoder: Decoder[Compliance] =
    Decoder.forProduct3("phi_data", "pci_data", "pii_data")(
      (phi: Boolean, pci: Boolean, pii: Boolean) => Compliance(phi, pci, pii)
    )

}
