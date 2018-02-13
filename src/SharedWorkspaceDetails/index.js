import React from "react";

const SharedWorkspaceDetails = ({match: {params: {id}}}) => (
    <div>
        You selected id {id}
    </div>
);

export default SharedWorkspaceDetails;