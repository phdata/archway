import React from "react";
import DetailPanel from "./DetailPanel";

const yarn = ({workspace: {yarn: {pool_name, cores, memory}}}) => (
    <DetailPanel title="YARN">
        {pool_name}
    </DetailPanel>
);

export default yarn;