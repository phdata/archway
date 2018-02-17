import React from "react";
import DetailPanel from "./DetailPanel";
import "./Overview.css";

const translate = (truthy) => truthy ? "Yes" : "No";

const Compliance = ({workspace: {compliance: {pii_data, pci_data, phi_data}}}) => (
    <DetailPanel title="Overview">
        <div>PII? {translate(pii_data)}</div>
        <div>PHI? {translate(phi_data)}</div>
        <div>PCI? {translate(pci_data)}</div>
    </DetailPanel>
);

export default Compliance;