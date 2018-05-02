import React from "react";
import {Field, reduxForm} from 'redux-form';
import Button from "../Common/Button";
import Checkbox from "../Common/Checkbox";

const RequestForm = ({className, handleSubmit, pristine, submitting}) => (
    <form onSubmit={handleSubmit} className={className}>
        <h2>Let's get you a place for your data!</h2>
        <label>NAME YOUR DATASET</label>
        <Field name="name" component="input" type="text"/>
        <label>WHAT'S THE PURPOSE (OR REQUEST #)?</label>
        <Field name="purpose" component="textarea" type="textarea"/>
        <label>THIS DATA MAY CONTAIN...</label>
        <div className="compliance">
            <Checkbox name="pci_data">PCI</Checkbox>
            <Checkbox name="pii_data">PII</Checkbox>
            <Checkbox name="phi_data">PHI</Checkbox>
        </div>
        <Button type="submit" disabled={submitting}>Request</Button>
    </form>
);

export default reduxForm({
    form: 'dataset'
})(RequestForm);