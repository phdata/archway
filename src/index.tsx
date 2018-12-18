import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { createHashHistory } from 'history';
import { Provider } from 'react-redux';
import App from './containers/App';
import store from './redux';
import './index.less';

if (module.hot) {
  module.hot.accept();
}

const history = createHashHistory();

const providerStore = store(history);

ReactDOM.render(
  <Provider store={providerStore}>
    <App history={history} />
  </Provider>,
  document.getElementById('root') as HTMLElement,
);
