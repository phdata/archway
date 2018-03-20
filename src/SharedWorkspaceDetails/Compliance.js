import React from "react";
import "./Compliance.css";

const ComplianceItem = ({name, value, icon}) => (
    <div className={`Compliance-item ${value}`}>
        <i className={`fa fa-${icon}`} />
    </div>
);

const Compliance = ({workspace: {compliance: {pii_data, pci_data, phi_data}}}) => (
    <div className="Compliance">
        <ComplianceItem name="PHI data" value={phi_data} icon="stethoscope"/>
        <ComplianceItem name="PCI data" value={pci_data} icon="bank"/>
        <ComplianceItem name="PII data" value={pii_data} icon="info"/>
    </div>
);

export default Compliance;