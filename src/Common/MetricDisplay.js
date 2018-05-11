import React from "react";
import "./MetricDisplay.css";

const MetricDisplay = ({metric, label}) => (
  <div className="MetricDisplay">
    <div className="MetricDisplay-metric">
      {metric}
    </div>
    <div className="MetricDisplay-label">
      {label}
    </div>
  </div>
);

export default MetricDisplay;
