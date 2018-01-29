import React, {Component} from 'react';
import './RequestProject.css';

class Home extends Component {

    static click() {
        console.log('something');
    }

    render() {
        return (
            <div className="Home">
                <h1>here at home</h1>
            </div>
        );
    }
}

export default Home;