import React, { Component } from 'react';
import logo from '../react.svg';
import Button from './Button'
import './RequestProjects.css';

class RequestProjects extends Component {

  click() {
    console.log('something');
  }

  render() {
    return (
      <div className="CrerateProject">
        <h1>Request a new project</h1>
        <div>
          <form>
            <Panel
          </form>
        </div>
      </div>
    );
  }
}

export default RequestProjects;