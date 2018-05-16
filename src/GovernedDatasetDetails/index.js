import React from "react";
import {setDataset, requestNewMember, removeMember} from "./actions";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import Compliance from "./Compliance";
import DatabaseDisplay from "../Common/DatabaseDisplay";
import ProcessingDisplay from "../Common/ProcessingDisplay";
import WorkspaceMemberList from "../SharedWorkspaceDetails/WorkspaceMemberList";
import "./GovernedDatasetDetails.css"

const DatasetDetails = ({dataset, members, cluster, requestNewMember, removeMember}) => (
    <div className="GovernedDatasetDetails-details">
        <div className="GovernedDatasetDetails-details-left">
            <DatabaseDisplay database={dataset.data}
                             cluster={cluster}/>
           <ProcessingDisplay processing={dataset.processing}
             cluster={cluster} />
        </div>
        <div className="GovernedDatasetDetails-details-right">
            <WorkspaceMemberList members={members} onAdd={requestNewMember} onRemove={removeMember} />
        </div>
    </div>
);

const DatasetLink = ({setDataset, datasetName, active, children}) => (
    <a
        onClick={() => setDataset(datasetName)}
        className={active === datasetName ? "active" : ""}>
        {children}
    </a>
);

const GovernedDatasetDetails = ({datasetDetails: {dataset, active: {dataset: activeDataset, name: activeName, members: activeMembers}}, cluster, setDataset, requestNewMember = () => {}, removeMember = () => {}}) => {
    if (!activeDataset)
        return <Spinner>Loading Details...</Spinner>;
    else {
        const {name, purpose} = dataset;
        return (
            <div className="GovernedDatasetDetails">
                <div className="GovernedDatasetDetails-header">
                    <h1>{name}</h1>
                    <Compliance workspace={dataset}/>
                </div>
                <h5>{purpose}</h5>
                <div className="SharedWorkspaceDetails-switcher">
                    <h2>Which dataset?</h2>
                    <DatasetLink
                        setDataset={setDataset}
                        datasetName="raw"
                        active={activeName}>
                        Raw
                    </DatasetLink>
                    <DatasetLink
                        setDataset={setDataset}
                        datasetName="staging"
                        active={activeName}>
                        Staging
                    </DatasetLink>
                    <DatasetLink
                        setDataset={setDataset}
                        datasetName="modeled"
                        active={activeName}>
                        Modeled
                    </DatasetLink>
                </div>
                <DatasetDetails dataset={activeDataset} members={activeMembers} cluster={cluster} requestNewMember={requestNewMember} removeMember={removeMember} />
            </div>
        );
    }
};

export default connect(
    state => state,
    {setDataset, requestNewMember, removeMember}
)(GovernedDatasetDetails);
