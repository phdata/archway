import React from 'react';
import ReactDOM from 'react-dom';
import configureStore from "./store/configureStore";
import App from "./App";
import {Provider} from "react-redux";

const store = configureStore();

const rootEl = document.getElementById('root');

let render = () => {
    ReactDOM.render(
        <Provider store={store}>
            <App/>
        </Provider>,
        rootEl
    );
};

if(module.hot) {
    // Support hot reloading of components
    // and display an overlay for runtime errors
    const renderApp = render;
    const renderError = (error) => {
        const RedBox = require("redbox-react");
        ReactDOM.render(
            <RedBox error={error} />,
            rootEl,
        );
    };

    render = () => {
        try {
            renderApp();
        }
        catch(error) {
            renderError(error);
        }
    };

    module.hot.accept("./App", () => {
        setTimeout(render);
    });
}

render();