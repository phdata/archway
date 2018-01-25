import React, { Component } from 'react';
import logo from '../react.svg';
import Button from './Button'
import './Home.css';

class Home extends Component {

  click() {
    console.log('something');
  }

  render() {
    return (
      <div className="Home">
        <div className="Home-header">
          <Button busy={false} onClick={this.click}>Something</Button>
          <img src={logo} className="Home-logo" alt="logo" />
          <h2>Welcome to Razzle</h2>
        </div>
        <p className="Home-intro">
          To get started, edit <code>src/App.js</code> or{' '}
          <code>src/Home.js</code> and save to reload.
        </p>
      </div>
    );
  }
}

export default Home;
