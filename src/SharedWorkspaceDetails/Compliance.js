import React from "react";
import "./Compliance.css";

const ComplianceItem = ({name, value, icon}) => (
    <div className={`Compliance-item ${value}`}>
        <i className={`fas fa-${icon}`} style={{color: "#E74C3C"}}/> {name} possible
    </div>
);

const Compliance = ({workspace: {compliance: {pii_data, pci_data, phi_data}}}) => (
    <div className="Compliance">
        <ComplianceItem name="PII data" value={pii_data} icon="id-card"/>
        <ComplianceItem name="PCI data" value={pci_data} icon="credit-card"/>
        <ComplianceItem name="PHI data" value={phi_data} icon="stethoscope"/>
    </div>
);

export default Compliance;