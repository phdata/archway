import React from "react";
import DetailPanel from "./DetailPanel";
import Spinner from "../Common/Spinner";

const yarn = ({workspace: {yarn: {pool_name}}}) => {
    let content;
    if (pool_name)
        content = <div>{pool_name}</div>;
    else
        content = <Spinner width={50}>Provisioning...</Spinner>;
    return (
        <DetailPanel title="YARN">
            {content}
        </DetailPanel>
    );
};

export default yarn;